package de.florianbeetz.ma.rest.order;

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
public class ShippingService {

    private final OrderRepository orderRepository;
    private final ShippingApi shippingApi;

    @Autowired
    public ShippingService(OrderRepository orderRepository, ShippingApi shippingApi) {
        this.orderRepository = orderRepository;
        this.shippingApi = shippingApi;
    }

    @Scheduled(fixedRate = 60 * 1000)
    public void updateShipments() {
        log.info("Updating shipments...");

        int updated = 0;
        val orders = orderRepository.findAllByStatus(OrderStatus.PAYMENT_RECEIVED.name());

        for (val order : orders) {
            val shipment = shippingApi.getShipment(order.getShipmentUrl());

            if (shipment.getStatus() == ShippingStatus.CREATED) {
                shippingApi.setShipmentStatus(shipment, ShippingStatus.READY_TO_SHIP);
                updated++;
            }
        }

        log.info("Updated {} shipments.", updated);
    }

}
