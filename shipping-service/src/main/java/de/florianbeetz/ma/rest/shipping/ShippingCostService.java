package de.florianbeetz.ma.rest.shipping;

import lombok.val;
import de.florianbeetz.ma.rest.shipping.client.order.OrderApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ShippingCostService {

    public static final double COST_LARGE = 14.99;
    public static final double COST_MEDIUM = 5.49;
    public static final double COST_SMALL = 3.49;

    private final OrderApi orderApi;

    @Autowired
    public ShippingCostService(OrderApi orderApi) {
        this.orderApi = orderApi;
    }

    public double getShippingCost(String orderUrl) {
        val order = orderApi.getOrder(orderUrl);

        if (order.getWeight() > 20) {
            return COST_LARGE;
        }
        if (order.getWeight() > 5) {
            return COST_MEDIUM;
        }
        return COST_SMALL;
    }

    public double getDefaultShippingCost() {
        return 4.59; // NOSONAR
    }

}
