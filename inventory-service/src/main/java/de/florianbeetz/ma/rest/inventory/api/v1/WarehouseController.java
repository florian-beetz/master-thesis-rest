package de.florianbeetz.ma.rest.inventory.api.v1;

import java.util.stream.Collectors;

import javax.validation.Valid;

import de.florianbeetz.ma.rest.inventory.PagingUtil;
import de.florianbeetz.ma.rest.inventory.api.ApiError;
import de.florianbeetz.ma.rest.inventory.api.Errors;
import de.florianbeetz.ma.rest.inventory.data.WarehouseRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
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
@SuppressWarnings("squid:S1452")
public class WarehouseController {

    private final WarehouseRepository warehouseRepository;

    public WarehouseController(@Autowired WarehouseRepository warehouseRepository) {
        this.warehouseRepository = warehouseRepository;
    }

    @Secured("ROLE_inventory_admin")
    @Operation(summary = "List all warehouses", security = @SecurityRequirement(name = "keycloak"))
    @ApiResponse(responseCode = "200", description = "Listing of the warehouses", content = {
            @Content(mediaType = "application/hal+json", schema = @Schema(implementation = CollectionModel.class))
    })
    @GetMapping("/")
    public CollectionModel<Warehouse> getWarehouses(@RequestParam(value = "page", defaultValue = "0") int page,
                                                    @RequestParam(value = "size", defaultValue = "20") int size) {
        val warehousePage = warehouseRepository.findAll(PageRequest.of(page, size));

        val warehouses = warehousePage.get()
                .map(Warehouse::from)
                .collect(Collectors.toList());

        return PagingUtil.getCollection(warehouses, warehousePage, page, size, (p, s) -> methodOn(WarehouseController.class).getWarehouses(p, s));
    }

    @Secured("ROLE_inventory_admin")
    @Operation(summary = "Get a warehouse by its ID", security = @SecurityRequirement(name = "keycloak"))
    @ApiResponse(responseCode = "200", description = "Warehouse found", content = {
            @Content(mediaType = "application/hal+json", schema = @Schema(implementation = Warehouse.class))
    })
    @ApiResponse(responseCode = "404", description = "Warehouse not found", content = {
            @Content(mediaType = "application/hal+json", schema = @Schema(implementation = ApiError.class))
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> getWarehouse(@PathVariable("id") long id) {
        val entity = warehouseRepository.findById(id);
        if (entity.isEmpty()) {
            return Errors.WAREHOUSE_NOT_FOUND.asResponse();
        }

        return ResponseEntity.of(entity.map(Warehouse::from));
    }

    @Secured("ROLE_inventory_admin")
    @Operation(summary = "Create a new warehouse", security = @SecurityRequirement(name = "keycloak"))
    @ApiResponse(responseCode = "201", description = "Warehouse created", content = {
            @Content(mediaType = "application/hal+json", schema = @Schema(implementation = Warehouse.class))
    })
    @PostMapping("/")
    public ResponseEntity<Warehouse> createWarehouse(@Valid @RequestBody Warehouse warehouse) {
        val entity = warehouseRepository.save(warehouse.toEntity());

        return ResponseEntity.created(linkTo(methodOn(WarehouseController.class).getWarehouse(entity.getId())).toUri())
                .body(Warehouse.from(entity));
    }

}
