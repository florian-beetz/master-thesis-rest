package de.florianbeetz.ma.rest.payment;

import de.florianbeetz.ma.rest.payment.client.order.OrderApi;
import de.florianbeetz.ma.rest.payment.client.order.OrderStatus;
import de.florianbeetz.ma.rest.payment.data.PaymentRepository;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class HousekeepingService {

    private final OrderApi orderApi;
    private final PaymentRepository paymentRepository;

    @Autowired
    public HousekeepingService(OrderApi orderApi, PaymentRepository paymentRepository) {
        this.orderApi = orderApi;
        this.paymentRepository = paymentRepository;
    }

    @Scheduled(cron = "${application.housekeeping.order-update}")
    public void updateOrders() {
        log.info("Updating orders...");

        int updated = 0;
        val payments = paymentRepository.findAllByStatusAndOrderUpdatedFalse(PaymentStatus.PAYED.name());

        for (val payment : payments) {
            log.debug("Updating order for payment {}", payment.getId());
            try {
                val order = orderApi.getOrder(payment.getOrderUrl());
                orderApi.setOrderStatus(order, OrderStatus.PAYMENT_RECEIVED);
                payment.setOrderUpdated(true);
                paymentRepository.save(payment);
                updated++;
            } catch (Exception e) {
                log.error("Failed to update order for payment {}", payment.getId());
            }
        }

        log.info("Updated {} orders.", updated);
    }

}
