package de.florianbeetz.ma.rest.order.data;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface OrderRepository extends PagingAndSortingRepository<OrderEntity, Long> {

    List<OrderEntity> findAllByStatus(String status);

    @Query("select entity from OrderEntity entity where entity.status=:status and (entity.shipmentUrl is not null or entity.paymentUrl is not null)")
    List<OrderEntity> findAllByStatusAndHasSubResourceUrls(String status);

    @Query("select entity from OrderEntity entity where entity.status=:status and entity.shipmentUrl is null")
    List<OrderEntity> findAllByStatusWithoutShipmentUrl(String status);

    @Query("select entity from OrderEntity entity where entity.status=:status and entity.shipmentUrl is not null and entity.paymentUrl is null")
    List<OrderEntity> findAllByStatusWithShipmentUrlAndWithoutPaymentUrl(String status);

}
