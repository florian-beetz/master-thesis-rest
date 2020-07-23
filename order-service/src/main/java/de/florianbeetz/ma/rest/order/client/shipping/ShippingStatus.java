package de.florianbeetz.ma.rest.order.client.shipping;

public enum ShippingStatus {

    /** shipment is not yet ready to ship */
    CREATED,

    /** shipment is ready to ship */
    READY_TO_SHIP,

    /** shipment is shipped */
    SHIPPED,

    /** shipment is cancelled */
    CANCELLED

}
