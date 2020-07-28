package de.florianbeetz.ma.rest.shipping.data;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

public interface ShipmentRepository extends CrudRepository<ShipmentEntity, Long> {

    List<ShipmentEntity> findAllByStatus(String status);

}
