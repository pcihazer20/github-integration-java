package com.example.demo.service;

import com.example.demo.client.GitHubClient;
import com.example.demo.dto.GitHubRepoResponse;
import com.example.demo.dto.GitHubUserResponse;
import com.example.demo.dto.UserRepoResponse;
import com.example.demo.mapper.GitHubMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GitHubService {

    private final GitHubClient gitHubClient;
    private final GitHubMapper gitHubMapper;

    public UserRepoResponse getUserWithRepos(String username) {
        GitHubUserResponse user = gitHubClient.getUser(username);
        List<GitHubRepoResponse> repos = gitHubClient.getRepos(username);

        return gitHubMapper.toUserRepoResponse(user, repos);
    }
}