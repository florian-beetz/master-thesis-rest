package de.florianbeetz.ma.rest.order.api.v1;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.xml.bind.DatatypeConverter;

import de.florianbeetz.ma.rest.order.OrderStatus;
import de.florianbeetz.ma.rest.order.api.ApiError;
import de.florianbeetz.ma.rest.order.api.Errors;
import de.florianbeetz.ma.rest.order.client.inventory.InventoryApi;
import de.florianbeetz.ma.rest.order.client.inventory.Item;
import de.florianbeetz.ma.rest.order.client.payment.Payment;
import de.florianbeetz.ma.rest.order.client.payment.PaymentApi;
import de.florianbeetz.ma.rest.order.client.shipping.Shipment;
import de.florianbeetz.ma.rest.order.client.shipping.ShippingAddress;
import de.florianbeetz.ma.rest.order.client.shipping.ShippingApi;
import de.florianbeetz.ma.rest.order.data.OrderEntity;
import de.florianbeetz.ma.rest.order.data.OrderPositionEntity;
import de.florianbeetz.ma.rest.order.data.OrderPositionRepository;
import de.florianbeetz.ma.rest.order.data.OrderRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Slf4j
@RestController
@RequestMapping("/api/v1/order")
public class OrderController {

    private final InventoryApi inventoryApi;
    private final ShippingApi shippingApi;
    private final PaymentApi paymentApi;
    private final OrderRepository orderRepository;
    private final OrderPositionRepository orderPositionRepository;

    @Autowired
    public OrderController(InventoryApi inventoryApi,
                           ShippingApi shippingApi,
                           PaymentApi paymentApi, OrderRepository orderRepository,
                           OrderPositionRepository orderPositionRepository) {
        this.inventoryApi = inventoryApi;
        this.shippingApi = shippingApi;
        this.paymentApi = paymentApi;
        this.orderRepository = orderRepository;
        this.orderPositionRepository = orderPositionRepository;
    }

    @Operation(summary = "Creates a new order")
    @ApiResponse(responseCode = "201", description = "Order was created", content = {
            @Content(mediaType = "application/hal+json", schema = @Schema(implementation = Order.class))
    })
    @ApiResponse(responseCode = "400", description = "Status is not created", content = {
            @Content(mediaType = "application/hal+json", schema = @Schema(implementation = ApiError.class))
    })
    @ApiResponse(responseCode = "422", description = "Order can not be satisfied because not enough items are in stock", content = {
            @Content(mediaType = "application/hal+json", schema = @Schema(implementation = ApiError.class))
    })
    @PostMapping("/")
    public ResponseEntity<?> createOrder(@RequestBody Order order) {
        // order must be in CREATED state when newly created
        val status = OrderStatus.from(order.getStatus());
        if (status != OrderStatus.CREATED) {
            return Errors.ORDER_STATUS_NOT_CREATED.asResponse();
        }

        // create reservation positions of all items in the order
        List<OrderPositionEntity> positions = new ArrayList<>();
        double total = 0d;
        for (OrderPosition position : order.getItems()) {
            Item item = inventoryApi.getItem(position.getItem());
            log.debug("reserving {} of item {}", position.getAmount(), item);

            val reservationPositions = inventoryApi.reserveStock(item, position.getAmount());
            if (reservationPositions == null) {
                return new ApiError(HttpStatus.UNPROCESSABLE_ENTITY, "Order has no positions.").asResponse();
            }

            for (String url : reservationPositions.keySet()) {
                positions.add(new OrderPositionEntity(null, null, url, reservationPositions.get(url)));
            }

            total += item.getPrice() * position.getAmount();
        }

        // save order
        val entity = new OrderEntity();
        val savedOrder = orderRepository.save(entity);
        positions.forEach(pos -> pos.setOrder(entity));
        val savedPositions = orderPositionRepository.saveAll(positions);
        savedOrder.setPositions(StreamSupport.stream(savedPositions.spliterator(), false).collect(Collectors.toList()));
        log.debug("Saved order: {}", entity);

        // create corresponding shipment
        val shipment = new Shipment(new ShippingAddress(order.getAddress().getStreet(), order.getAddress().getCity(), order.getAddress().getZip()),
                linkTo(methodOn(OrderController.class).getOrder(savedOrder.getId())).toUri().toString());
        val createdShipment = shippingApi.createShipment(shipment);

        // get shipment cost
        val shipmentCost = shippingApi.getShipmentCost(createdShipment);
        total += shipmentCost.getPrice();

        // create corresponding payment
        val payment = new Payment(total, linkTo(methodOn(OrderController.class).getOrder(savedOrder.getId())).toUri().toString());
        val createdPayment = paymentApi.createPayment(payment);

        // update entity with shipment URL
        savedOrder.setShipmentUrl(shippingApi.getShipmentUrl(createdShipment));
        savedOrder.setPaymentUrl(paymentApi.getPaymentUrl(createdPayment));
        orderRepository.save(savedOrder);

        return ResponseEntity.created(linkTo(methodOn(OrderController.class).getOrder(savedOrder.getId())).toUri())
                             .body(Order.from(savedOrder));
    }

