package de.florianbeetz.ma.rest.order.client.shipping;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = true)
public class ShippingAddress {

    private final String street;
    private final String city;
    private final String zip;

    @JsonCreator
    public ShippingAddress(@JsonProperty("street") String street,
                           @JsonProperty("city") String city,
                           @JsonProperty("zip") String zip) {
        this.street = street;
        this.city = city;
        this.zip = zip;
    }
}
