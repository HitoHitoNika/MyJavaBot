package de.hitohitonika.discord.myjavabot.commandhandler;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;

import java.time.Duration;
import java.time.Instant;

@Component
@Slf4j
public class TwitchCommandHandler extends ListenerAdapter{
    private final static String GUILD_ID = "530135236680613890";
    private final static String TARGET_CHANNEL_ID = "977623213053980692";

    private TextChannel channel;

    private final WebClient webClient;
    private final String twitchClientId;

    private Instant lastNotificationTimestamp;

    public TwitchCommandHandler(JDA jda, @Qualifier("twitchWebClient") WebClient webClient, @Value("${spring.security.oauth2.client.registration.twitch.client-id}") String twitchClientId) {
        var guild = jda.getGuildById(GUILD_ID);
        if(guild != null){
            channel = jda.getTextChannelById(TARGET_CHANNEL_ID);
        }
        jda.addEventListener(this);
        this.webClient = webClient;
        this.twitchClientId = twitchClientId;
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if(event.getName().equals("twitch")){
            event.reply("HIER STREAMT DAS ALLE ECHTE LAMA: https://www.twitch.tv/mr_lama_").queue();
        }
    }

    @Scheduled(fixedDelay = 60000)
    public void checkIfLamaLive(){
        if(lastNotificationTimestamp != null && Duration.between(lastNotificationTimestamp, Instant.now()).toHours() < 2){
            return;
        }
        this.webClient.get().uri(uriBuilder -> uriBuilder.path("/streams").queryParam("user_login","mr_lama_").build())
                .header("Client-ID", twitchClientId)
                .retrieve()
                .bodyToMono(String.class)
                .doOnSuccess(response -> {
                    if(response.contains("game_id")){
                        channel.sendMessage("LAMA IST JETZT LIVE: https://www.twitch.tv/mr_lama_").queue();
                        lastNotificationTimestamp = Instant.now();
                    } else {
                        lastNotificationTimestamp = null;
                    }
                })
                .doOnError(WebClientException.class, error -> log.error("ALARM"))
                .subscribe();
    }

}