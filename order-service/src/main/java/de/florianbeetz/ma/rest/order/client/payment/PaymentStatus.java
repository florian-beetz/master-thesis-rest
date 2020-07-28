package de.florianbeetz.ma.rest.order.client.payment;

public enum PaymentStatus {
    /** payment request was created */
    CREATED,
    /** payment was received */
    PAYED,
    /** payment is no longer payable */
    CANCELED
}
