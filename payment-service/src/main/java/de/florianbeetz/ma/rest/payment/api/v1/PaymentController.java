package de.florianbeetz.ma.rest.payment.api.v1;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

import javax.validation.Valid;
import javax.xml.bind.DatatypeConverter;

import de.florianbeetz.ma.rest.payment.PaymentStatus;
import de.florianbeetz.ma.rest.payment.api.ApiError;
import de.florianbeetz.ma.rest.payment.api.Errors;
import de.florianbeetz.ma.rest.payment.data.PaymentEntity;
import de.florianbeetz.ma.rest.payment.data.PaymentRepository;
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
@RequestMapping("/api/v1/payment")
public class PaymentController {

    private final PaymentRepository paymentRepository;

    @Autowired
    public PaymentController(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @Operation(summary = "Creates a new payment")
    @ApiResponse(responseCode = "201", description = "Payment created", content = {
            @Content(mediaType = "application/hal+json", schema = @Schema(implementation = Payment.class))
    })
    @ApiResponse(responseCode = "400", description = "Status is not created", content = {
            @Content(mediaType = "application/hal+json", schema = @Schema(implementation = ApiError.class))
    })
    @PostMapping("/")
    public ResponseEntity<?> createPayment(@Valid @RequestBody Payment payment) {
        // payments must be in status CREATED when newly created
        val status = PaymentStatus.from(payment.getStatus());
        if (status != PaymentStatus.CREATED) {
            return Errors.PAYMENT_STATUS_NOT_CREATED.asResponse();
        }

        val paymentEntity = new PaymentEntity(
                null,
                payment.getAmount(),
                payment.getOrderUrl(),
                UUID.randomUUID().toString(),
                status.name(),
                false
        );
        val savedEntity = paymentRepository.save(paymentEntity);
        return ResponseEntity.created(linkTo(methodOn(PaymentController.class).getPayment(savedEntity.getId())).toUri())
                .body(Payment.from(savedEntity));
    }

    @Operation(summary = "Gets a payment by its ID")
    @ApiResponse(responseCode = "200", description = "Payment found", content = {
            @Content(mediaType = "application/hal+json", schema = @Schema(implementation = Payment.class))
    })
    @ApiResponse(responseCode = "404", description = "Payment not found", content = {
            @Content(mediaType = "application/hal+json", schema = @Schema(implementation = ApiError.class))
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> getPayment(@PathVariable("id") long id) {
        val entity = paymentRepository.findById(id);
        if (entity.isEmpty()) {
            return Errors.PAYMENT_NOT_FOUND.asResponse();
        }

        val headers = new HttpHeaders();
        headers.setETag(calculatePaymentEtag(entity.get()));
        return new ResponseEntity<>(Payment.from(entity.get()), headers, HttpStatus.OK);
    }

    @Operation(summary = "Deletes a payment by its ID")
    @ApiResponse(responseCode = "204", description = "Payment deleted")
    @ApiResponse(responseCode = "403", description = "Payment can no longer be deleted, as it already payed", content = {
            @Content(mediaType = "application/hal+json", schema = @Schema(implementation = ApiError.class))
    })
    @ApiResponse(responseCode = "404", description = "Payment not found", content = {
            @Content(mediaType = "application/hal+json", schema = @Schema(implementation = ApiError.class))
    })
    @ApiResponse(responseCode = "412", description = "ETag does not match", content = {
            @Content(mediaType = "application/hal+json", schema = @Schema(implementation = ApiError.class))
    })
    @ApiResponse(responseCode = "428", description = "no ETag provided", content = {
            @Content(mediaType = "application/hal+json", schema = @Schema(implementation = ApiError.class))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePayment(@PathVariable("id") long id,
                                           @RequestHeader(HttpHeaders.IF_MATCH) String etag) {
        if (etag == null) {
            return Errors.ETAG_MISSING.asResponse();
        }

        val payment = paymentRepository.findById(id);
        if (payment.isEmpty()) {
            return Errors.PAYMENT_NOT_FOUND.asResponse();
        }

        val paymentEntity = payment.get();
        if (!etag.equals(calculatePaymentEtag(paymentEntity))) {
            return Errors.ETAG_MISMATCH.asResponse();
        }

        if (!PaymentStatus.isValidTransition(PaymentStatus.from(paymentEntity.getStatus()), PaymentStatus.CANCELED)) {
            return Errors.PAYMENT_TOO_LATE.asResponse();
        }

        paymentRepository.delete(paymentEntity);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Operation(summary = "Gets the status of a payment by its ID")
    @ApiResponse(responseCode = "200", description = "Status of the payment could be returned", content = {
            @Content(mediaType = "text/plain", schema = @Schema(implementation = PaymentStatus.class))
    })
    @ApiResponse(responseCode = "404", description = "Payment not found", content = {
            @Content(mediaType = "application/hal+json", schema = @Schema(implementation = ApiError.class))
    })
    @GetMapping("/{id}/status")
    public ResponseEntity<?> getPaymentStatus(@PathVariable("id") long id) {
        val entity = paymentRepository.findById(id);
        if (entity.isEmpty()) {
            return Errors.PAYMENT_NOT_FOUND.asResponse();
        }

        val paymentEntity = entity.get();
        val headers = new HttpHeaders();
        headers.setETag(calculatePaymentStatusEtag(paymentEntity));
        return new ResponseEntity<>(Payment.from(paymentEntity).getStatus(), headers, HttpStatus.OK);
    }

    @Operation(summary = "Updates the status of a payment by its ID",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(schema = @Schema(implementation = PaymentStatus.class))))
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
    public ResponseEntity<?> updatePaymentStatus(@PathVariable("id") long id,
                                                 @RequestBody String status,
                                                 @RequestHeader(HttpHeaders.IF_MATCH) String etag) {
        if (etag == null) {
            return Errors.ETAG_MISSING.asResponse();
        }

        val paymentEntity = paymentRepository.findById(id);
        if (paymentEntity.isEmpty()) {
            return Errors.PAYMENT_NOT_FOUND.asResponse();
        }

        val entity = paymentEntity.get();
        if (!etag.equals(calculatePaymentStatusEtag(entity))) {
            return Errors.ETAG_MISMATCH.asResponse();
        }

        val newStatus = PaymentStatus.from(status);
        if (newStatus == null) {
            return Errors.PAYMENT_STATUS_INVALID.asResponse();
        }

        val entityStatus = PaymentStatus.from(entity.getStatus());
        if (!PaymentStatus.isValidTransition(entityStatus, newStatus)) {
            return Errors.PAYMENT_INVALID_TRANSITION.asResponse();
        }

        entity.setStatus(newStatus.name());
        paymentRepository.save(entity);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    private static String calculatePaymentEtag(PaymentEntity entity) {
        return calculateEtag("Payment" + entity.getId() + entity.getOrderUrl() + entity.getPaymentReference() + entity.getAmount() + entity.getStatus());
    }

    private static String calculatePaymentStatusEtag(PaymentEntity entity) {
        return calculateEtag("PaymentStatus" + entity.getId() + entity.getStatus());
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
