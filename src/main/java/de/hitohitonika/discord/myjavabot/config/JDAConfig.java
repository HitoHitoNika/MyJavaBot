package de.hitohitonika.discord.myjavabot.config;

import de.hitohitonika.discord.myjavabot.commandhandler.UserSpammer;
import de.hitohitonika.discord.myjavabot.detectors.*;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class JDAConfig {
    @Bean
    public JDA jda(@Value("${bot.token}")String token, @Value("${github.repos}") List<String> repos) throws InterruptedException {
        //Discord Verbindung wird erstellt, am Ende purzelt ein JDA Objekt raus - praktisch wie eine Engine zusehen.
        var jda = JDABuilder.create(token, List.of(GatewayIntent.values()))
                .addEventListeners(new KrillDetector(),new KysDetector(), new SoitzuDetector(), new TwitterLinkDetector(),new UserSpammer(),new WunkusDetector())
                .setActivity(Activity.competing("Soitzu hate tournament"))
                .build().awaitReady();

        //TODO: Dynamisches Command adden ermöglichen a la "Ist Command schon da? -> skip"

        jda.updateCommands().addCommands(
                Commands.slash("spotify","Spuckt den Spotify Übersicht Link aus."),
                Commands.slash("spam","Spammed den Huan deiner Wahl voll.").addOptions(
                        List.of(
                                new OptionData(OptionType.USER,"target","Der Huan"),
                                new OptionData(OptionType.STRING,"message","Die Nachricht für den Huan"),
                                new OptionData(OptionType.INTEGER,"amount","Die Anzahl der Nachrichten für den Huan")
                        )
                ),
                Commands.slash("twitch","Spuckt den Link für Hameds Twitch aus"),
                Commands.slash("report-issue","Hier kannst du Probleme mit einer meiner Anwendungen melden :)").addOptions(
                        List.of(
                                new OptionData(OptionType.STRING,"anwendung", "Mit welcher Anwendung tritt das Problem auf? (Im Zweifel nimm einfach den MyJavaBot)",true).addChoices(
                                        repos.stream().map(val -> new Command.Choice(val,val)).toList()
                                ),
                                new OptionData(OptionType.STRING,"title","Gib dem Problem ein kurzen, prägnanten und erklärenden Namen",true),
                                new OptionData(OptionType.STRING,"erwartung","Wie sollte es eigentlich funktionieren ?",true),
                                new OptionData(OptionType.STRING,"tatsächliches-verhalten","Wie äußert sich das Fehlverhalten?",true)
                        )
                )
        ).queue(commands -> System.out.println("REGISTRIERUNG ABGESCHLOSSEN: "+commands));

        return jda;
    }
}
