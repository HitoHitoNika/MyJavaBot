package de.hitohitonika.discord.myjavabot.commandhandler;

import de.hitohitonika.discord.myjavabot.services.GitHubService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
public class IssueReportHandler extends ListenerAdapter {
    private final GitHubService gitHubService;

    public IssueReportHandler(GitHubService gitHubService, JDA jda) {
        this.gitHubService = gitHubService;
        jda.addEventListener(this);
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getUser().isBot()) {
            return;
        }

        if (event.getName().equals("report-issue")) {
            event.deferReply().queue();
            log.info("EVENT INCOMING: {}",event);

            var app = event.getInteraction().getOption("anwendung").getAsString();
            var title = event.getOption("title").getAsString();
            var expected = event.getOption("erwartung").getAsString();
            var actually = event.getOption("tatsächliches-verhalten").getAsString();
            var reporter = event.getUser().getName();

            log.info("Folgende Werte konnten dem Event entnommen werden: app:[{}], title:[{}], expected:[{}], actually:[{}], reporter:[{}]", app, title, expected, actually, reporter);

            try {
                var issue = gitHubService.createIssue(title,formattedBody(expected,actually,reporter),app);
                log.info("Issue wurde angelegt! [{}]", issue);
                event.getHook().sendMessage("Dein Ticket wurde erfolgreich angelegt und liegt hier: %s".formatted(issue.getHtmlUrl())).queue();
            } catch (IOException e) {
                log.error(e.getMessage(),e);
                event.getHook().sendMessage("Fehler beim erstellen des Tickets, bitte wende dich an Max").queue();
            }
        }
    }

    private String formattedBody(String expected, String actual, String reporter) {
        return """
            Erwartetes Verhalten: %s,
            Tatsächliches Verhalten: %s,
            Melder: %s
            """.formatted(expected, actual, reporter);
    }


}
