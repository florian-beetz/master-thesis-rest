package de.florianbeetz.ma.rest.shipping.client.inventory;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.springframework.hateoas.RepresentationModel;

@Getter
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = true)
public class ItemStock extends RepresentationModel<ItemStock> {

    private final long inStock;
    private final long available;

    @JsonCreator
    public ItemStock(@JsonProperty("inStock") long inStock,
                     @JsonProperty("available") long available) {
        this.inStock = inStock;
        this.available = available;
    }
}
