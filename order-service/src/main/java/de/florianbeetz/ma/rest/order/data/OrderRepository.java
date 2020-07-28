package de.florianbeetz.ma.rest.order.data;

import java.util.List;

import org.springframework.data.repository.PagingAndSortingRepository;

public interface OrderRepository extends PagingAndSortingRepository<OrderEntity, Long> {

    List<OrderEntity> findAllByStatus(String status);

    List<OrderEntity> findAllByStatusAndShipmentUrlNotNull(String status);

}
