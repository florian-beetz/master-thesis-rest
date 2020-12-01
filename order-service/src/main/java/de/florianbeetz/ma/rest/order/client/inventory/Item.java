package de.florianbeetz.ma.rest.order.client.inventory;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.springframework.hateoas.RepresentationModel;

@Getter
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = true)
public class Item extends RepresentationModel<Item> {

    private final String title;
    private final String description;
    private final double price;
    private final double weight;

    public Item(@JsonProperty(value = "title", required = true) String title,
                @JsonProperty("description") String description,
                @JsonProperty(value = "price", required = true) double price,
                @JsonProperty(value = "weight", required = true) double weight) {
        this.title = title;
        this.description = description;
        this.price = price;
        this.weight = weight;
    }
}
