package de.hitohitonika.discord.myjavabot.detectors;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


@Component
@Slf4j
public class SoitzuDetector extends ListenerAdapter {

    private int soitzuCounter = 0;
    private final String soitzuId;

    public SoitzuDetector(@Value("${discord.id.soitzu}") String soitzuId, JDA jda) {
        this.soitzuId = soitzuId;
        jda.addEventListener(this);
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent messageReceivedEvent) {
        if (messageReceivedEvent.getAuthor().getId().equals(soitzuId)){
            soitzuCounter += 1;
            log.info("Soitzu Detected. Current Counter: {}", soitzuCounter);
            if(soitzuCounter % 5 == 0){
                soitzuCounter = 0;
                messageReceivedEvent.getChannel().sendMessage("Nikita halt doch bitte mal dein Maul").queue();
            }
        }
    }
}

