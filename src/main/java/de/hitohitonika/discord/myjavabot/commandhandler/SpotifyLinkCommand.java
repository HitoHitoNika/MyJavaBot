package de.hitohitonika.discord.myjavabot.commandhandler;

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
            Neuer Monat, neue Rechnung :)
            <@&1049289887146319952>
            https://spotify.hitohitonika.de/debts
            """;

    private final static String SPECIFIC_MESSAGE = "Dein Link: https://spotify.hitohitonika.de/debts?%s";

    private final static String GUILD_ID = "530135236680613890";
    private final static String CHANNEL_ID = "1049290103354298420";

    private final static String WUNKUS_ID = "306792892373139456";
    private final static String HAMHAM_ID = "253896365006913536";

    private TextChannel guildChannel;

    public SpotifyLinkCommand(JDA jda) {
        var guild = jda.getGuildById(GUILD_ID);
        if(guild != null){
            guildChannel = jda.getTextChannelById(CHANNEL_ID);
        }
        jda.addEventListener(this);
    }


    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if(event.getName().equals("spotify")){
            event.deferReply().queue();
            if(event.getUser().getId().equals(WUNKUS_ID)){
                //TODO
                event.getHook().sendMessage(SPOTIFY_URL_MESSAGE).queue();
            }else if(event.getUser().getId().equals(HAMHAM_ID)){
                event.getHook().sendMessage(SPOTIFY_URL_MESSAGE).queue();
            }
            event.getHook().sendMessage(SPOTIFY_URL_MESSAGE).queue();
        }
    }

    @Scheduled(cron = "0 0 5 1 * ?")
    public void sendReminder(){
        guildChannel.sendMessage(SPOTIFY_URL_MESSAGE).queue();
    }
}
