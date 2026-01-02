package com.example.demo.controller;

import com.example.demo.dto.UserRepoResponse;
import com.example.demo.service.GitHubService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class GitHubController {

    private final GitHubService gitHubService;

    @GetMapping("/{username}")
    public ResponseEntity<UserRepoResponse> getUserWithRepos(@PathVariable String username) {
        UserRepoResponse response = gitHubService.getUserWithRepos(username);
        return ResponseEntity.ok(response);
    }
}