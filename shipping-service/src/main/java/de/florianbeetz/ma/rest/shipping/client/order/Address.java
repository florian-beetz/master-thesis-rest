package de.florianbeetz.ma.rest.shipping.client.order;

import javax.validation.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Address {

    @NotEmpty
    private final String street;
    @NotEmpty
    private final String city;
    @NotEmpty
    private final String zip;

    @JsonCreator
    public Address(@JsonProperty("street") String street,
                   @JsonProperty("city") String city,
                   @JsonProperty("zip") String zip) {
        this.street = street;
        this.city = city;
        this.zip = zip;
    }

}

