package de.florianbeetz.ma.rest.payment;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

public enum PaymentStatus {

    /** payment request was created */
    CREATED,
    /** payment was received */
    PAYED,
    /** payment is no longer payable */
    CANCELED

    ;

    private static final Map<PaymentStatus, Set<PaymentStatus>> VALID_TRANSITIONS = Map.of(
            CREATED, Set.of(PAYED, CANCELED),
            PAYED, Collections.emptySet(),
            CANCELED, Collections.emptySet()
    );

    public static boolean isValidTransition(PaymentStatus from, PaymentStatus to) {
        Objects.requireNonNull(from);
        Objects.requireNonNull(to);

        return VALID_TRANSITIONS.get(from).contains(to);
    }

    public static PaymentStatus from(String value) {
        return Stream.of(PaymentStatus.values())
                .filter(s -> s.name().equalsIgnoreCase(value))
                .findAny()
                .orElse(null);
    }
}
