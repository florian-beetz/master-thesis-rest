package de.florianbeetz.ma.rest.inventory.api.v1;

import java.util.stream.Collectors;

import de.florianbeetz.ma.rest.inventory.PagingUtil;
import de.florianbeetz.ma.rest.inventory.data.WarehouseRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/v1/warehouse")
public class WarehouseController {

    private final WarehouseRepository warehouseRepository;

    public WarehouseController(@Autowired WarehouseRepository warehouseRepository) {
        this.warehouseRepository = warehouseRepository;
    }

    @Operation(summary = "List all warehouses")
    @ApiResponse(responseCode = "200", description = "Listing of the warehouses")
    @GetMapping("/")
    public CollectionModel<Warehouse> getWarehouses(@RequestParam(value = "page", defaultValue = "0") int page,
                                                    @RequestParam(value = "size", defaultValue = "20") int size) {
        val warehousePage = warehouseRepository.findAll(PageRequest.of(page, size));

        val warehouses = warehousePage.get()
                .map(Warehouse::from)
                .collect(Collectors.toList());

        return PagingUtil.getCollection(warehouses, warehousePage, page, size, (p, s) -> methodOn(WarehouseController.class).getWarehouses(p, s));
    }

    @Operation(summary = "Get a warehouse by its ID")
    @ApiResponse(responseCode = "200", description = "Warehouse found")
    @ApiResponse(responseCode = "404", description = "Warehouse not found")
    @GetMapping("/{id}")
    public ResponseEntity<Warehouse> getWarehouse(@PathVariable("id") long id) {
        return ResponseEntity.of(warehouseRepository.findById(id).map(Warehouse::from));
    }

    @Operation(summary = "Create a new warehouse")
    @ApiResponse(responseCode = "201", description = "Warehouse created")
    @PostMapping("/")
    public ResponseEntity<Warehouse> createWarehouse(@RequestBody Warehouse warehouse) {
        val entity = warehouseRepository.save(warehouse.toEntity());

        return ResponseEntity.created(linkTo(methodOn(WarehouseController.class).getWarehouse(entity.getId())).toUri())
                .body(Warehouse.from(entity));
    }

}
