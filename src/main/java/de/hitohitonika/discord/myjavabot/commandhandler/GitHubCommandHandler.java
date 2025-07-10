package de.hitohitonika.discord.myjavabot.commandhandler;

import de.hitohitonika.discord.myjavabot.services.GitHubService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
public class GitHubCommandHandler extends ListenerAdapter {
    private final GitHubService gitHubService;
    private final JDA jda;
    private final String hhnId;

    public GitHubCommandHandler(GitHubService gitHubService, JDA jda, @Value("${discord.id.max}") String maxId) {
        this.hhnId = maxId;
        this.gitHubService = gitHubService;
        jda.addEventListener(this);
        this.jda = jda;
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getUser().isBot()) {
            return;
        }

        if (event.getName().equals("report-issue")) {
            reportIssue(event);
        } else if (event.getName().equals("request-feature")) {
            requestFeature(event);
        }

        //TODO: Mehr GitHub Befehle ? Bspw Liste meiner issues oder sowas


    }

    public void reportIssue(SlashCommandInteractionEvent event) {
        event.deferReply().queue();

        var app = event.getInteraction().getOption("anwendung").getAsString();
        var title = event.getOption("title").getAsString();
        var expected = event.getOption("erwartung").getAsString();
        var actually = event.getOption("tatsächliches-verhalten").getAsString();
        var reporter = event.getUser().getName();

        log.info("Folgende Werte konnten dem Event entnommen werden: app:[{}], title:[{}], expected:[{}], actually:[{}], reporter:[{}]", app, title, expected, actually, reporter);

        try {
            var issue = gitHubService.createIssue(title, formatIssueBody(expected, actually, reporter), app);
            issue.addLabels("bug");
            log.info("Issue wurde angelegt! [{}]", issue);
            event.getHook().sendMessage("Dein Ticket wurde erfolgreich angelegt und liegt hier: %s".formatted(issue.getHtmlUrl())).queue();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            jda.getUserById(hhnId).openPrivateChannel().queue(channel ->
                    channel.sendMessage("Jemand wollte n Issue anlegen und es hat gekracht. app: %s , title %s , expected %s , actually %s , reporter %s"
                                    .formatted(app, title, expected, actually, reporter))
                            .queue()
            );
            event.getHook().sendMessage("Fehler beim erstellen des Tickets, bitte wende dich an <@%s>. Die Informationen wurden bereits weitergeleitet!".formatted(hhnId)).queue();
        }
    }

    public void requestFeature(SlashCommandInteractionEvent event) {
        event.deferReply().queue();

        var app = event.getInteraction().getOption("anwendung").getAsString();
        var title = event.getOption("title").getAsString();
        var description = event.getOption("beschreibung").getAsString();
        var reporter = event.getUser().getName();

        log.info("Folgende Werte konnten dem Event entnommen werden: app:[{}], title:[{}], description:[{}], reporter:[{}]", app, title, description, reporter);

        try {
            //Issue ist hier irreführend, aber aus GitHub Sicht sind das alles Issues.
            var issue = gitHubService.createIssue(title,formatFeatureBody(description,reporter),app);
            issue.addLabels("enhancement");
            log.info("Issue wurde angelegt! [{}]", issue);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            jda.getUserById(hhnId).openPrivateChannel().queue(channel ->
                    channel.sendMessage("Jemand wollte n feature request anlegen und es hat gekracht. app %s , title %s , description %s , reporter %s"
                                    .formatted(app, title, description, reporter))
                            .queue()
            );
            event.getHook().sendMessage("Fehler beim erstellen des Tickets, bitte wende dich an <@%s>. Die Informationen wurden bereits weitergeleitet!".formatted(hhnId)).queue();
        }
    }

    private String formatIssueBody(String expected, String actual, String reporter) {
        return """
                Erwartetes Verhalten: %s,
                Tatsächliches Verhalten: %s,
                Melder: %s
                """.formatted(expected, actual, reporter);
    }

    private String formatFeatureBody(String description, String reporter) {
        return """
                Beschreibung: %s,
                Melder: %s
                """.formatted(description, reporter);
    }

}
