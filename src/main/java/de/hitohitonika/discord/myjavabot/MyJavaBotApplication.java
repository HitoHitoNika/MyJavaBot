package de.hitohitonika.discord.myjavabot;

import de.hitohitonika.discord.myjavabot.detectors.*;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.List;

@SpringBootApplication
@EnableScheduling
public class MyJavaBotApplication {


    public static void main(String[] args) {
        SpringApplication.run(MyJavaBotApplication.class, args);
    }

    @Bean
    public JDA jda(@Value("${bot.token}")String token) throws InterruptedException {
        return JDABuilder.create(token,List.of(GatewayIntent.values()))
                .addEventListeners(new KrillDetector(),new KysDetector(), new SoitzuDetector(), new TwitterLinkDetector(),new UserSpammer())
                .setActivity(Activity.competing("Soitzu hate tournament"))
                .build().awaitReady();
    }

}
