package de.hitohitonika.discord.myjavabot.detectors;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

@Slf4j
public class KrillDetector extends ListenerAdapter {

    @Override
    public void onMessageReceived(MessageReceivedEvent messageReceivedEvent) {
        if (messageReceivedEvent.getAuthor().isBot() || !messageReceivedEvent.getMessage().getContentRaw().toLowerCase().contains("krill")) return;
        log.info("Message met criteria. Author: {} , Content {} ",messageReceivedEvent.getAuthor(),messageReceivedEvent.getMessage().getContentRaw());
        messageReceivedEvent.getChannel()
                .sendMessage("https://tenor.com/view/you-should-low-tier-god-ltg-krill-one-in-a-krillion-gif-7939027577238167153")
                .queue();
    }


}

