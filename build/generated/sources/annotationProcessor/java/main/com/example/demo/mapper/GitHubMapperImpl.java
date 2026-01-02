package com.example.demo.mapper;

import com.example.demo.dto.GitHubRepoResponse;
import com.example.demo.dto.GitHubUserResponse;
import com.example.demo.dto.RepoInfo;
import com.example.demo.dto.UserRepoResponse;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-01-02T12:22:02-0700",
    comments = "version: 1.6.3, compiler: IncrementalProcessingEnvironment from gradle-language-java-8.11.1.jar, environment: Java 21.0.2 (Oracle Corporation)"
)
@Component
public class GitHubMapperImpl implements GitHubMapper {

    @Override
    public UserRepoResponse toUserRepoResponse(GitHubUserResponse user, List<GitHubRepoResponse> repos) {
        if ( user == null && repos == null ) {
            return null;
        }

        UserRepoResponse.UserRepoResponseBuilder userRepoResponse = UserRepoResponse.builder();

        if ( user != null ) {
            userRepoResponse.userName( user.getLogin() );
            userRepoResponse.displayName( user.getName() );
            userRepoResponse.avatar( user.getAvatarUrl() );
            userRepoResponse.geoLocation( user.getLocation() );
            userRepoResponse.email( user.getEmail() );
            userRepoResponse.url( user.getUrl() );
            userRepoResponse.createdAt( formatDate( user.getCreatedAt() ) );
        }
        userRepoResponse.repos( toRepoInfoList( repos ) );

        return userRepoResponse.build();
    }

    @Override
    public RepoInfo toRepoInfo(GitHubRepoResponse repo) {
        if ( repo == null ) {
            return null;
        }

        RepoInfo.RepoInfoBuilder repoInfo = RepoInfo.builder();

        repoInfo.name( repo.getName() );
        repoInfo.url( repo.getUrl() );

        return repoInfo.build();
    }

    @Override
    public List<RepoInfo> toRepoInfoList(List<GitHubRepoResponse> repos) {
        if ( repos == null ) {
            return null;
        }

        List<RepoInfo> list = new ArrayList<RepoInfo>( repos.size() );
        for ( GitHubRepoResponse gitHubRepoResponse : repos ) {
            list.add( toRepoInfo( gitHubRepoResponse ) );
        }

        return list;
    }
}
