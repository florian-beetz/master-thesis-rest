package de.florianbeetz.ma.rest.payment;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class PaymentApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaymentApplication.class, args);
    }

    @Bean
    public OpenAPI openAPI(@Value("${application.title}") String title,
                           @Value("${application.version}") String version,
                           @Value("${application.description}") String description) {
        return new OpenAPI()
                .info(new Info()
                        .title(title)
                        .version("v" + version)
                        .description(description)
                        .contact(new Contact()
                                .name("Florian Beetz")
                                .email("master-thesis@florianbeetz.de")
                                .url("https://florianbeetz.de")));
    }
}
