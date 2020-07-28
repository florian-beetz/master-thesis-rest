package de.florianbeetz.ma.rest.order.api.v1;

import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.florianbeetz.ma.rest.order.data.OrderPositionEntity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode(callSuper = false)
public class OrderPosition {

    private final String item;
    private final long amount;

    @JsonCreator
    public OrderPosition(@JsonProperty("item") String item,
                         @JsonProperty("amount") long amount) {
        this.item = item;
        this.amount = amount;
    }

    public static OrderPosition from(OrderPositionEntity entity) {
        return new OrderPosition(entity.getItemStock(), entity.getAmount());
    }

    public static List<OrderPosition> from(List<OrderPositionEntity> entities) {
        return entities.stream()
                       .map(OrderPosition::from)
                       .collect(Collectors.toList());
    }
}
