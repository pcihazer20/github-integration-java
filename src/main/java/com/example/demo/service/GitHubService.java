package com.example.demo.service;

import com.example.demo.client.GitHubClient;
import com.example.demo.dto.GitHubRepoResponse;
import com.example.demo.dto.GitHubUserResponse;
import com.example.demo.dto.UserRepoResponse;
import com.example.demo.mapper.GitHubMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GitHubService {

    private final GitHubClient gitHubClient;
    private final GitHubMapper gitHubMapper;

    public UserRepoResponse getUserWithRepos(String username) {
        log.debug("Fetching GitHub user and repository data for username: {}", username);

        GitHubUserResponse user = gitHubClient.getUser(username);
        log.debug("Successfully fetched user data for username: {}. Display name: {}, Created: {}",
                username, user.getName(), user.getCreatedAt());

        List<GitHubRepoResponse> repos = gitHubClient.getRepos(username);
        log.debug("Successfully fetched {} repositories for username: {}", repos.size(), username);

        UserRepoResponse response = gitHubMapper.toUserRepoResponse(user, repos);
        log.debug("Successfully mapped response for username: {}. Total repos in response: {}",
                username, response.getRepos() != null ? response.getRepos().size() : 0);

        return response;
    }
}