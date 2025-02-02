package de.florianbeetz.ma.rest.order.api;

import org.springframework.http.HttpStatus;

public class Errors {

    public static final ApiError ORDER_NOT_FOUND = new ApiError(HttpStatus.NOT_FOUND, "Order does not exist.");
    public static final ApiError ORDER_INVALID_STATUS = new ApiError(HttpStatus.BAD_REQUEST, "The status of the order does not have a valid value.");
    public static final ApiError ORDER_INVALID_TRANSITION = new ApiError(HttpStatus.BAD_REQUEST, "The status can not be updated to the given value, because its current status does not allow this transition.");
    public static final ApiError ORDER_STATUS_NOT_CREATED = new ApiError(HttpStatus.BAD_REQUEST, "New orders must be in status 'created'.");

    /** ETag is required to prevent lost-update problem but was not provided */
    public static final ApiError ETAG_MISSING = new ApiError(HttpStatus.PRECONDITION_REQUIRED, "This request can not be fulfilled unless an ETag is included in the If-Match header. Obtain the current ETag by sending a GET request to the same resource.");
    /** ETag was provided but does not match current state */
    public static final ApiError ETAG_MISMATCH = new ApiError(HttpStatus.PRECONDITION_FAILED, "The ETag included in this request does not match the current one. Resolve this error by obtaining the current ETag by sending a GET request to the same resource and repeat the request with this ETag.");

    private Errors() {}
}
