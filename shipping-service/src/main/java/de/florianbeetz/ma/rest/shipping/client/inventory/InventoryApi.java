package de.florianbeetz.ma.rest.shipping.client.inventory;

import java.util.Map;

import de.florianbeetz.ma.rest.shipping.client.UnexpectedApiBehaviourException;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

/**
 * Provides a wrapper for the REST API exposed by the inventory microservice.
 */
@Slf4j
@Service
public class InventoryApi {

    private final RestTemplate restTemplate;

    @Autowired
    public InventoryApi(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Books out stock positions.
     *
     * @param positions
     */
    public void bookOutStock(Map<String, Long> positions) {
        positions.forEach(this::bookOutStock);
    }

    /**
     * Books out a single stock position.
     *
     * @param position
     * @param amount
     */
    public void bookOutStock(String position, long amount) {
        HttpStatus updateStatus;
        do {
            // request ItemStock on its own to get ETag
            val response = restTemplate.exchange(position, HttpMethod.GET, null, ItemStock.class);

            // calculate new amount in stock
            val body = response.getBody();
            val updatedStock = new ItemStock(body.getInStock() - amount, body.getAvailable());

            try {
                // update stock position
                val headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.setIfMatch(response.getHeaders().getETag());
                val putResponse = restTemplate.exchange(position, HttpMethod.PUT, new HttpEntity<>(updatedStock, headers), ItemStock.class);

                updateStatus = putResponse.getStatusCode();
            } catch (HttpClientErrorException e) {
                updateStatus = e.getStatusCode();
            }
        } while (updateStatus == HttpStatus.PRECONDITION_FAILED);

        if (updateStatus != HttpStatus.OK) {
            throw new UnexpectedApiBehaviourException("Failed to book out " + amount + " items of position " + position + " : API responded with code " + updateStatus);
        }
    }
}
