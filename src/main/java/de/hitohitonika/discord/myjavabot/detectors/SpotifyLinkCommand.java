package de.hitohitonika.discord.myjavabot.detectors;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SpotifyLinkCommand extends ListenerAdapter {
    private final static String SPOTIFY_URL_MESSAGE = """
            Neuer Monat, neue Rechnung :)  <br>
            <br>
            <@&1049289887146319952>
            https://docs.google.com/spreadsheets/d/1SJaYWfOVZXFLG0bq_740pDzVtOsCoUtDQr2JcoPepAU/edit?usp=sharing
            """;

    private final static String GUILD_ID ="530135236680613890";
    private final static String CHANNEL_ID = "1049290103354298420";

    private TextChannel guildChannel;

    public SpotifyLinkCommand(JDA jda) {
        var guild = jda.getGuildById(GUILD_ID);
        if(guild != null){
            guildChannel = jda.getTextChannelById(CHANNEL_ID);
        }
        log.info(guildChannel.getName());
        jda.addEventListener(this);
    }


    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        event.reply(SPOTIFY_URL_MESSAGE).queue();
    }

    @Scheduled(cron = "0 0 0 1 * ?")
    public void sendReminder(){
        guildChannel.sendMessage(SPOTIFY_URL_MESSAGE).queue();
    }
}
