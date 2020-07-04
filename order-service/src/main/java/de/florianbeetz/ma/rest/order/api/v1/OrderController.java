package de.florianbeetz.ma.rest.order.api.v1;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import de.florianbeetz.ma.rest.order.client.inventory.InventoryApi;
import de.florianbeetz.ma.rest.order.client.inventory.Item;
import de.florianbeetz.ma.rest.order.data.OrderEntity;
import de.florianbeetz.ma.rest.order.data.OrderPositionEntity;
import de.florianbeetz.ma.rest.order.data.OrderPositionRepository;
import de.florianbeetz.ma.rest.order.data.OrderRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Slf4j
@RestController
@RequestMapping("/api/v1/order")
public class OrderController {

    private final InventoryApi inventoryApi;
    private final OrderRepository orderRepository;
    private final OrderPositionRepository orderPositionRepository;

    @Autowired
    public OrderController(InventoryApi inventoryApi, OrderRepository orderRepository, OrderPositionRepository orderPositionRepository) {
        this.inventoryApi = inventoryApi;
        this.orderRepository = orderRepository;
        this.orderPositionRepository = orderPositionRepository;
    }

    @Operation(summary = "Creates a new order")
    @ApiResponse(responseCode = "201", description = "Order was created")
    @ApiResponse(responseCode = "400", description = "Status is not created")
    @ApiResponse(responseCode = "422", description = "Order can not be satisfied because not enough items are in stock")
    @PostMapping("/")
    public ResponseEntity<Order> createOrder(@RequestBody Order order) {
        // order must be in CREATED state when newly created
        if (order.getStatus() != OrderStatus.CREATED) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        // create reservation positions of all items in the order
        List<OrderPositionEntity> positions = new ArrayList<>();
        for (OrderPosition position : order.getItems()) {
            Item item = inventoryApi.getItem(position.getItem());
            log.debug("reserving {} of item {}", position.getAmount(), item);

            val reservationPositions = inventoryApi.reserveStock(item, position.getAmount());
            if (reservationPositions == null) {
                return new ResponseEntity<>(HttpStatus.UNPROCESSABLE_ENTITY);
            }

            for (String url : reservationPositions.keySet()) {
                positions.add(new OrderPositionEntity(null, null, url, reservationPositions.get(url)));
            }
        }

        // save order
        val entity = new OrderEntity(null, null, OrderStatus.CREATED.name());
        val savedOrder = orderRepository.save(entity);
        positions.forEach(pos -> pos.setOrder(entity));
        val savedPositions = orderPositionRepository.saveAll(positions);
        savedOrder.setPositions(StreamSupport.stream(savedPositions.spliterator(), false).collect(Collectors.toList()));

        log.debug("Saved order: {}", entity);

        return ResponseEntity.created(linkTo(methodOn(OrderController.class).getOrder(savedOrder.getId())).toUri())
                             .body(Order.from(savedOrder));
    }

    @Operation(summary = "Get an Order by its ID")
    @ApiResponse(responseCode = "200", description = "Order found")
    @ApiResponse(responseCode = "404", description = "Order not found")
    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrder(@PathVariable("id") long id) {
        return ResponseEntity.of(orderRepository.findById(id).map(Order::from));
    }
}
