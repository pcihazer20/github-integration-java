package com.example.demo.mapper;

import com.example.demo.dto.GitHubRepoResponse;
import com.example.demo.dto.GitHubUserResponse;
import com.example.demo.dto.RepoInfo;
import com.example.demo.dto.UserRepoResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Mapper(componentModel = "spring")
public interface
GitHubMapper {

    @Mapping(source = "user.login", target = "userName")
    @Mapping(source = "user.name", target = "displayName")
    @Mapping(source = "user.avatarUrl", target = "avatar")
    @Mapping(source = "user.location", target = "geoLocation")
    @Mapping(source = "user.email", target = "email")
    @Mapping(source = "user.url", target = "url")
    @Mapping(source = "user.createdAt", target = "createdAt", qualifiedByName = "formatDate")
    @Mapping(source = "repos", target = "repos")
    UserRepoResponse toUserRepoResponse(GitHubUserResponse user, List<GitHubRepoResponse> repos);

    RepoInfo toRepoInfo(GitHubRepoResponse repo);

    List<RepoInfo> toRepoInfoList(List<GitHubRepoResponse> repos);

    @Named("formatDate")
    default String formatDate(String isoDateTime) {
        if (isoDateTime == null) {
            return null;
        }
        try {
            ZonedDateTime zonedDateTime = ZonedDateTime.parse(isoDateTime)
                    .withZoneSameInstant(ZoneId.of("GMT"));
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.ENGLISH);
            return zonedDateTime.format(formatter);
        } catch (Exception e) {
            return isoDateTime;
        }
    }
}