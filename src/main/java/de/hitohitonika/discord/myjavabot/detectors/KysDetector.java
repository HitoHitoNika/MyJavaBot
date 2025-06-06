package de.hitohitonika.discord.myjavabot.detectors;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

@Slf4j
public class KysDetector extends ListenerAdapter {

    @Override
    public void onMessageReceived(MessageReceivedEvent messageReceivedEvent) {
        if (messageReceivedEvent.getAuthor().isBot() ||
                !(messageReceivedEvent.getMessage().getContentRaw().toLowerCase().contains("kill yourself") ||
                        messageReceivedEvent.getMessage().getContentRaw().toLowerCase().contains("kys"))) return;
        log.info("Message met criteria. Author: {} , Content {} ",messageReceivedEvent.getAuthor(),messageReceivedEvent.getMessage().getContentRaw());
        messageReceivedEvent.getChannel()
                .sendMessage("https://vxtwitter.com/i/status/1912444281601814819")
                .queue();
    }
}
