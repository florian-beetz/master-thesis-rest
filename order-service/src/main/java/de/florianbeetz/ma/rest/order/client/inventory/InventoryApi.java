package de.florianbeetz.ma.rest.order.client.inventory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import de.florianbeetz.ma.rest.order.client.UnexpectedApiBehaviourException;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
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
     * Returns the {@link Item} identified by the given URL.
     *
     * @throws InvalidReferenceException Signals that the URL did not point to a valid {@link Item}.
     */
    public Item getItem(String url) {
        try {
            return restTemplate.getForObject(url, Item.class);
        } catch (Exception e) {
            throw new InvalidReferenceException("Failed to resolve item reference: " + e.getMessage(), e);
        }
    }

    /**
     * Returns the {@link ItemStock stock} of the given item.
     */
    public Iterator<ItemStock> getStock(Item item) {
        val stockUrl = item.getLink("stock").orElseThrow(() -> new UnexpectedApiBehaviourException("Item has no 'stock' relation."));
        return new PagedIterator<>(stockUrl.getHref(), restTemplate, new ParameterizedTypeReference<>() {
        });
    }

    /**
     * Reserves a given amount of stock of the item.
     *
     * @return a mapping from the URL of a stock position to the amount reserved of this position.
     */
    public Map<String, Long> reserveStock(Item item, long amount) {
        Iterator<ItemStock> stockIterator = getStock(item);

        Map<String, Long> reservedPositions = new HashMap<>();
        long remainingAmount = amount;
        while (stockIterator.hasNext() && remainingAmount > 0) {
            ItemStock stock = stockIterator.next();
            val selfLink = stock.getLink("self").orElseThrow();

            val reservedItems = tryReserveStock(stock, remainingAmount);
            remainingAmount = remainingAmount - reservedItems;
            if (reservedItems > 0) {
                reservedPositions.put(selfLink.getHref(), reservedItems);
            }
        }

        if (remainingAmount > 0) {
            log.debug("reservation of {} items is not satisfiable, rolling back", amount);
            rollbackReservation(reservedPositions);
            return null;
        }

        return reservedPositions;
    }

    /**
     * Tries to reserve the given amount of the given {@link ItemStock stock position}.
     *
     * @return the amount of stock that could be reserved.
     */
    public long tryReserveStock(ItemStock stock, long amount) {
        val selfLink = stock.getLink("self").orElseThrow(() -> new UnexpectedApiBehaviourException("ItemStock does not have 'self' relation."));

        HttpStatus updateStatus;
        long reserveItems;
        do {
            // request ItemStock on its own to get ETag
            val response = restTemplate.exchange(selfLink.getHref(), HttpMethod.GET, null, ItemStock.class);

            // calculate amount that can be reserved
            val body = response.getBody();
            reserveItems = Math.min(body.getAvailable(), amount);
            val updatedStock = new ItemStock(body.getInStock(), body.getAvailable() - reserveItems);

            try {
                // update stock position
                val headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.setIfMatch(response.getHeaders().getETag());
                val putResponse = restTemplate.exchange(selfLink.getHref(), HttpMethod.PUT, new HttpEntity<>(updatedStock, headers), ItemStock.class);

                updateStatus = putResponse.getStatusCode();
            } catch (HttpClientErrorException e) {
                updateStatus = e.getStatusCode();
            }
        } while (updateStatus == HttpStatus.PRECONDITION_FAILED);

        // status is not precondition failed
        if (updateStatus == HttpStatus.OK) {
            return reserveItems;
        } else {
            throw new UnexpectedApiBehaviourException("Failed to reserve items: API responded with status " + updateStatus + " when trying to update stock.");
        }
    }

    /**
     * Rolls back a previously made reservation.
     *
     * @param positions a mapping from the URL of a position to the amount of items reserved in this position.
     */
    public void rollbackReservation(Map<String, Long> positions) {
        StringBuilder error = null;
        for (Map.Entry<String, Long> entry : positions.entrySet()) {
            val result = addAvailableStock(entry.getKey(), entry.getValue());

            if (!result) {
                if (error == null) {
                    error = new StringBuilder("Failed to reverse reservation [");
                }
                error.append(String.format("%s -> %d", entry.getKey(), entry.getValue()));
            }
        }

        if (error != null) {
            error.append("]");
            throw new UnexpectedApiBehaviourException(error.toString());
        }
    }

    private boolean addAvailableStock(String url, long amount) {
        HttpStatus updateStatus;
        do {
            // request ItemStock on its own to get ETag
            val response = restTemplate.exchange(url, HttpMethod.GET, null, ItemStock.class);

            // calculate new available amount
            val body = response.getBody();
            val updatedStock = new ItemStock(body.getInStock(), body.getAvailable() + amount);

            try {
                // update stock position
                val headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.setIfMatch(response.getHeaders().getETag());
                val putResponse = restTemplate.exchange(url, HttpMethod.PUT, new HttpEntity<>(updatedStock, headers), ItemStock.class);

                updateStatus = putResponse.getStatusCode();
            } catch (HttpClientErrorException e) {
                updateStatus = e.getStatusCode();
            }
        } while (updateStatus == HttpStatus.PRECONDITION_FAILED);

        log.debug("adding {} to item {} resulted in status {}", amount, url, updateStatus);
        if (updateStatus != HttpStatus.OK) {
            log.error("Failed to roll back reservation of {} items in stock position {}: response code was {}", amount, url, updateStatus);
        }

        return updateStatus == HttpStatus.OK;
    }
}
