package de.florianbeetz.ma.rest.order.client.shipping;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
public class ShippingApi {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public ShippingApi(@Autowired RestTemplate restTemplate,
                       @Value("${application.api.shipping.base-url}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    public Shipment createShipment(Shipment shipment) {
        String url = baseUrl + "/shipment/";
        log.debug("Creating shipment using URL '{}'", url);
        val response = restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(shipment), Shipment.class);
        return response.getBody(); // TODO: handle errors
    }

    public String getShipmentUrl(Shipment shipment) {
        return shipment.getRequiredLink("self").getHref(); // TODO: handle errors
    }

}