    @Operation(summary = "Get an Order by its ID")
    @ApiResponse(responseCode = "200", description = "Order found", content = {
            @Content(mediaType = "application/hal+json", schema = @Schema(implementation = Order.class))
    })
    @ApiResponse(responseCode = "404", description = "Order not found", content = {
            @Content(mediaType = "application/hal+json", schema = @Schema(implementation = ApiError.class))
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> getOrder(@PathVariable("id") long id) {
        val entity = orderRepository.findById(id);
        if (entity.isEmpty()) {
            return Errors.ORDER_NOT_FOUND.asResponse();
        }
        return ResponseEntity.of(entity.map(Order::from));
    }

    @Operation(summary = "Gets the status of an Order by its ID")
    @ApiResponse(responseCode = "200", description = "Status of the order could be returned", content = {
            @Content(mediaType = "text/plain", schema = @Schema(implementation = OrderStatus.class))
    })
    @ApiResponse(responseCode = "404", description = "Order not found", content = {
            @Content(mediaType = "application/hal+json", schema = @Schema(implementation = ApiError.class))
    })
    @GetMapping("/{id}/status")
    public ResponseEntity<?> getOrderStatus(@PathVariable("id") long id) {
        val entity = orderRepository.findById(id);
        if (entity.isEmpty()) {
            return Errors.ORDER_NOT_FOUND.asResponse();
        }

        val orderEntity = entity.get();
        val headers = new HttpHeaders();
        headers.setETag(calculateOrderStatusETag(orderEntity));

        return new ResponseEntity<>(orderEntity.getStatus(), headers, HttpStatus.OK);
    }

    @Operation(summary = "Update the status of an order identified by its ID",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(schema = @Schema(implementation = OrderStatus.class))))
    @ApiResponse(responseCode = "204", description = "Status of the order was updated")
    @ApiResponse(responseCode = "404", description = "Order not found", content = {
            @Content(mediaType = "application/hal+json", schema = @Schema(implementation = ApiError.class))
    })
    @ApiResponse(responseCode = "400", description = "Status of the order can not be updated to this status", content = {
            @Content(mediaType = "application/hal+json", schema = @Schema(implementation = ApiError.class))
    })
    @ApiResponse(responseCode = "428", description = "no ETag provided", content = {
            @Content(mediaType = "application/hal+json", schema = @Schema(implementation = ApiError.class))
    })
    @ApiResponse(responseCode = "412", description = "ETag does not match", content = {
            @Content(mediaType = "application/hal+json", schema = @Schema(implementation = ApiError.class))
    })
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateOrderStatus(@PathVariable("id") long id,
                                               @RequestBody String status,
                                               @RequestHeader(HttpHeaders.IF_MATCH) String etag) {
        if (etag == null) {
            return Errors.ETAG_MISSING.asResponse();
        }

        val orderEntity = orderRepository.findById(id);
        if (orderEntity.isEmpty()) {
            return Errors.ORDER_NOT_FOUND.asResponse();
        }

        val entity = orderEntity.get();
        if (!etag.equals(calculateOrderStatusETag(entity))) {
            return Errors.ETAG_MISMATCH.asResponse();
        }

        val newStatus = OrderStatus.from(status);
        if (newStatus == null) {
            return Errors.ORDER_INVALID_STATUS.asResponse();
        }

        val entityStatus = OrderStatus.from(entity.getStatus());
        if (!OrderStatus.isValidStatusTransition(entityStatus, newStatus)) {
            return Errors.ORDER_INVALID_TRANSITION.asResponse();
        }

        entity.setStatus(newStatus.name());
        orderRepository.save(entity);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    private static String calculateOrderStatusETag(final OrderEntity entity) {
        return calculateEtag("OrderStatus" + entity.getId() + entity.getStatus());
    }

    @SneakyThrows(NoSuchAlgorithmException.class)
    private static String calculateEtag(final String input) {
        final ByteBuffer buf = StandardCharsets.UTF_8.encode(input);
        final MessageDigest digest = MessageDigest.getInstance("SHA1");
        buf.mark();
        digest.update(buf);
        buf.reset();
        return String.format("W/\"%s\"", DatatypeConverter.printHexBinary(digest.digest()));
    }
}
