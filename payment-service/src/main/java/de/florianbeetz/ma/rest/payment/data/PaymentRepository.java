package de.florianbeetz.ma.rest.payment.data;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

public interface PaymentRepository extends CrudRepository<PaymentEntity, Long> {

    List<PaymentEntity> findAllByStatusAndOrderUpdatedFalse(String status);

}
