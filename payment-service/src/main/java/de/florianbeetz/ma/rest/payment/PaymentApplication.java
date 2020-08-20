package de.florianbeetz.ma.rest.payment;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.oauth2.client.DefaultOAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.token.grant.client.ClientCredentialsResourceDetails;

@EnableScheduling
@SpringBootApplication
public class PaymentApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaymentApplication.class, args);
    }

    @Bean
    public OAuth2RestTemplate restTemplate(@Value("${keycloak.auth-server-url}") String baseUrl,
                                           @Value("${keycloak.realm}") String realm,
                                           @Value("${keycloak.resource}") String clientId,
                                           @Value("${keycloak.credentials.secret}") String clientSecret) {
        ClientCredentialsResourceDetails resourceDetails = new ClientCredentialsResourceDetails();
        resourceDetails.setAccessTokenUri(String.format("%s/realms/%s/protocol/openid-connect/token", baseUrl, realm));
        resourceDetails.setClientId(clientId);
        resourceDetails.setClientSecret(clientSecret);
        resourceDetails.setGrantType("client_credentials");

        DefaultOAuth2ClientContext clientContext = new DefaultOAuth2ClientContext();
        return new OAuth2RestTemplate(resourceDetails, clientContext);
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
