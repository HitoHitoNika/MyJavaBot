package de.hitohitonika.discord.myjavabot.services;

import lombok.Getter;
import org.kohsuke.github.GHIssue;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class GitHubService {
    private final GitHub gitHub;
    //Theoretisch unnötig, TODO: Kicken ?
    @Getter
    private final List<String> repositoryNames;
    private final String username;

    public GitHubService(@Value("${github.username}") String username, @Value("${github.repos}") List<String> repositoryNames, @Value("${github.token}") String token) throws IOException {
        this.username = username;
        this.repositoryNames = repositoryNames;
        this.gitHub = new GitHubBuilder().withOAuthToken(token).build();
        this.gitHub.checkApiUrlValidity();
    }

    public GHIssue createIssue(String title, String body, String repository) throws IOException {
        GHRepository repo = gitHub.getRepository(username+"/"+repository);

        return repo.createIssue(title).body(body).create();
    }
}
