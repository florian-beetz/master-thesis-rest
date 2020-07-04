package de.florianbeetz.ma.rest.inventory.api.v1;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.stream.Collectors;

import javax.xml.bind.DatatypeConverter;

import de.florianbeetz.ma.rest.inventory.PagingUtil;
import de.florianbeetz.ma.rest.inventory.data.ItemRepository;
import de.florianbeetz.ma.rest.inventory.data.ItemStockEntity;
import de.florianbeetz.ma.rest.inventory.data.ItemStockRepository;
import de.florianbeetz.ma.rest.inventory.data.WarehouseRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.SneakyThrows;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/v1/item")
public class ItemStockController {

    private final ItemRepository itemRepository;
    private final ItemStockRepository itemStockRepository;
    private final WarehouseRepository warehouseRepository;

    @Autowired
    public ItemStockController(ItemRepository itemRepository, ItemStockRepository itemStockRepository, WarehouseRepository warehouseRepository) {
        this.itemRepository = itemRepository;
        this.itemStockRepository = itemStockRepository;
        this.warehouseRepository = warehouseRepository;
    }

    @Operation(summary = "List stock of item")
    @ApiResponse(responseCode = "200", description = "Listing of the stock")
    @ApiResponse(responseCode = "404", description = "Item does not exist")
    @GetMapping("/{id}/stock/")
    public ResponseEntity<CollectionModel<ItemStock>> getStockOfItem(@PathVariable("id") long itemId,
                                                                     @RequestParam(value = "page", defaultValue = "0") int page,
                                                                     @RequestParam(value = "size", defaultValue = "20") int size) {
        val itemEntity = itemRepository.findById(itemId);
        if (itemEntity.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        val itemStockPage = itemStockRepository.findAllByItem(itemEntity.get(), PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "available")));
        val itemStock = itemStockPage.get()
                .map(ItemStock::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(PagingUtil.getCollection(itemStock, itemStockPage, page, size, (p, s) -> methodOn(ItemStockController.class).getStockOfItem(itemId, p, s)));
    }

    @Operation(summary = "Get stock by its ID")
    @ApiResponse(responseCode = "200", description = "Stock found")
    @ApiResponse(responseCode = "404", description = "Stock not found")
    @GetMapping("/{itemId}/stock/{stockId}")
    public ResponseEntity<ItemStock> getStock(@PathVariable("itemId") long itemId,
                                              @PathVariable("stockId") long stockId) {
        val stock = itemStockRepository.findById(stockId);

        if (stock.isPresent()) {
            val headers = new HttpHeaders();
            headers.setETag(calculateEtag(stock.get().toString()));
            return new ResponseEntity<>(ItemStock.from(stock.get()), headers, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @Operation(summary = "Create a new stock position")
            @ApiResponse(responseCode = "201", description = "Stock position created")
            @ApiResponse(responseCode = "400", description = "Invalid request (warehouse invalid or available > inStock)")
            @ApiResponse(responseCode = "404", description = "Item or warehouse does not exist")
    @PostMapping("/{itemId}/stock/")
    public ResponseEntity<ItemStock> createStock(@PathVariable("itemId") long itemId,
                                                 @RequestBody ItemStock stock) {
        val id = Warehouse.getIdFromUri(stock.getWarehouse());
        if (id == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        val warehouseEntity = warehouseRepository.findById(id);
        val itemEntity = itemRepository.findById(itemId);

        if (warehouseEntity.isEmpty() || itemEntity.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        // if stock is created without available items, use the items in stock
        var available = stock.getAvailable();
        if (available == null) {
            available = stock.getInStock();
        }

        // if no in stock was passed, or more is available than in stock, that request does not make sense
        if (stock.getInStock() == null || stock.getInStock() < available) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        ItemStockEntity entity = new ItemStockEntity(null, itemEntity.get(), warehouseEntity.get(), stock.getInStock(), available);
        entity = itemStockRepository.save(entity);

        return ResponseEntity.created(linkTo(methodOn(ItemStockController.class).getStock(itemId, entity.getId())).toUri())
                .body(ItemStock.from(entity));
    }

    @Operation(summary = "Update a stock position")
    @ApiResponse(responseCode = "200", description = "stock position updated",
            content = {
                    @Content(mediaType = "application/hal+json",
                            schema = @Schema(implementation = ItemStock.class))
            })
    @ApiResponse(responseCode = "400", description = "invalid request: available > inStock")
    @ApiResponse(responseCode = "404", description = "item not found")
    @ApiResponse(responseCode = "428", description = "no ETag provided")
    @ApiResponse(responseCode = "412", description = "ETag does not match")
    @PutMapping("/{itemId}/stock/{stockId}")
    public ResponseEntity<ItemStock> updateStock(@RequestBody ItemStock stock,
                                                 @PathVariable("itemId") long itemId,
                                                 @PathVariable("stockId") long stockId,
                                                 @RequestHeader(HttpHeaders.IF_MATCH) String etag) {
        if (etag == null) {
            return new ResponseEntity<>(HttpStatus.PRECONDITION_REQUIRED);
        }

        val entity = itemStockRepository.findById(stockId);
        if (entity.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        var itemStock = entity.get();

        if (!etag.equals(calculateEtag(itemStock.toString()))) {
            return new ResponseEntity<>(HttpStatus.PRECONDITION_FAILED);
        }

        if (stock.getAvailable() < 0 || stock.getInStock() < 0 || stock.getAvailable() > stock.getInStock()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        itemStock.setInStock(stock.getInStock());
        itemStock.setAvailable(stock.getAvailable());

        itemStock = itemStockRepository.save(itemStock);

        val headers = new HttpHeaders();
        headers.setETag(calculateEtag(itemStock.toString()));
        return new ResponseEntity<>(ItemStock.from(itemStock), headers, HttpStatus.OK);
    }

    @Operation(summary = "Deletes a stock position")
    @ApiResponse(responseCode = "200", description = "stock position deleted")
    @ApiResponse(responseCode = "404", description = "stock position not found")
    @DeleteMapping("/{itemId}/stock/{stockId}")
    public ResponseEntity<Object> deleteStock(@PathVariable("itemId") long itemId,
                                         @PathVariable("stockId") long stockId) {
        val stock = itemStockRepository.findById(stockId);

        if (stock.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        itemStockRepository.delete(stock.get());
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @SneakyThrows(NoSuchAlgorithmException.class)
    public static String calculateEtag(final String input) {
        final ByteBuffer buf = StandardCharsets.UTF_8.encode(input);
        final MessageDigest digest = MessageDigest.getInstance("SHA1");
        buf.mark();
        digest.update(buf);
        buf.reset();
        return String.format("W/\"%s\"", DatatypeConverter.printHexBinary(digest.digest()));
    }
}
