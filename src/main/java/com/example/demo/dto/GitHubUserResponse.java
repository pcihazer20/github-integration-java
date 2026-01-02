package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GitHubUserResponse {
    private String login;
    private String name;

    @JsonProperty("avatar_url")
    private String avatarUrl;

    private String location;
    private String email;
    private String url;

    @JsonProperty("created_at")
    private String createdAt;
}