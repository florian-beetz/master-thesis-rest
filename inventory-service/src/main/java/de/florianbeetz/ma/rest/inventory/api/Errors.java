package de.florianbeetz.ma.rest.inventory.api;

import org.springframework.http.HttpStatus;

public class Errors {

    /** requested item does not exist */
    public static final ApiError ITEM_NOT_FOUND = new ApiError(HttpStatus.NOT_FOUND, "Item does not exist.");

    /** requested warehouse does not exist */
    public static final ApiError WAREHOUSE_NOT_FOUND = new ApiError(HttpStatus.NOT_FOUND, "Warehouse does not exist.");
    /** user provided URL does not point to a warehouse */
    public static final ApiError WAREHOUSE_URL_INVALID = new ApiError(HttpStatus.BAD_REQUEST, "The provided warehouse URL is not valid.");

    /** requested stock position does not exist */
    public static final ApiError STOCK_POSITION_NOT_FOUND = new ApiError(HttpStatus.NOT_FOUND, "Stock position does not exist.");
    /** stock position is not valid */
    public static final ApiError STOCK_POSITION_INVALID = new ApiError(HttpStatus.BAD_REQUEST, "This stock position does not make sense: more items are available, than there are in stock.");

    /** ETag is required to prevent lost-update problem but was not provided */
    public static final ApiError ETAG_MISSING = new ApiError(HttpStatus.PRECONDITION_REQUIRED, "This request can not be fulfilled unless an ETag is included in the If-Match header. Obtain the current ETag by sending a GET request to the same resource.");
    /** ETag was provided but does not match current state */
    public static final ApiError ETAG_MISMATCH = new ApiError(HttpStatus.PRECONDITION_FAILED, "The ETag included in this request does not match the current one. Resolve this error by obtaining the current ETag by sending a GET request to the same resource and repeat the request with this ETag.");

    private Errors() {}
}
