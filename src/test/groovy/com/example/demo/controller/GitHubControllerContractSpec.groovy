package com.example.demo.controller

import com.example.demo.dto.RepoInfo
import com.example.demo.dto.UserRepoResponse
import com.example.demo.service.GitHubService
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import spock.lang.Specification

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

class GitHubControllerContractSpec extends Specification {

    MockMvc mockMvc
    GitHubService gitHubService
    GitHubController controller

    def setup() {
        gitHubService = Mock(GitHubService)
        controller = new GitHubController(gitHubService)
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build()
    }

    def "endpoint should accept GET requests at /api/v1/users/{username}"() {
        given: "A valid username"
        def username = "testuser"
        def mockResponse = buildMockResponse(username)
        gitHubService.getUserWithRepos(username) >> mockResponse

        when: "Making a GET request to the endpoint"
        def result = mockMvc.perform(get("/api/v1/users/{username}", username))

        then: "Request should succeed"
        result.andExpect(status().isOk())
    }

    def "endpoint should reject POST requests with 405 Method Not Allowed"() {
        when: "Making a POST request to the endpoint"
        def result = mockMvc.perform(post("/api/v1/users/testuser"))

        then: "Request should be rejected with 405"
        result.andExpect(status().isMethodNotAllowed())
    }

    def "endpoint should return JSON content type"() {
        given: "A valid username"
        def username = "testuser"
        def mockResponse = buildMockResponse(username)
        gitHubService.getUserWithRepos(username) >> mockResponse

        when: "Making a GET request to the endpoint"
        def result = mockMvc.perform(get("/api/v1/users/{username}", username))

        then: "Response should have JSON content type"
        result.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
    }

    def "endpoint should return response with required fields"() {
        given: "A valid username"
        def username = "testuser"
        def mockResponse = buildMockResponse(username)
        gitHubService.getUserWithRepos(username) >> mockResponse

        when: "Making a GET request to the endpoint"
        def result = mockMvc.perform(get("/api/v1/users/{username}", username))

        then: "Response should contain all required fields"
        result.andExpect(status().isOk())
                .andExpect(jsonPath('$.user_name').exists())
                .andExpect(jsonPath('$.display_name').exists())
                .andExpect(jsonPath('$.avatar').exists())
                .andExpect(jsonPath('$.geo_location').exists())
                .andExpect(jsonPath('$.email').exists())
                .andExpect(jsonPath('$.url').exists())
                .andExpect(jsonPath('$.created_at').exists())
                .andExpect(jsonPath('$.repos').isArray())
    }

    def "endpoint should return response with correct field values"() {
        given: "A valid username"
        def username = "octocat"
        def mockResponse = UserRepoResponse.builder()
                .userName("octocat")
                .displayName("The Octocat")
                .avatar("https://avatars.githubusercontent.com/u/583231?v=4")
                .geoLocation("San Francisco")
                .email(null)
                .url("https://api.github.com/users/octocat")
                .createdAt("Tue, 25 Jan 2011 18:44:36 GMT")
                .repos([
                        RepoInfo.builder()
                                .name("test-repo")
                                .url("https://api.github.com/repos/octocat/test-repo")
                                .build()
                ])
                .build()
        gitHubService.getUserWithRepos(username) >> mockResponse

        when: "Making a GET request to the endpoint"
        def result = mockMvc.perform(get("/api/v1/users/{username}", username))

        then: "Response should contain correct values"
        result.andExpect(status().isOk())
                .andExpect(jsonPath('$.user_name').value("octocat"))
                .andExpect(jsonPath('$.display_name').value("The Octocat"))
                .andExpect(jsonPath('$.avatar').value("https://avatars.githubusercontent.com/u/583231?v=4"))
                .andExpect(jsonPath('$.geo_location').value("San Francisco"))
                .andExpect(jsonPath('$.email').isEmpty())
                .andExpect(jsonPath('$.url').value("https://api.github.com/users/octocat"))
                .andExpect(jsonPath('$.created_at').value("Tue, 25 Jan 2011 18:44:36 GMT"))
                .andExpect(jsonPath('$.repos[0].name').value("test-repo"))
                .andExpect(jsonPath('$.repos[0].url').value("https://api.github.com/repos/octocat/test-repo"))
    }

