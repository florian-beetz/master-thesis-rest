package de.florianbeetz.ma.rest.order;

import java.util.stream.Collectors;

import de.florianbeetz.ma.rest.order.api.v1.OrderController;
import de.florianbeetz.ma.rest.order.client.inventory.InventoryApi;
import de.florianbeetz.ma.rest.order.client.payment.Payment;
import de.florianbeetz.ma.rest.order.client.payment.PaymentApi;
import de.florianbeetz.ma.rest.order.client.shipping.Shipment;
import de.florianbeetz.ma.rest.order.client.shipping.ShipmentCost;
import de.florianbeetz.ma.rest.order.client.shipping.ShippingAddress;
import de.florianbeetz.ma.rest.order.client.shipping.ShippingApi;
import de.florianbeetz.ma.rest.order.client.shipping.ShippingStatus;
import de.florianbeetz.ma.rest.order.data.OrderPositionEntity;
import de.florianbeetz.ma.rest.order.data.OrderRepository;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Slf4j
@Service
public class HousekeepingService {

    private final OrderRepository orderRepository;
    private final ShippingApi shippingApi;
    private final PaymentApi paymentApi;
    private final InventoryApi inventoryApi;

    @Value("${application.api.order.base-url}")
    private String rootUrl;

    @Autowired
    public HousekeepingService(OrderRepository orderRepository, ShippingApi shippingApi, PaymentApi paymentApi, InventoryApi inventoryApi) {
        this.orderRepository = orderRepository;
        this.shippingApi = shippingApi;
        this.paymentApi = paymentApi;
        this.inventoryApi = inventoryApi;
    }

    @Scheduled(cron = "${application.housekeeping.payment-create}")
    public void createPayments() {
        log.info("Creating payments for orders...");
        int created = 0;

        val orders = orderRepository.findAllByStatusWithShipmentUrlAndWithoutPaymentUrl(OrderStatus.CREATED.name());
        for (val order : orders) {
            try {
                double total = 0;
                for (val position : order.getPositions()) {
                    total += position.getAmount() * position.getItemPrice();
                }

                Shipment shipment = shippingApi.getShipment(order.getShipmentUrl())
                ShipmentCost shipmentCost = shippingApi.getShipmentCost(shipment);

                total += shipmentCost.getPrice();

                Payment payment = new Payment(total, getOrderUrl(order.getId()));
                val createdPayment = paymentApi.createPayment(payment);

                order.setPaymentUrl(paymentApi.getPaymentUrl(createdPayment));
                orderRepository.save(order);
                created++;
            } catch (Exception e) {
                log.error("Failed to create payment for order id={}", order.getId(), e);
            }
        }

        log.info("Created {} payments.", created);
    }

    @Scheduled(cron = "${application.housekeeping.shipment-create}")
    public void createShipment() {
        log.info("Creating shipments for orders...");
        int created = 0;

        val orders = orderRepository.findAllByStatusWithoutShipmentUrl(OrderStatus.CREATED.name());
        for (val order : orders) {
            val shipment = new Shipment(new ShippingAddress(order.getDeliveryStreet(), order.getDeliveryCity(), order.getDeliveryZip()),
                    getOrderUrl(order.getId()));
            val createdShipment = shippingApi.createShipment(shipment);

            order.setShipmentUrl(shippingApi.getShipmentUrl(createdShipment));
            orderRepository.save(order);
            created++;
        }

        log.info("Created {} shipments.", created);
    }

    @Scheduled(cron = "${application.housekeeping.ready-to-ship}")
    public void updateReadyToShipShipments() {
        log.info("Updating ready-to-ship shipments...");

        int updated = 0;
        val orders = orderRepository.findAllByStatus(OrderStatus.PAYMENT_RECEIVED.name());

        for (val order : orders) {
            log.debug("Updating shipment for order {}", order.getId());
            try {
                val shipment = shippingApi.getShipment(order.getShipmentUrl());

                if (shipment.getStatus() == ShippingStatus.CREATED) {
                    shippingApi.setShipmentStatus(shipment, ShippingStatus.READY_TO_SHIP);
                    updated++;
                }
            } catch (Exception e) {
                log.error("Failed to update shipment for order {}", order.getId(), e);
            }
        }

        log.info("Updated {} shipments.", updated);
    }

    @Scheduled(cron = "${application.housekeeping.dangling-subresources}")
    public void deleteSubResourcesOfCancelledOrders() {
        log.info("Deleting shipments for cancelled orders...");

        int deletedShipments = 0;
        int deletedPayments = 0;
        val orders = orderRepository.findAllByStatusAndHasSubResourceUrls(OrderStatus.CANCELED.name());

        for (val order : orders) {
            if (order.getShipmentUrl() != null) {
                log.debug("Deleting shipment for order {}", order.getId());
                try {
                    val shipment = shippingApi.getShipment(order.getShipmentUrl());

                    shippingApi.deleteShipment(shipment);
                    order.setShipmentUrl(null);
                    deletedShipments++;
                } catch (Exception e) {
                    log.error("Failed to delete shipment for order {}", order.getId(), e);
                }
            }

            if (order.getPaymentUrl() != null) {
                log.debug("Deleting payment for order {}", order.getId());
                try {
                    val payment = paymentApi.getPayment(order.getPaymentUrl());

                    paymentApi.deletePayment(payment);
                    order.setPaymentUrl(null);
                    deletedPayments++;
                } catch (Exception e) {
                    log.error("Failed to delete payment for order {}", order.getId(), e);
                }
            }
            orderRepository.save(order);
        }

        log.info("Deleted {} shipments and {} payments.", deletedShipments, deletedPayments);
    }


    @Scheduled(cron = "${application.housekeeping.inventory-update}")
    public void updateInventory() {
        log.info("Updating inventory for shipped and cancelled orders...");
        int updatedShipped = 0;
        int updatedCancelled = 0;

        val shippedOrders = orderRepository.findAllByStatusAndItemsBookedOutIsFalse(OrderStatus.SHIPPED.name());
        for (val order : shippedOrders) {
            log.debug("Booking out items of order {}", order.getId());
            try {
                val positions = order.getPositions().stream()
                                                   .collect(Collectors.toMap(OrderPositionEntity::getItemStock, OrderPositionEntity::getAmount));
                inventoryApi.bookOutItems(positions);

                order.setItemsBookedOut(true);
                orderRepository.save(order);
                updatedShipped++;
            } catch (Exception e) {
                log.error("Failed to book out items of order {}", order.getId(), e);
            }
        }

        val cancelledOrders = orderRepository.findAllByStatusAndItemsBookedOutIsFalse(OrderStatus.CANCELED.name());
        for (val order : cancelledOrders) {
            log.debug("Cancelling reservations for order {}", order.getId());
            try {
                val positions = order.getPositions().stream()
                        .collect(Collectors.toMap(OrderPositionEntity::getItemStock, OrderPositionEntity::getAmount));
                inventoryApi.rollbackReservation(positions);

                order.setItemsBookedOut(true);
                orderRepository.save(order);
                updatedCancelled++;
            } catch (Exception e) {
                log.error("Failed to roll back reservations of order {}", order.getId(), e);
            }
        }

        log.info("Booked out items of {} orders and cancelled reservations for {} orders.", updatedShipped, updatedCancelled);
    }

    private String getOrderUrl(long id) {
        return this.rootUrl + linkTo(methodOn(OrderController.class).getOrder(id)).toString();
    }

}
