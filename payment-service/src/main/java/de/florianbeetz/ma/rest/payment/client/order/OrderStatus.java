package de.florianbeetz.ma.rest.payment.client.order;

public enum OrderStatus {

    /** order was created by the customer */
    CREATED,
    /** payment for the order was received */
    PAYMENT_RECEIVED,
    /** order is shipped */
    SHIPPED,
    /** order was cancelled */
    CANCELED

}
