package de.florianbeetz.ma.rest.payment.data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class PaymentEntity {

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private double amount;

    @Column(nullable = false)
    private String orderUrl;

    @Column(nullable = false)
    private String paymentReference;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private boolean orderUpdated;

}
