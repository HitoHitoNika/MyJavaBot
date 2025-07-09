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

    @Bean
    public OAuth2AuthorizedClientManager authorizedClientManager(
            ClientRegistrationRepository clientRegistrationRepository) {

        OAuth2AuthorizedClientService authorizedClientService =
                new InMemoryOAuth2AuthorizedClientService(clientRegistrationRepository);

        AuthorizedClientServiceOAuth2AuthorizedClientManager authorizedClientManager =
                new AuthorizedClientServiceOAuth2AuthorizedClientManager(
                        clientRegistrationRepository, authorizedClientService);

        OAuth2AuthorizedClientProvider authorizedClientProvider =
                OAuth2AuthorizedClientProviderBuilder.builder()
                        .clientCredentials()
                        .build();

        authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider);

        return authorizedClientManager;
    }

    /**
     * Erstellt die WebClient-Bean, die vorkonfiguriert ist um die Twitch-Calls zu machen.
     */
    @Bean
    @Qualifier("twitchWebClient")
    public WebClient twitchWebClient(OAuth2AuthorizedClientManager authorizedClientManager) {

        //Konfiguration für die Anfragen mit dem WebClient. Hier wird sichergestellt das die OAuth2-Authorisierung valide durch läuft, und der Token per Auth Header als Bearer mitgegeben wird.
        ExchangeFilterFunction oauth2Filter = (clientRequest, next) -> {
            OAuth2AuthorizeRequest authorizeRequest = OAuth2AuthorizeRequest
                    .withClientRegistrationId(TWITCH_REGISTRATION_ID)
                    .principal("MyDiscordBot")
                    .build();

            OAuth2AuthorizedClient authorizedClient = authorizedClientManager.authorize(authorizeRequest);

            if (authorizedClient == null) {
                return Mono.error(new IllegalStateException("Twitch-Authentifizierung fehlgeschlagen. Überprüfe Client-ID/Secret."));
            }

            OAuth2AccessToken accessToken = authorizedClient.getAccessToken();

            ClientRequest newRequest = ClientRequest.from(clientRequest)
                    .headers(headers -> headers.setBearerAuth(accessToken.getTokenValue()))
                    .build();

            return next.exchange(newRequest);
        };

        return WebClient.builder()
                .baseUrl(TWITCH_API_BASE_URL)
                .filter(oauth2Filter)
                .build();
    }
}