    def "endpoint should accept username as path variable"() {
        given: "Different usernames and their mock responses"
        def username = "user-name-123"
        def mockResponse = buildMockResponse(username)
        gitHubService.getUserWithRepos(username) >> mockResponse

        when: "Making a GET request with the username"
        def result = mockMvc.perform(get("/api/v1/users/{username}", username))

        then: "The username should be accepted"
        result.andExpect(status().isOk())
    }

    def "endpoint should have repos array in response"() {
        given: "A username with repos"
        def username = "testuser"
        def mockResponse = UserRepoResponse.builder()
                .userName(username)
                .displayName("Test User")
                .avatar("https://avatar.url")
                .geoLocation("Location")
                .email("test@example.com")
                .url("https://api.github.com/users/${username}")
                .createdAt("Mon, 01 Jan 2020 00:00:00 GMT")
                .repos([
                        RepoInfo.builder().name("repo1").url("https://api.github.com/repos/${username}/repo1").build(),
                        RepoInfo.builder().name("repo2").url("https://api.github.com/repos/${username}/repo2").build()
                ])
                .build()
        gitHubService.getUserWithRepos(username) >> mockResponse

        when: "Making a GET request to the endpoint"
        def result = mockMvc.perform(get("/api/v1/users/{username}", username))

        then: "Response should contain repos array with correct structure"
        result.andExpect(status().isOk())
                .andExpect(jsonPath('$.repos').isArray())
                .andExpect(jsonPath('$.repos.length()').value(2))
                .andExpect(jsonPath('$.repos[0].name').exists())
                .andExpect(jsonPath('$.repos[0].url').exists())
                .andExpect(jsonPath('$.repos[1].name').exists())
                .andExpect(jsonPath('$.repos[1].url').exists())
    }

    def "endpoint should maintain field order in response"() {
        given: "A valid username"
        def username = "testuser"
        def mockResponse = buildMockResponse(username)
        gitHubService.getUserWithRepos(username) >> mockResponse

        when: "Making a GET request to the endpoint"
        def result = mockMvc.perform(get("/api/v1/users/{username}", username))

        then: "Response fields should be in correct order"
        def responseBody = result.andReturn().response.contentAsString
        def fieldOrder = ['user_name', 'display_name', 'avatar', 'geo_location', 'email', 'url', 'created_at', 'repos']

        // Verify field order by checking their positions in the JSON string
        def positions = fieldOrder.collect { field -> responseBody.indexOf("\"${field}\"") }
        positions == positions.sort()
    }

    def "endpoint response should have repos with only name and url fields"() {
        given: "A username with repos"
        def username = "testuser"
        def mockResponse = buildMockResponse(username)
        gitHubService.getUserWithRepos(username) >> mockResponse

        when: "Making a GET request to the endpoint"
        def result = mockMvc.perform(get("/api/v1/users/{username}", username))

        then: "Repo objects should only have name and url fields"
        result.andExpect(status().isOk())
                .andExpect(jsonPath('$.repos[0].name').exists())
                .andExpect(jsonPath('$.repos[0].url').exists())
                .andExpect(jsonPath('$.repos[0].id').doesNotExist())
                .andExpect(jsonPath('$.repos[0].description').doesNotExist())
                .andExpect(jsonPath('$.repos[0].owner').doesNotExist())
    }

    private UserRepoResponse buildMockResponse(String username) {
        return UserRepoResponse.builder()
                .userName(username)
                .displayName("Display Name")
                .avatar("https://avatar.url")
                .geoLocation("Location")
                .email("email@example.com")
                .url("https://api.github.com/users/${username}")
                .createdAt("Mon, 01 Jan 2020 00:00:00 GMT")
                .repos([
                        RepoInfo.builder()
                                .name("repo1")
                                .url("https://api.github.com/repos/${username}/repo1")
                                .build()
                ])
                .build()
    }
}
