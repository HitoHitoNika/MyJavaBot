package de.hitohitonika.discord.myjavabot.commandhandler;

import de.hitohitonika.discord.myjavabot.data.spotify.PaymentInfo;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.http.HttpHeaders;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

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

    private final static String GUILD_ID = "530135236680613890";
    private final static String CHANNEL_ID = "1049290103354298420";

    private final static String WUNKUS_ID = "306792892373139456";
    private final static String HAMHAM_ID = "253896365006913536";
    private final static String MAX_ID = "229313757975805952";

    private final WebClient webClient;

    private TextChannel guildChannel;

    public SpotifyLinkCommand(JDA jda) {
        var guild = jda.getGuildById(GUILD_ID);
        if(guild != null){
            guildChannel = jda.getTextChannelById(CHANNEL_ID);
        }
        jda.addEventListener(this);
        webClient = WebClient.builder()
                    .baseUrl("http://192.168.0.229:8082/paymentinfo")
                    .build();
    }


    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if(event.getName().equals("spotify")){
            event.deferReply().queue();
            if(event.getUser().getId().equals(WUNKUS_ID)){
                event.getHook().sendMessage(SPECIFIC_MESSAGE.formatted("Lucas")).queue();
            }else if(event.getUser().getId().equals(HAMHAM_ID)){
                event.getHook().sendMessage(SPECIFIC_MESSAGE.formatted("Hamed")).queue();
            }
            event.getHook().sendMessage(SPOTIFY_URL_MESSAGE).queue();
        }
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().getId().equals(MAX_ID) && event.getMessage().getContentRaw().startsWith("###pay:")) {
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
