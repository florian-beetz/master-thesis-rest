package de.florianbeetz.ma.rest.shipping.api.v1;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.validation.Valid;
import javax.xml.bind.DatatypeConverter;

import de.florianbeetz.ma.rest.shipping.ShippingCostService;
import de.florianbeetz.ma.rest.shipping.ShippingStatus;
import de.florianbeetz.ma.rest.shipping.api.ApiError;
import de.florianbeetz.ma.rest.shipping.api.Errors;
import de.florianbeetz.ma.rest.shipping.data.ShipmentEntity;
import de.florianbeetz.ma.rest.shipping.data.ShipmentRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.SneakyThrows;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
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
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/v1/shipment")
public class ShipmentController {

    private final ShippingCostService shippingCostService;
    private final ShipmentRepository shipmentRepository;

    @Autowired
    public ShipmentController(ShippingCostService shippingCostService, ShipmentRepository shipmentRepository) {
        this.shippingCostService = shippingCostService;
        this.shipmentRepository = shipmentRepository;
    }

    @Operation(description = "Creates a new shipment")
    @ApiResponse(responseCode = "201", description = "Shipment was created", content = {
            @Content(mediaType = "application/hal+json", schema = @Schema(implementation = Shipment.class))
    })
    @ApiResponse(responseCode = "400", description = "Status is not created", content = {
            @Content(mediaType = "application/hal+json", schema = @Schema(implementation = ApiError.class))
    })
    @PostMapping("/")
    public ResponseEntity<?> createShipment(@Valid @RequestBody Shipment shipment) {
        // shipment must be in CREATED state when newly created
        val status = ShippingStatus.from(shipment.getStatus());
        if (status != ShippingStatus.CREATED) {
            return Errors.SHIPMENT_STATUS_NOT_CREATED.asResponse();
        }

        val entity = new ShipmentEntity(null,
                shipment.getOrder(),
                shipment.getDestinationAddress().getStreet(),
                shipment.getDestinationAddress().getCity(),
                shipment.getDestinationAddress().getZip(),
                status.name());
        val savedEntity = shipmentRepository.save(entity);
        return ResponseEntity.created(linkTo(methodOn(ShipmentController.class).getShipment(savedEntity.getId())).toUri())
                .body(Shipment.from(savedEntity));
    }

    @Operation(summary = "Get a shipment by its ID")
    @ApiResponse(responseCode = "200", description = "Shipment found", content = {
            @Content(mediaType = "application/hal+json", schema = @Schema(implementation = Shipment.class))
    })
    @ApiResponse(responseCode = "404", description = "Shipment not found", content = {
            @Content(mediaType = "application/hal+json", schema = @Schema(implementation = ApiError.class))
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> getShipment(@PathVariable("id") long id) {
        val entity = shipmentRepository.findById(id);
        if (entity.isEmpty()) {
            return Errors.SHIPMENT_NOT_FOUND.asResponse();
        }

        return ResponseEntity.of(entity.map(Shipment::from));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Shipment> deleteShipment(@PathVariable("id") long id) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @GetMapping("/{id}/cost")
    public ResponseEntity<?> getShippingCost(@PathVariable("id") long id) {
        if (!shipmentRepository.existsById(id)) {
            return Errors.SHIPMENT_NOT_FOUND.asResponse();
        }

        val cost = new ShipmentCost(shippingCostService.getDefaultShippingCost(), "default");
        cost.add(linkTo(methodOn(ShipmentController.class).getShippingCost(id)).withSelfRel());
        cost.add(linkTo(methodOn(ShipmentController.class).getShipment(id)).withRel("shipment"));
        cost.add(linkTo(methodOn(ShipmentController.class).getShippingStatus(id)).withRel("status"));

        return new ResponseEntity<>(cost, HttpStatus.OK);
    }

    @Operation(summary = "Get the status of a shipment by its ID")
    @ApiResponse(responseCode = "200", description = "Status of the shipment could be returned", content = {
            @Content(mediaType = "text/plain", schema = @Schema(implementation = ShippingStatus.class))
    })
    @ApiResponse(responseCode = "404", description = "Shipment not found", content = {
            @Content(mediaType = "application/hal+json", schema = @Schema(implementation = ApiError.class))
    })
    @GetMapping(value = "/{id}/status")
    public ResponseEntity<?> getShippingStatus(@PathVariable("id") long id) {
        val entity = shipmentRepository.findById(id);
        if (entity.isEmpty()) {
            return Errors.SHIPMENT_NOT_FOUND.asResponse();
        }

        val shipmentEntity = entity.get();
        val headers = new HttpHeaders();
        headers.setETag(calculateShipmentStatusETag(shipmentEntity));
        return new ResponseEntity<>(Shipment.from(shipmentEntity).getStatus(), headers, HttpStatus.OK);
    }

    @Operation(summary = "Update the status of a shipment identified by its ID",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(schema = @Schema(implementation = ShippingStatus.class))))
    @ApiResponse(responseCode = "204", description = "Status of the shipment was updated")
    @ApiResponse(responseCode = "400", description = "Status of the shipment can not be updated to this status", content = {
            @Content(mediaType = "application/hal+json", schema = @Schema(implementation = ApiError.class))
    })
    @ApiResponse(responseCode = "404", description = "Shipment not found", content = {
            @Content(mediaType = "application/hal+json", schema = @Schema(implementation = ApiError.class))
    })
    @ApiResponse(responseCode = "412", description = "ETag does not match", content = {
            @Content(mediaType = "application/hal+json", schema = @Schema(implementation = ApiError.class))
    })
    @ApiResponse(responseCode = "428", description = "no ETag provided", content = {
            @Content(mediaType = "application/hal+json", schema = @Schema(implementation = ApiError.class))
    })
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateShipmentStatus(@PathVariable("id") long id,
                                                  @RequestBody String status,
                                                  @RequestHeader(HttpHeaders.IF_MATCH) String etag) {
        if (etag == null) {
            return Errors.ETAG_MISSING.asResponse();
        }

        val shipmentEntity = shipmentRepository.findById(id);
        if (shipmentEntity.isEmpty()) {
            return Errors.SHIPMENT_NOT_FOUND.asResponse();
        }

        val entity = shipmentEntity.get();
        if (etag.equals(calculateShipmentStatusETag(entity))) {
            return Errors.ETAG_MISMATCH.asResponse();
        }

        val newStatus = ShippingStatus.from(status);
        if (newStatus == null) {
            return Errors.SHIPMENT_STATUS_INVALID.asResponse();
        }

        val entityStatus = ShippingStatus.from(entity.getStatus());
        if (!ShippingStatus.isValidTransition(entityStatus, newStatus)) {
            return Errors.SHIPMENT_INVALID_TRANSITION.asResponse();
        }

        entity.setStatus(newStatus.name());
        shipmentRepository.save(entity);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    private static String calculateShipmentStatusETag(final ShipmentEntity entity) {
        return calculateEtag("ShipmentEntity" + entity.getId() + entity.getStatus());
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
