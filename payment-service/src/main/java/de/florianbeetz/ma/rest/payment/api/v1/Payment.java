package de.florianbeetz.ma.rest.payment.api.v1;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Positive;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.florianbeetz.ma.rest.payment.data.PaymentEntity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.LinkRelation;
import org.springframework.hateoas.RepresentationModel;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Getter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class Payment extends RepresentationModel<Payment> {

    public static final LinkRelation ORDER_RELATION = LinkRelation.of("order");
    public static final LinkRelation STATUS_RELATION = LinkRelation.of("status");

    @Positive
    private final double amount;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final String orderUrl;
    private final String paymentReference;
    @NotEmpty
    private final String status;

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

    public static Payment from(PaymentEntity entity) {
        Payment payment = new Payment(entity.getAmount(), null, entity.getPaymentReference(), entity.getStatus());
        payment.add(linkTo(methodOn(PaymentController.class).getPayment(entity.getId())).withSelfRel());
        payment.add(linkTo(methodOn(PaymentController.class).getPaymentStatus(entity.getId())).withRel(STATUS_RELATION));
        payment.add(new Link(entity.getOrderUrl(), ORDER_RELATION));
        return payment;
    }
}
