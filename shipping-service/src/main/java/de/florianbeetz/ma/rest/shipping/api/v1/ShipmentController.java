package de.florianbeetz.ma.rest.shipping.api.v1;

import de.florianbeetz.ma.rest.shipping.ShippingCostService;
import de.florianbeetz.ma.rest.shipping.data.ShipmentEntity;
import de.florianbeetz.ma.rest.shipping.data.ShipmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

    @PostMapping("/")
    public ResponseEntity<Shipment> createShipment(@RequestBody Shipment shipment) {
        ShipmentEntity entity = shipmentRepository.save(shipment.toEntity());
        return ResponseEntity.created(linkTo(methodOn(ShipmentController.class).getShipment(entity.getId())).toUri())
                .body(Shipment.from(entity));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Shipment> getShipment(@PathVariable("id") long id) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Shipment> deleteShipment(@PathVariable("id") long id) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @GetMapping("/{id}/cost")
    public ResponseEntity<?> getShippingCost(@PathVariable("id") long id) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }
}
