package de.florianbeetz.ma.rest.shipping;

import org.springframework.stereotype.Service;

@Service
public class ShippingCostService {

    public double getDefaultShippingCost() {
        return 4.59;
    }

}
