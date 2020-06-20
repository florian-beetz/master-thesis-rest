package de.florianbeetz.ma.rest.inventory.data;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemStockRepository extends PagingAndSortingRepository<ItemStockEntity, Long> {

    Page<ItemStockEntity> findAllByItem(ItemEntity item, Pageable page);

}
