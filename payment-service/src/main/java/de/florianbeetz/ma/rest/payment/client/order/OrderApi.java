package de.florianbeetz.ma.rest.payment.client.order;

import de.florianbeetz.ma.rest.payment.client.UnexpectedApiBehaviourException;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.LinkRelation;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
public class OrderApi {

    public static final LinkRelation STATUS_RELATION = LinkRelation.of("status");

    private final RestTemplate restTemplate;

    @Autowired
    public OrderApi(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public Order getOrder(String url) {
        val response = restTemplate.getForEntity(url, Order.class);
        return response.getBody();
    }

    public void setOrderStatus(Order order, OrderStatus status) {
        String statusUrl = order.getRequiredLink(STATUS_RELATION).getHref();

        HttpStatus updateStatus;
        do {
            // request status to get current ETag
            val response = restTemplate.getForEntity(statusUrl, String.class);

            try {
                // update status
                val headers = new HttpHeaders();
                headers.setIfMatch(response.getHeaders().getETag());
                val updateResponse = restTemplate.exchange(statusUrl, HttpMethod.PUT, new HttpEntity<>(status.name(), headers), Object.class);

                updateStatus = updateResponse.getStatusCode();
            } catch (HttpClientErrorException e) {
                updateStatus = e.getStatusCode();
            }
        } while (updateStatus == HttpStatus.PRECONDITION_FAILED);

        if (updateStatus != HttpStatus.NO_CONTENT) {
            throw new UnexpectedApiBehaviourException("Failed to update order status: API responded with " + updateStatus);
        }
    }

}
