package de.florianbeetz.ma.rest.order.data;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface OrderRepository extends PagingAndSortingRepository<OrderEntity, Long> {

    List<OrderEntity> findAllByStatus(String status);

    @Query("select entity from OrderEntity entity where entity.status=:status and (entity.shipmentUrl is not null or entity.paymentUrl is not null)")
    List<OrderEntity> findAllByStatusAndHasSubResourceUrls(String status);

}
