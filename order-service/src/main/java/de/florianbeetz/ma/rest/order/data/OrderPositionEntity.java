package de.florianbeetz.ma.rest.order.data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@Entity
@ToString(exclude = "order")
@NoArgsConstructor
@AllArgsConstructor
public class OrderPositionEntity {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    private OrderEntity order;

    @Column(nullable = false)
    private String itemStock;

    @Column(nullable = false)
    private long amount;
}
