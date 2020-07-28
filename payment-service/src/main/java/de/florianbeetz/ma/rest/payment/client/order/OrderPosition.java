package de.florianbeetz.ma.rest.payment.client.order;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class OrderPosition {

    private final String item;
    private final long amount;

    @JsonCreator
    public OrderPosition(@JsonProperty("item") String item,
                         @JsonProperty("amount") long amount) {
        this.item = item;
        this.amount = amount;
    }
}
