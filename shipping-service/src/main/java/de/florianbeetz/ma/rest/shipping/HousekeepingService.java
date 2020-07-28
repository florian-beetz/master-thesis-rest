package de.florianbeetz.ma.rest.shipping;

import de.florianbeetz.ma.rest.shipping.client.order.OrderApi;
import de.florianbeetz.ma.rest.shipping.client.order.OrderStatus;
import de.florianbeetz.ma.rest.shipping.data.ShipmentRepository;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class HousekeepingService {

    private final ShipmentRepository shipmentRepository;
    private final OrderApi orderApi;

    @Autowired
    public HousekeepingService(ShipmentRepository shipmentRepository, OrderApi orderApi) {
        this.shipmentRepository = shipmentRepository;
        this.orderApi = orderApi;
    }

    @Scheduled(cron = "${application.housekeeping.order-update}")
    public void shipShipments() {
        log.info("Updating ready-to-ship shipments...");

        int updated = 0;
        val shipments = shipmentRepository.findAllByStatus(ShippingStatus.READY_TO_SHIP.name());

        for (val shipment : shipments) {
            log.debug("Updating shipment {}", shipment.getId());
            shipment.setStatus(ShippingStatus.SHIPPED.name());

            try {
                val order = orderApi.getOrder(shipment.getOrderUrl());
                orderApi.setOrderStatus(order, OrderStatus.SHIPPED);

                shipmentRepository.save(shipment);
                updated++;
            } catch (Exception e) {
                log.error("Failed to update shipment {}", shipment.getId(), e);
            }
        }

        log.info("Updated {} shipments...", updated);
    }

}
