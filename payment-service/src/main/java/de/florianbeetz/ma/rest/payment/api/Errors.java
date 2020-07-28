package de.florianbeetz.ma.rest.payment.api;

import org.springframework.http.HttpStatus;

public class Errors {

    public static final ApiError PAYMENT_NOT_FOUND = new ApiError(HttpStatus.NOT_FOUND, "Payment does not exist.");
    public static final ApiError PAYMENT_STATUS_NOT_CREATED = new ApiError(HttpStatus.BAD_REQUEST, "New payments must be in status 'created'.");
    public static final ApiError PAYMENT_STATUS_INVALID = new ApiError(HttpStatus.BAD_REQUEST, "The status of the payment is invalid.");
    public static final ApiError PAYMENT_INVALID_TRANSITION = new ApiError(HttpStatus.BAD_REQUEST, "The status can not be updated to the given value, because its current status does not allow this transition.");
    public static final ApiError PAYMENT_TOO_LATE = new ApiError(HttpStatus.FORBIDDEN, "The payment can not be deleted, because it has already been payed.");

    /** ETag is required to prevent lost-update problem but was not provided */
    public static final ApiError ETAG_MISSING = new ApiError(HttpStatus.PRECONDITION_REQUIRED, "This request can not be fulfilled unless an ETag is included in the If-Match header. Obtain the current ETag by sending a GET request to the same resource.");
    /** ETag was provided but does not match current state */
    public static final ApiError ETAG_MISMATCH = new ApiError(HttpStatus.PRECONDITION_FAILED, "The ETag included in this request does not match the current one. Resolve this error by obtaining the current ETag by sending a GET request to the same resource and repeat the request with this ETag.");

    private Errors() {}
}
