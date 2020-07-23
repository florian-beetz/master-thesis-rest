package de.florianbeetz.ma.rest.inventory.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StatusController {

    private final String title;
    private final String version;

    public StatusController(@Value("${application.title}") String title,
                            @Value("${application.version}") String version) {
        this.title = title;
        this.version = version;
    }

    @GetMapping("/api/status")
    public Status getStatus() {
        return new Status(title, version);
    }

    @Data
    public static class Status {
        private final String title;
        private final String version;

        @JsonCreator
        public Status(@JsonProperty("title") String title,
                      @JsonProperty("version") String version) {
            this.title = title;
            this.version = version;
        }
    }
}
