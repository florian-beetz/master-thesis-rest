package de.florianbeetz.ma.rest.order.api.v1;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.florianbeetz.ma.rest.order.data.OrderEntity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.val;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.LinkRelation;
import org.springframework.hateoas.RepresentationModel;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Getter
@ToString
@EqualsAndHashCode(callSuper = false)
public class Order extends RepresentationModel<Order> {

    public static final LinkRelation STATUS_RELATION = LinkRelation.of("status");
    public static final LinkRelation SHIPMENT_RELATION = LinkRelation.of("shipment");
    public static final LinkRelation PAYMENT_RELATION = LinkRelation.of("payment");

    @NotEmpty
    private final List<OrderPosition> items;
    @NotEmpty
    private final String status;
    @Valid
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final Address address;
    @Setter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double weight;

    @JsonCreator
    public Order(@JsonProperty("items") List<OrderPosition> items,
                 @JsonProperty("status") String status,
                 @JsonProperty("address") Address address) {
        this.items = items;
        this.status = status;
        this.address = address;
    }


    public static Order from(OrderEntity entity) {
        val order = new Order(OrderPosition.from(entity.getPositions()), entity.getStatus(), null);
        order.add(linkTo(methodOn(OrderController.class).getOrder(entity.getId())).withSelfRel());
        order.add(linkTo(methodOn(OrderController.class).getOrderStatus(entity.getId())).withRel(STATUS_RELATION));
        if (entity.getShipmentUrl() != null) {
            order.add(new Link(entity.getShipmentUrl(), SHIPMENT_RELATION));
        }
        if (entity.getPaymentUrl() != null) {
            order.add(new Link(entity.getPaymentUrl(), PAYMENT_RELATION));
        }
        return order;
    }
}
