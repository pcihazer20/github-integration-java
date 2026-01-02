package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonPropertyOrder({"userName", "displayName", "avatar", "geoLocation", "email", "url", "createdAt", "repos"})
public class UserRepoResponse {
    @JsonProperty("user_name")
    private String userName;

    @JsonProperty("display_name")
    private String displayName;

    private String avatar;

    @JsonProperty("geo_location")
    private String geoLocation;

    private String email;
    private String url;

    @JsonProperty("created_at")
    private String createdAt;

    private List<RepoInfo> repos;
}