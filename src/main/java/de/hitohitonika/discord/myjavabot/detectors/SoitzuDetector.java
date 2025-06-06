package de.hitohitonika.discord.myjavabot.detectors;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SoitzuDetector extends ListenerAdapter {

    private int soitzuCounter = 0;

    @Override
    public void onMessageReceived(MessageReceivedEvent messageReceivedEvent) {
        if (messageReceivedEvent.getAuthor().getId().equals("411916575340232704")){
            soitzuCounter += 1;
            log.info("Soitzu Detected. Current Counter: {}", soitzuCounter);
            if(soitzuCounter % 3 == 0){
                soitzuCounter = 0;
                messageReceivedEvent.getChannel().sendMessage("Nikita halt doch bitte mal dein Maul").queue();
            }
        }
    }
}

