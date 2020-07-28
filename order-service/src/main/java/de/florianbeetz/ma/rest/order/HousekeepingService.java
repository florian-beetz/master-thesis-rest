package de.florianbeetz.ma.rest.order;

import de.florianbeetz.ma.rest.order.client.payment.PaymentApi;
import de.florianbeetz.ma.rest.order.client.shipping.ShippingApi;
import de.florianbeetz.ma.rest.order.client.shipping.ShippingStatus;
import de.florianbeetz.ma.rest.order.data.OrderRepository;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class HousekeepingService {

    private final OrderRepository orderRepository;
    private final ShippingApi shippingApi;
    private final PaymentApi paymentApi;

    @Autowired
    public HousekeepingService(OrderRepository orderRepository, ShippingApi shippingApi, PaymentApi paymentApi) {
        this.orderRepository = orderRepository;
        this.shippingApi = shippingApi;
        this.paymentApi = paymentApi;
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
