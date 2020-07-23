package de.florianbeetz.ma.rest.shipping.data;

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
public class ShipmentEntity {

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private String order;

    @Column(nullable = false)
    private String destinationStreet;

    @Column(nullable = false)
    private String destinationCity;

    @Column(nullable = false)
    private String destinationZip;

}
