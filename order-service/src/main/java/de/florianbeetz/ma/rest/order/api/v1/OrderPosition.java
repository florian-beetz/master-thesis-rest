package de.florianbeetz.ma.rest.order.api.v1;

import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.florianbeetz.ma.rest.order.data.OrderPositionEntity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.springframework.hateoas.LinkRelation;
import org.springframework.hateoas.RepresentationModel;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Getter
@ToString
@EqualsAndHashCode(callSuper = false)
public class OrderPosition extends RepresentationModel<OrderPosition> {

    private static final LinkRelation ORDER_RELATION = LinkRelation.of("order");

    private final String item;
    private final long amount;

    @JsonCreator
    public OrderPosition(@JsonProperty("item") String item,
                         @JsonProperty("amount") long amount) {
        this.item = item;
        this.amount = amount;
    }

    public static OrderPosition from(OrderPositionEntity entity) {
        OrderPosition position = new OrderPosition(entity.getItemStock(), entity.getAmount());
        position.add(linkTo(methodOn(OrderController.class).getOrder(entity.getOrder().getId())).withRel(ORDER_RELATION));
        return position;
    }

    public static List<OrderPosition> from(List<OrderPositionEntity> entities) {
        return entities.stream()
                       .map(OrderPosition::from)
                       .collect(Collectors.toList());
    }
}
