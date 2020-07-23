package de.florianbeetz.ma.rest.order.api.v1;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import de.florianbeetz.ma.rest.order.OrderStatus;
import de.florianbeetz.ma.rest.order.data.OrderEntity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.val;
import org.springframework.hateoas.LinkRelation;
import org.springframework.hateoas.RepresentationModel;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Getter
@ToString
@EqualsAndHashCode(callSuper = false)
public class Order extends RepresentationModel<Order> {

    public static final LinkRelation STATUS_RELATION = LinkRelation.of("status");

    private final List<OrderPosition> items;
    private final String status;
    // TODO: address, payment info

    public Order(@JsonProperty("items") List<OrderPosition> items,
                 @JsonProperty("status") String status) {
        this.items = items;
        this.status = status;
    }

    public static Order from(OrderEntity entity) {
        val order = new Order(OrderPosition.from(entity.getPositions()), entity.getStatus());
        order.add(linkTo(methodOn(OrderController.class).getOrder(entity.getId())).withSelfRel());
        order.add(linkTo(methodOn(OrderController.class).getOrderStatus(entity.getId())).withRel(STATUS_RELATION));
        return order;
    }
}
