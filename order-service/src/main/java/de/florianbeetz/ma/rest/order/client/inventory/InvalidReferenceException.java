package de.florianbeetz.ma.rest.order.client.inventory;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Signals that the response to a request to a provided URL did not match the expected format.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidReferenceException extends RuntimeException {

    public InvalidReferenceException() {
    }

    public InvalidReferenceException(String message) {
        super(message);
    }

    public InvalidReferenceException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidReferenceException(Throwable cause) {
        super(cause);
    }
}
