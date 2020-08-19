package de.florianbeetz.ma.rest.inventory.api.v1;

import java.util.stream.Collectors;

import javax.validation.Valid;

import de.florianbeetz.ma.rest.inventory.PagingUtil;
import de.florianbeetz.ma.rest.inventory.api.ApiError;
import de.florianbeetz.ma.rest.inventory.api.Errors;
import de.florianbeetz.ma.rest.inventory.data.ItemEntity;
import de.florianbeetz.ma.rest.inventory.data.ItemRepository;
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
@RequestMapping("/api/v1/item")
@SuppressWarnings("squid:S1452")
public class ItemController {

    private final ItemRepository itemRepository;

    public ItemController(@Autowired ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    @Operation(summary = "List all items")
    @ApiResponse(responseCode = "200", description = "Listing of the items", content = {
            @Content(mediaType = "application/hal+json", schema = @Schema(implementation = CollectionModel.class))
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
    @ApiResponse(responseCode = "200", description = "Item found", content = {
            @Content(mediaType = "application/hal+json", schema = @Schema(implementation = Item.class))
    })
    @ApiResponse(responseCode = "404", description = "Item not found", content = {
            @Content(mediaType = "application/hal+json", schema = @Schema(implementation = ApiError.class))
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> getItem(@PathVariable("id") long id) {
        val entity = itemRepository.findById(id);
        if (entity.isEmpty()) {
            return Errors.ITEM_NOT_FOUND.asResponse();
        }
        return ResponseEntity.of(entity.map(Item::from));
    }

    @Secured("ROLE_inventory_admin")
    @Operation(summary = "Create a new item", security = @SecurityRequirement(name = "keycloak"))
    @ApiResponse(responseCode = "201", description = "Item created", content = {
            @Content(mediaType = "application/hal+json", schema = @Schema(implementation = Item.class))
    })
    @PostMapping("/")
    public ResponseEntity<Item> createItem(@Valid @RequestBody Item item) {
        ItemEntity entity = itemRepository.save(item.toEntity());
        return ResponseEntity.created(linkTo(methodOn(ItemController.class).getItem(entity.getId())).toUri())
                .body(Item.from(entity));
    }

}
