package de.florianbeetz.ma.rest.shipping.data;

import org.springframework.data.repository.CrudRepository;

public interface ShipmentRepository extends CrudRepository<ShipmentEntity, Long> {
}
