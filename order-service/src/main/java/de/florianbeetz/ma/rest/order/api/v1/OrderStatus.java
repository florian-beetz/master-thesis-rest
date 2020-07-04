package de.florianbeetz.ma.rest.order.api.v1;

import java.util.stream.Stream;

/**
 * Represents the status of an {@link Order}.
 */
public enum OrderStatus {

    /** order was created by the customer */
    CREATED,
    /** payment for the order was received */
    PAYMENT_RECEIVED,
    /** order is shipped */
    SHIPPED,
    /** order was cancelled */
    CANCELED

    ;

    public static OrderStatus from(String value) {
        return Stream.of(OrderStatus.values())
                .filter(s -> s.name().equalsIgnoreCase(value))
                .findAny()
                .orElse(null);
    }

}
