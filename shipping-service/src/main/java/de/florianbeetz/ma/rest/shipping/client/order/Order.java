package de.florianbeetz.ma.rest.shipping.client.order;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.springframework.hateoas.RepresentationModel;

@Getter
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = true)
public class Order extends RepresentationModel<Order> {

    @NotEmpty
    private final List<OrderPosition> items;
    @NotEmpty
    private final String status;
    @Valid
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final Address address;

    // TODO: address, payment info

    @JsonCreator
    public Order(@JsonProperty("items") List<OrderPosition> items,
                 @JsonProperty("status") String status,
                 @JsonProperty("address") Address address) {
        this.items = items;
        this.status = status;
        this.address = address;
    }

}
