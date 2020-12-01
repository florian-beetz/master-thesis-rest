package de.florianbeetz.ma.rest.order.data;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface OrderPositionRepository extends CrudRepository<OrderPositionEntity, Long> {

    @Query("select sum(pos.itemWeight * pos.amount) from OrderPositionEntity pos where pos.order = :order")
    double getOrderWight(OrderEntity order);

}
