package de.florianbeetz.ma.rest.shipping;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

public enum ShippingStatus {

    /** shipment is not yet ready to ship */
    CREATED,

    /** shipment is ready to ship */
    READY_TO_SHIP,

    /** shipment is shipped */
    SHIPPED,

    /** shipment is cancelled */
    CANCELLED

    ;

    private static final Map<ShippingStatus, Set<ShippingStatus>> VALID_TRANSITIONS = Map.of(
            CREATED, Set.of(READY_TO_SHIP, CANCELLED),
            READY_TO_SHIP, Set.of(SHIPPED, CANCELLED),
            SHIPPED, Collections.emptySet(),
            CANCELLED, Collections.emptySet()
    );

    public static ShippingStatus from(String value) {
        return Stream.of(ShippingStatus.values())
                .filter(s -> s.name().equalsIgnoreCase(value))
                .findAny()
                .orElse(null);
    }

    public static boolean isValidTransition(ShippingStatus from, ShippingStatus to) {
        Objects.requireNonNull(from);
        Objects.requireNonNull(to);

        return VALID_TRANSITIONS.get(from).contains(to);
    }

}
