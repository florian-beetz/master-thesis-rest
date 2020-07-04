package de.florianbeetz.ma.rest.order.client.inventory;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Signals that the inventory API behaved in some unexpected way.
 */
@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class UnexpectedApiBehaviourException extends RuntimeException {
    public UnexpectedApiBehaviourException() {
    }

    public UnexpectedApiBehaviourException(String message) {
        super(message);
    }

    public UnexpectedApiBehaviourException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnexpectedApiBehaviourException(Throwable cause) {
        super(cause);
    }
}
