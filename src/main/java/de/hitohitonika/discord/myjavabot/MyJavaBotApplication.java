package de.hitohitonika.discord.myjavabot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication
@EnableScheduling
public class MyJavaBotApplication {

    public static void main(String[] args) {
       SpringApplication.run(MyJavaBotApplication.class, args);
    }

}
