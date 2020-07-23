package de.florianbeetz.ma.rest.order.client.shipping;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.springframework.hateoas.RepresentationModel;

@Getter
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = true)
public class Shipment extends RepresentationModel<Shipment> {

    private final ShippingAddress destinationAddress;
    private final String order;
    private final ShippingStatus status;

    public Shipment(ShippingAddress destinationAddress, String order) {
        this(destinationAddress, order, ShippingStatus.CREATED);
    }

    @JsonCreator
    public Shipment(@JsonProperty("destinationAddress") ShippingAddress destinationAddress,
                    @JsonProperty("order") String order,
                    @JsonProperty("status") ShippingStatus status) {
        this.destinationAddress = destinationAddress;
        this.order = order;
        this.status = status;
    }
}
