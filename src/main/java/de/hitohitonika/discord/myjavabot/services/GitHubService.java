package de.hitohitonika.discord.myjavabot.services;

import org.kohsuke.github.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Service
public class GitHubService {
    private final GitHub gitHub;
    private List<String> repositoryNames;
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

    public List<String> getRepositoryNames() throws IOException {
        var allRepos = allRepositories();

        repositoryNames = new ArrayList<>(allRepos.keySet());

        return repositoryNames;
    }

    public Map<String, String> allRepositories() throws IOException {
        GHMyself myself = gitHub.getMyself();

        Map<String, GHRepository> repositories = myself.getAllRepositories();

        Map<String, String> result = new TreeMap<>();
        repositories.values().forEach(repository -> result.put(repository.getName(), repository.getHtmlUrl().toString()));

        return result;
    }
}
