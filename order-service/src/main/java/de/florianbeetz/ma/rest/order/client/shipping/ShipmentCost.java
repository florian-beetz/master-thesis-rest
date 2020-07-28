package de.florianbeetz.ma.rest.order.client.shipping;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.springframework.hateoas.RepresentationModel;

@Getter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class ShipmentCost extends RepresentationModel<ShipmentCost> {

    private double price;
    private String shippingOption;

    @JsonCreator
    public ShipmentCost(@JsonProperty("price") double price,
                        @JsonProperty("shippingOption") String shippingOption) {
        this.price = price;
        this.shippingOption = shippingOption;
    }

}