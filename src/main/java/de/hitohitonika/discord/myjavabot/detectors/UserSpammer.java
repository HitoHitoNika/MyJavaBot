package de.hitohitonika.discord.myjavabot.detectors;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.stream.IntStream;

@Slf4j
public class UserSpammer extends ListenerAdapter {
    private final int DEFAULT_AMOUNT = 1;
    private final String DEFAULT_MESSAGE = "<@%s> will dich ficken";
    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if(event.getName().equals("spam")){
            event.deferReply().queue();
            log.info("Processing spam Command!");

            int amount = getAmount(event);

            User user = getUser(event);
            if(user == null){
                return;
            }

            String message = getMessage(event);

            event.getHook().sendMessage("Taktische Nuke gegen <@%s> wird abgefeuert!".formatted(user.getId())).queue();

            if(message.equals(DEFAULT_MESSAGE)) {
                for (int i = 0; i < amount; i++) {
                    user.openPrivateChannel().queue((channel) -> {
                        channel.sendMessage(String.format(DEFAULT_MESSAGE, event.getUser().getId())).queue();
                    });
                }
            }else{
                for (int i = 0; i < amount; i++) {
                    user.openPrivateChannel().queue((channel) -> {
                        channel.sendMessage(message).queue();
                    });
                }
            }
        }
    }

    private int getAmount(SlashCommandInteractionEvent event){
        try {
            return ((int) event.getOption("amount").getAsDouble());
        } catch (Exception e) {
            log.debug("No Amount was given, proceeds with Default");
            return DEFAULT_AMOUNT;
        }
    }

    private User getUser(SlashCommandInteractionEvent event){
        try {
            var user = event.getOption("target").getAsUser();
            if(user.isBot()| user.isSystem()){
                event.getHook().sendMessage("Kreativ, Discord erlaubt aber leider so faxen nicht 😭").queue();
                return null;
            }
            return user;
        }catch (Exception e){
            log.debug("No target was given, aborting...");
            event.getHook().sendMessage("Du Trottel hast kein Target angegeben 💀").queue();
            return null;
        }
    }

    private String getMessage(SlashCommandInteractionEvent event){
        try {
            return event.getOption("message").getAsString();
        }catch (Exception e){
            log.debug("No Message was given, proceeds with Default");
            return DEFAULT_MESSAGE;
        }
    }

}
