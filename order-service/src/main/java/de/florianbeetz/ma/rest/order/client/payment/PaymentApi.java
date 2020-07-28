package de.florianbeetz.ma.rest.order.client.payment;

import de.florianbeetz.ma.rest.order.client.UnexpectedApiBehaviourException;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
public class PaymentApi {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public PaymentApi(@Autowired RestTemplate restTemplate,
                      @Value("${application.api.payment.base-url}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    public Payment getPayment(String url) {
        val response = restTemplate.getForEntity(url, Payment.class);
        return response.getBody();
    }

    public Payment createPayment(Payment payment) {
        String url = baseUrl + "/payment/";
        val response = restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(payment), Payment.class);
        return response.getBody();
    }

    public String getPaymentUrl(Payment createdPayment) {
        return createdPayment.getRequiredLink(IanaLinkRelations.SELF).getHref();
    }

    public void deletePayment(Payment payment) {
        val paymentUrl = payment.getRequiredLink(IanaLinkRelations.SELF).getHref();
        HttpStatus deleteStatus;
        do {
            // request entity for current ETag
            val response = restTemplate.getForEntity(paymentUrl, Payment.class);

            try {
                val headers = new HttpHeaders();
                headers.setIfMatch(response.getHeaders().getETag());
                val deleteResponse = restTemplate.exchange(paymentUrl, HttpMethod.DELETE, new HttpEntity<>(headers), Object.class);

                deleteStatus = deleteResponse.getStatusCode();
            } catch (HttpClientErrorException e) {
                deleteStatus = e.getStatusCode();
            }
        } while (deleteStatus == HttpStatus.PRECONDITION_FAILED);

        if (deleteStatus != HttpStatus.NO_CONTENT) {
            throw new UnexpectedApiBehaviourException("Failed to delete payment: API responded with status " +  deleteStatus);
        }
    }
}
