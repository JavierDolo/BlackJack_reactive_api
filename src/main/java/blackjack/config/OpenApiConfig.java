package blackjack.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI blackjackOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Blackjack Reactive API")
                        .description("Reactive Blackjack game built with Spring WebFlux, MongoDB, and MySQL R2DBC.\n\n" +
                                "You can create players, start games and play directly from this interface.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Javier")
                                .email("unmaildejemplo@ejemplo.com")));
    }
}
