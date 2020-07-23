package de.florianbeetz.ma.rest.order;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Represents the status of an order.
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

    private static final Map<OrderStatus, Set<OrderStatus>> VALID_TRANSITIONS = Map.of(
            CREATED, Set.of(PAYMENT_RECEIVED, CANCELED),
            PAYMENT_RECEIVED, Set.of(SHIPPED, CANCELED),
            SHIPPED, Collections.emptySet(),
            CANCELED, Collections.emptySet()
    );

    public static OrderStatus from(String value) {
        return Stream.of(OrderStatus.values())
                .filter(s -> s.name().equalsIgnoreCase(value))
                .findAny()
                .orElse(null);
    }

    public static boolean isValidStatusTransition(OrderStatus from, OrderStatus to) {
        Objects.requireNonNull(from);
        Objects.requireNonNull(to);

        return VALID_TRANSITIONS.get(from).contains(to);
    }

}
