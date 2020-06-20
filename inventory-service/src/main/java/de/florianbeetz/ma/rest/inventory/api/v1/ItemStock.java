package de.florianbeetz.ma.rest.inventory.api.v1;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.florianbeetz.ma.rest.inventory.data.ItemStockEntity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.val;
import org.springframework.hateoas.LinkRelation;
import org.springframework.hateoas.RepresentationModel;

import javax.validation.constraints.PositiveOrZero;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

/**
 * Represents a certain amount of {@link Item Items} in a {@link Warehouse}.
 */
@Getter
@EqualsAndHashCode(callSuper = false)
public class ItemStock extends RepresentationModel<ItemStock> {

    /** link relation to this stock's item */
    public static final LinkRelation ITEM_RELATION = LinkRelation.of("item");
    /** link relation to the warehouse this stock is stored in */
    public static final LinkRelation WAREHOUSE_RELATION = LinkRelation.of("warehouse");

    @PositiveOrZero
    private final Long inStock;
    @PositiveOrZero
    private final Long available;


    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private final String warehouse;

    @JsonCreator
    public ItemStock(@JsonProperty("inStock") Long inStock,
                     @JsonProperty("available") Long available,
                     @JsonProperty("warehouse") String warehouse) {
        this.inStock = inStock;
        this.available = available;
        this.warehouse = warehouse;
    }

    public static ItemStock from(ItemStockEntity entity) {
        val stock = new ItemStock(entity.getInStock(), entity.getAvailable(), null);

        stock.add(linkTo(methodOn(ItemStockController.class).getStock(entity.getItem().getId(), entity.getId())).withSelfRel());
        stock.add(linkTo(methodOn(ItemController.class).getItem(entity.getItem().getId())).withRel(ITEM_RELATION));
        stock.add(linkTo(methodOn(WarehouseController.class).getWarehouse(entity.getWarehouse().getId())).withRel(WAREHOUSE_RELATION));

        return stock;
    }
}
