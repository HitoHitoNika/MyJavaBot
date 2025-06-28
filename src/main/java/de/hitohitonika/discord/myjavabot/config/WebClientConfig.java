package de.hitohitonika.discord.myjavabot.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.*;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import reactor.core.publisher.Mono;

@Configuration
public class WebClientConfig {

    private static final String TWITCH_API_BASE_URL = "https://api.twitch.tv/helix";
    private static final String TWITCH_REGISTRATION_ID = "twitch";

    /**
     * Erstellt einen OAuth2-Client-Manager, der die Token in-memory verwaltet.
     * Diese Implementierung ist NICHT vom Servlet-Kontext abhängig.
     *
     * @param clientRegistrationRepository Repository mit den Client-Daten aus der application.yml.
     * @return Ein Manager, der für Hintergrundaufgaben geeignet ist.
     */
    @Bean
    public OAuth2AuthorizedClientManager authorizedClientManager(
            ClientRegistrationRepository clientRegistrationRepository) {

        // 1. Ein In-Memory-Service zum Speichern der abgerufenen Token.
        OAuth2AuthorizedClientService authorizedClientService =
                new InMemoryOAuth2AuthorizedClientService(clientRegistrationRepository);

        // 2. Der Manager, der den Service und das Repository zusammenbringt.
        AuthorizedClientServiceOAuth2AuthorizedClientManager authorizedClientManager =
                new AuthorizedClientServiceOAuth2AuthorizedClientManager(
                        clientRegistrationRepository, authorizedClientService);

        // 3. Der "Provider", der weiß, wie man einen Token per "client_credentials" holt.
        OAuth2AuthorizedClientProvider authorizedClientProvider =
                OAuth2AuthorizedClientProviderBuilder.builder()
                        .clientCredentials()
                        .build();

        authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider);

        return authorizedClientManager;
    }

    /**
     * Erstellt die WebClient-Bean. Diese Version wurde korrigiert und verwendet den
     * blockierenden Aufruf von authorizedClientManager korrekt.
     */
    @Bean
    @Qualifier("twitchWebClient")
    public WebClient twitchWebClient(OAuth2AuthorizedClientManager authorizedClientManager) {

        ExchangeFilterFunction oauth2Filter = (clientRequest, next) -> {
            // Dies ist die Authorisierungsanfrage für den "twitch"-Client
            OAuth2AuthorizeRequest authorizeRequest = OAuth2AuthorizeRequest
                    .withClientRegistrationId(TWITCH_REGISTRATION_ID)
                    .principal("MyDiscordBot") // Ein beliebiger Name, für client_credentials nicht relevant
                    .build();

            // 1. Rufe die blockierende Methode auf. Dies holt den Token synchron.
            OAuth2AuthorizedClient authorizedClient = authorizedClientManager.authorize(authorizeRequest);

            // 2. Prüfe, ob die Authorisierung fehlgeschlagen ist.
            if (authorizedClient == null) {
                // Ein Fehler-Mono zurückgeben, wenn kein Token geholt werden konnte.
                return Mono.error(new IllegalStateException("Twitch-Authentifizierung fehlgeschlagen. Überprüfe Client-ID/Secret."));
            }

            // 3. Hole den Access-Token aus dem authorisierten Client.
            OAuth2AccessToken accessToken = authorizedClient.getAccessToken();

            // 4. Erstelle eine neue Anfrage und füge den 'Authorization'-Header hinzu.
            ClientRequest newRequest = ClientRequest.from(clientRequest)
                    .headers(headers -> headers.setBearerAuth(accessToken.getTokenValue()))
                    .build();

            // 5. Führe die modifizierte Anfrage aus.
            return next.exchange(newRequest);
        };

        return WebClient.builder()
                .baseUrl(TWITCH_API_BASE_URL)
                .filter(oauth2Filter)
                .build();
    }
}