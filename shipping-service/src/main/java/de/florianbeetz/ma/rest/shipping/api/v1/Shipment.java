package de.florianbeetz.ma.rest.shipping.api.v1;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.florianbeetz.ma.rest.shipping.data.ShipmentEntity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.LinkRelation;
import org.springframework.hateoas.RepresentationModel;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Getter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = false)
public class Shipment extends RepresentationModel<Shipment> {

    public static final LinkRelation ORDER_RELATION = LinkRelation.of("order");

    @NotNull
    private final Address destinationAddress;

    @NotEmpty
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final String order;

    @NotEmpty
    private final String status;

    @JsonCreator
    public Shipment(@JsonProperty("destinationAddress") Address destinationAddress,
                    @JsonProperty("order") String order,
                    @JsonProperty("status") String status) {
        this.destinationAddress = destinationAddress;
        this.order = order;
        this.status = status;
    }

    static Shipment from(ShipmentEntity entity) {
        Shipment shipment = new Shipment(new Address(entity.getDestinationStreet(), entity.getDestinationCity(), entity.getDestinationZip()), null, entity.getStatus());
        shipment.add(linkTo(methodOn(ShipmentController.class).getShipment(entity.getId())).withSelfRel());
        shipment.add(new Link(entity.getOrderUrl(), ORDER_RELATION));
        return shipment;
    }
}
