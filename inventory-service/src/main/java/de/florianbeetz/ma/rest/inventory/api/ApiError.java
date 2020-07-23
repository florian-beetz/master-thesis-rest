package de.florianbeetz.ma.rest.inventory.api;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Represents errors to the user.
 */
@Getter
@EqualsAndHashCode(callSuper = true)
public class ApiError extends RepresentationModel<ApiError> {

    @JsonIgnore
    private final HttpStatus status;
    @JsonProperty
    private final int httpCode;
    @JsonProperty
    private final String message;
    @JsonProperty
    private final String description;

    public ApiError(HttpStatus status, String description) {
        this.status = status;
        this.httpCode = status.value();
        this.message = status.getReasonPhrase();
        this.description = description;
    }

    public ResponseEntity<ApiError> asResponse() {
        return new ResponseEntity<>(this, this.status);
    }
}
