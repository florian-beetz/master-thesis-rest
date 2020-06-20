package de.florianbeetz.ma.rest.inventory.api.v1;

import de.florianbeetz.ma.rest.inventory.PagingUtil;
import de.florianbeetz.ma.rest.inventory.data.ItemEntity;
import de.florianbeetz.ma.rest.inventory.data.ItemRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/v1/item")
public class ItemController {

    private final ItemRepository itemRepository;

    public ItemController(@Autowired ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    @Operation(summary = "List all items")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listing of the items")
    })
    @GetMapping(value = "/")
    public CollectionModel<Item> getItems(@RequestParam(value = "page", defaultValue = "0") Integer page,
                                          @RequestParam(value = "size", defaultValue = "20") Integer size) {
        val itemsPage = itemRepository.findAll(PageRequest.of(page, size));

        val items = itemsPage.get()
                .map(Item::from)
                .collect(Collectors.toList());

        return PagingUtil.getCollection(items, itemsPage, page, size, (p, s) -> methodOn(ItemController.class).getItems(p, s));
    }

    @Operation(summary = "Get an item by its id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Item found"),
            @ApiResponse(responseCode = "404", description = "Item not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Item> getItem(@PathVariable("id") long id) {
        return ResponseEntity.of(itemRepository.findById(id).map(Item::from));
    }

    @Operation(summary = "Create a new item")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Item created")
    })
    @PostMapping("/")
    public ResponseEntity<Item> createItem(@RequestBody Item item) {
        ItemEntity entity = itemRepository.save(item.toEntity());
        return ResponseEntity.created(linkTo(methodOn(ItemController.class).getItem(entity.getId())).toUri())
                .body(Item.from(entity));
    }

}
