package de.florianbeetz.ma.rest.order;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ShippingService {

    @Scheduled(fixedRate = 60 * 1000)
    public void updateShipments() {
        log.info("Updating shipments...");

        int updated = 0;
        log.info("Updated {} shipments.", updated);
    }

}
