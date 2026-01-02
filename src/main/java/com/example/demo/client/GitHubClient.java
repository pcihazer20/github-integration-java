package com.example.demo.client;

import com.example.demo.config.GitHubFeignConfiguration;
import com.example.demo.dto.GitHubRepoResponse;
import com.example.demo.dto.GitHubUserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(
        name = "github-client",
        url = "${github.api.url:https://api.github.com}",
        configuration = GitHubFeignConfiguration.class
)
public interface GitHubClient {

    @GetMapping("/users/{username}")
    GitHubUserResponse getUser(@PathVariable("username") String username);

    @GetMapping("/users/{username}/repos")
    List<GitHubRepoResponse> getRepos(@PathVariable("username") String username);
}