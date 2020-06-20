package de.florianbeetz.ma.rest.inventory.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class ItemStockEntity {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    private ItemEntity item;

    @ManyToOne
    private WarehouseEntity warehouse;

    @Column(nullable = false)
    private long inStock;

    @Column(nullable = false)
    private long available;

}
