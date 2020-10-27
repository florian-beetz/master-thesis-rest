package de.florianbeetz.ma.rest.order.data;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import de.florianbeetz.ma.rest.order.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@Entity
@AllArgsConstructor
public class OrderEntity {

    @Id
    @GeneratedValue
    private Long id;

    @OneToMany(mappedBy = "order", orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<OrderPositionEntity> positions = new ArrayList<>();

    @Column(nullable = false)
    private String status;

    private String shipmentUrl;
    private String paymentUrl;

    private String deliveryStreet;
    private String deliveryCity;
    private String deliveryZip;

    @Column(nullable = false)
    private boolean itemsBookedOut;

    public OrderEntity() {}

    public OrderEntity(String deliveryStreet, String deliveryCity, String deliveryZip) {
        this(null, new ArrayList<>(), OrderStatus.CREATED.name(), null, null, deliveryStreet, deliveryCity, deliveryZip, false);
    }
}
