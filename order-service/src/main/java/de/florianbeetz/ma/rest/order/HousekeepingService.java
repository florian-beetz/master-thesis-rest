package de.florianbeetz.ma.rest.order;

import de.florianbeetz.ma.rest.order.api.v1.OrderController;
import de.florianbeetz.ma.rest.order.client.inventory.InventoryApi;
import de.florianbeetz.ma.rest.order.client.payment.Payment;
import de.florianbeetz.ma.rest.order.client.payment.PaymentApi;
import de.florianbeetz.ma.rest.order.client.shipping.Shipment;
import de.florianbeetz.ma.rest.order.client.shipping.ShipmentCost;
import de.florianbeetz.ma.rest.order.client.shipping.ShippingAddress;
import de.florianbeetz.ma.rest.order.client.shipping.ShippingApi;
import de.florianbeetz.ma.rest.order.client.shipping.ShippingStatus;
import de.florianbeetz.ma.rest.order.data.OrderRepository;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
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

                ShipmentCost shipmentCost = shippingApi.getShipmentCost(order.getShipmentUrl());

                total += shipmentCost.getPrice();

                Payment payment = new Payment(total, linkTo(methodOn(OrderController.class).getOrder(order.getId())).toString());
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
                    linkTo(methodOn(OrderController.class).getOrder(order.getId())).toUri().toString());
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

}
