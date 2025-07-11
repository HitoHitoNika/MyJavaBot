package de.hitohitonika.discord.myjavabot.config;

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
        var jda = JDABuilder.createLight(token, List.of(GatewayIntent.DIRECT_MESSAGES,GatewayIntent.GUILD_MESSAGES,GatewayIntent.MESSAGE_CONTENT))
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
                ),
                Commands.slash("request-feature","Hier kannst du dir ein neues Feature wünschen").addOptions(
                        List.of(
                                new OptionData(OptionType.STRING,"anwendung", "Mit welcher Anwendung tritt das Problem auf? (Im Zweifel nimm einfach den MyJavaBot)",true).addChoices(
                                        repos.stream().map(val -> new Command.Choice(val,val)).toList()
                                ),
                                new OptionData(OptionType.STRING,"title","Gib dem Feature ein kurzen, prägnanten und erklärenden Namen",true),
                                new OptionData(OptionType.STRING,"beschreibung","Erkläre genau wie du dir vorstellst das dein Feature funktionieren soll",true)
                        )
                )
        ).queue(commands -> System.out.println("REGISTRIERUNG ABGESCHLOSSEN: "+commands));
        return jda;
    }
}
