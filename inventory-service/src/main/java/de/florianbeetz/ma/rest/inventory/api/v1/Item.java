package de.florianbeetz.ma.rest.inventory.api.v1;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.florianbeetz.ma.rest.inventory.data.ItemEntity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.hateoas.LinkRelation;
import org.springframework.hateoas.RepresentationModel;

import javax.validation.constraints.*;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * Represents a type of item for sale.
 *
 * The quantity of items actually in stock is modelled with {@link ItemStock}.
 */
@Getter
@EqualsAndHashCode(callSuper = true)
public class Item extends RepresentationModel<Item> {

    /** link relation to this item's stock */
    public static final LinkRelation STOCK_RELATION = LinkRelation.of("stock");

    @NotEmpty
    private final String title;
    private final String description;
    @Positive
    private final double price;

    @JsonCreator
    public Item(@JsonProperty("title") String title,
                @JsonProperty("description") String description,
                @JsonProperty("price") double price) {
        this.title = title;
        this.description = description;
        this.price = price;
    }

    public static Item from(ItemEntity entity) {
        Item item = new Item(entity.getTitle(), entity.getDescription(), entity.getPrice());
        item.add(linkTo(methodOn(ItemController.class).getItem(entity.getId())).withSelfRel());
        item.add(linkTo(methodOn(ItemStockController.class).getStockOfItem(entity.getId(), 0, 20)).withRel(STOCK_RELATION));
        return item;
    }

    public ItemEntity toEntity() {
        return new ItemEntity(null, title, description, price);
    }
}
