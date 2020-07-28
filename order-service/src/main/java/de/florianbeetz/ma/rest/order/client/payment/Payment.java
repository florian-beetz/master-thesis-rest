package de.florianbeetz.ma.rest.order.client.payment;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.springframework.hateoas.RepresentationModel;

@Getter
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = true)
public class Payment extends RepresentationModel<Payment> {

    private final double amount;
    private final String orderUrl;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final String paymentReference;
    private final String status;

    public Payment(double amount, String orderUrl) {
        this(amount, orderUrl, null, PaymentStatus.CREATED.name());
    }

    @JsonCreator
    public Payment(@JsonProperty("amount") double amount,
                   @JsonProperty("orderUrl") String orderUrl,
                   @JsonProperty("paymentReference") String paymentReference,
                   @JsonProperty("status") String status) {
        this.amount = amount;
        this.orderUrl = orderUrl;
        this.paymentReference = paymentReference;
        this.status = status;
    }
}
