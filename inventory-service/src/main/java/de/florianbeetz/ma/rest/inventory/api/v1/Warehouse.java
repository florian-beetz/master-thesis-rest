package de.florianbeetz.ma.rest.inventory.api.v1;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.florianbeetz.ma.rest.inventory.data.WarehouseEntity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.val;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.web.util.UriComponentsBuilder;

import javax.validation.constraints.NotEmpty;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * Represents a warehouse to store {@link Item Items} in.
 */
@Getter
@EqualsAndHashCode(callSuper = true)
public class Warehouse extends RepresentationModel<Warehouse> {

    @NotEmpty
    private final String name;

    @JsonCreator
    public Warehouse(@JsonProperty("name") String name) {
        this.name = name;
    }

    public static Warehouse from(WarehouseEntity entity) {
        Warehouse warehouse = new Warehouse(entity.getName());
        warehouse.add(linkTo(methodOn(WarehouseController.class).getWarehouse(entity.getId())).withSelfRel());
        return warehouse;
    }

    public WarehouseEntity toEntity() {
        return new WarehouseEntity(null, name);
    }

    /**
     * Returns the ID of a {@link Warehouse} based on a given URI, or {@code null} if no ID could be extracted.
     */
    public static Long getIdFromUri(String uri) {
        val components = UriComponentsBuilder.fromUriString(uri).build();
        val segments = components.getPathSegments();

        try {
            return Long.parseLong(segments.get(segments.size() - 1));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
