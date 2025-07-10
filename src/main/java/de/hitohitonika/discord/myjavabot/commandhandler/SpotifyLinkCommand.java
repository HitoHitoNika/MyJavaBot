package de.hitohitonika.discord.myjavabot.commandhandler;

import de.hitohitonika.discord.myjavabot.data.spotify.PaymentInfo;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.util.Arrays;

@Component
@Slf4j
public class SpotifyLinkCommand extends ListenerAdapter {
    private final static String SPOTIFY_URL_MESSAGE = """
            Neuer Monat, neue Rechnung :)
            <@&1049289887146319952>
            https://spotify.hitohitonika.de/debts
            """;

    private final static String SPECIFIC_MESSAGE = "Dein Link: https://spotify.hitohitonika.de/debts?name=%s";

    private final String wunkusId;
    private final String hamhamId;
    private final String maxId;

    private final WebClient webClient;

    private TextChannel guildChannel;

    public SpotifyLinkCommand(JDA jda, @Value("${discord.id.humunkulus}") String wunkus, @Value("${discord.id.hamham}") String hamham, @Value("${discord.id.max}") String max, @Value("${discord.id.lobby1}") String lobbyEinsId, @Value("${discord.id.lobby1.channel.spotify}") String spotifyChannelId, @Value("${service.spotify}") String spotifyService) throws IOException {
        wunkusId = wunkus;
        hamhamId = hamham;
        maxId = max;

        var guild = jda.getGuildById(lobbyEinsId);
        if(guild != null){
            guildChannel = jda.getTextChannelById(spotifyChannelId);
        }
        jda.addEventListener(this);
        webClient = WebClient.builder()
                    .baseUrl(spotifyService)
                    .build();
    }


    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if(event.getName().equals("spotify")){
            event.deferReply().queue();
            if(event.getUser().getId().equals(wunkusId)){
                event.getHook().sendMessage(SPECIFIC_MESSAGE.formatted("Lucas")).queue();
            }else if(event.getUser().getId().equals(hamhamId)){
                event.getHook().sendMessage(SPECIFIC_MESSAGE.formatted("Hamed")).queue();
            }
            event.getHook().sendMessage(SPOTIFY_URL_MESSAGE).queue();
        }
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().getId().equals(maxId) && event.getMessage().getContentRaw().startsWith("###pay:")) {
            var messageParts = event.getMessage().getContentRaw().split(":",3);

            if (messageParts.length != 3) {
                return;
            }

            log.info(Arrays.toString(messageParts));

            String name = messageParts[1].trim();
            String amount  = messageParts[2].trim();

            webClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .path("/pay")
                            .queryParam("name",name)
                            .queryParam("amount",amount)
                            .build())
                    .header(HttpHeaders.CONTENT_TYPE,"Application/Json")
                    .retrieve()
                    .bodyToMono(PaymentInfo.class)
                    .doOnSuccess(response -> event.getChannel().sendMessage(response.toString()).queue())
                    .doOnError(throwable -> event.getChannel().sendMessage("Wallah hier hats gekracht "+throwable).queue())
                    .subscribe();
        }
    }

    @Scheduled(cron = "0 0 5 1 * ?")
    public void sendReminder(){
        guildChannel.sendMessage(SPOTIFY_URL_MESSAGE).queue();
    }
}
