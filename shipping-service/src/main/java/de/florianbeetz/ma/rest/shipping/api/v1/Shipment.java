package de.florianbeetz.ma.rest.shipping.api.v1;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.florianbeetz.ma.rest.shipping.data.ShipmentEntity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.springframework.hateoas.RepresentationModel;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Getter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = false)
public class Shipment extends RepresentationModel<Shipment> {

    @NotNull
    private final Address destinationAddress;

    @NotEmpty
    private final String order;

    @JsonCreator
    public Shipment(@JsonProperty("destinationAddress") Address destinationAddress,
                    @JsonProperty("order") String order) {
        this.destinationAddress = destinationAddress;
        this.order = order;
    }

    ShipmentEntity toEntity() {
        return new ShipmentEntity(null, order, destinationAddress.getStreet(), destinationAddress.getCity(), destinationAddress.getZip());
    }

    static Shipment from(ShipmentEntity entity) {
        Shipment shipment = new Shipment(new Address(entity.getDestinationStreet(), entity.getDestinationCity(), entity.getDestinationZip()), entity.getOrder());
        shipment.add(linkTo(methodOn(ShipmentController.class).getShipment(entity.getId())).withSelfRel());
        return shipment;
    }
}
