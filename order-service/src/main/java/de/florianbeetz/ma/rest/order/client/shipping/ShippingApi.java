package de.florianbeetz.ma.rest.order.client.shipping;

import de.florianbeetz.ma.rest.order.client.UnexpectedApiBehaviourException;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.LinkRelation;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
public class ShippingApi {

    public static final LinkRelation STATUS_RELATION = LinkRelation.of("status");

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

    public Shipment getShipment(String url) {
        val response = restTemplate.getForEntity(url, Shipment.class);
        return response.getBody();
    }

    public void setShipmentStatus(Shipment shipment, ShippingStatus status) {
        HttpStatus updateStatus;
        do {
            // request status to get ETag
            val response = restTemplate.getForEntity(shipment.getRequiredLink(STATUS_RELATION).getHref(), String.class);

            try {
                // update status
                val headers = new HttpHeaders();
                headers.setIfMatch(response.getHeaders().getETag());
                val putResponse = restTemplate.exchange(shipment.getRequiredLink(STATUS_RELATION).getHref(), HttpMethod.PUT, new HttpEntity<>(status.name(), headers), Object.class);

                updateStatus = putResponse.getStatusCode();
            } catch (HttpClientErrorException e) {
                updateStatus = e.getStatusCode();
            }
        } while (updateStatus == HttpStatus.PRECONDITION_FAILED);

        if (updateStatus != HttpStatus.NO_CONTENT) {
            throw new UnexpectedApiBehaviourException("Failed to update shipment status: API responded with with status " + updateStatus);
        }
    }

    public void deleteShipment(Shipment shipment) {
        String shipmentUrl = shipment.getRequiredLink(IanaLinkRelations.SELF).getHref();
        HttpStatus deleteStatus;
        do {
            // request entity to get ETag
            val response = restTemplate.getForEntity(shipmentUrl, Shipment.class);

            try {
                // update status
                val headers = new HttpHeaders();
                headers.setIfMatch(response.getHeaders().getETag());
                val deleteResponse = restTemplate.exchange(shipmentUrl, HttpMethod.DELETE, new HttpEntity<>(headers), Object.class);

                deleteStatus = deleteResponse.getStatusCode();
            } catch (HttpClientErrorException e) {
                deleteStatus = e.getStatusCode();
            }

        } while (deleteStatus == HttpStatus.PRECONDITION_FAILED);

        if (deleteStatus != HttpStatus.NO_CONTENT) {
            throw new UnexpectedApiBehaviourException("Failed to delete shipment: API responded with status " + deleteStatus);
        }
    }
}
