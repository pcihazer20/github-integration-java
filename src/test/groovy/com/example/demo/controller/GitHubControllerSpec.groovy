package com.example.demo.controller

import com.example.demo.DemoApplication
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.client.WireMock
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock
import org.springframework.http.MediaType
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.MockMvc
import spock.lang.Specification

import static com.github.tomakehurst.wiremock.client.WireMock.*
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest(
        classes = DemoApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = ["github.api.url=http://localhost:\${wiremock.server.port}"]
)
@AutoConfigureMockMvc
@AutoConfigureWireMock(port = 0)
class GitHubControllerSpec extends Specification {

    @Autowired
    MockMvc mockMvc

    @Autowired
    ObjectMapper objectMapper

    def setup() {
        WireMock.reset()
    }

    def "should return merged user and repo data matching expected payload"() {
        given: "Mock GitHub API responses"
        def username = "octocat"
        def userPayload = loadJsonFile("/contracts/user-payload.json")
        def repoPayload = loadJsonFile("/contracts/repo-payload-single.json")
        def expectedMergedPayload = loadJsonFile("/contracts/merged-payload.json")

        stubFor(WireMock.get(urlEqualTo("/users/${username}"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(userPayload)))

        stubFor(WireMock.get(urlEqualTo("/users/${username}/repos"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(repoPayload)))

        when: "Calling the merged user endpoint"
        def result = mockMvc.perform(get("/api/users/${username}")
                .contentType(MediaType.APPLICATION_JSON))

        then: "Response should match expected merged payload"
        result.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(expectedMergedPayload, true))

        and: "GitHub API was called"
        verify(exactly(1), getRequestedFor(urlEqualTo("/users/${username}")))
        verify(exactly(1), getRequestedFor(urlEqualTo("/users/${username}/repos")))
    }

    def "should handle rate limiting with retries"() {
        given: "Mock GitHub API with initial rate limit error"
        def username = "testuser"
        def userPayload = loadJsonFile("/contracts/user-payload.json")
        def repoPayload = loadJsonFile("/contracts/repo-payload-single.json")

        // First call returns 429 (rate limit), subsequent calls succeed
        stubFor(WireMock.get(urlEqualTo("/users/${username}"))
                .inScenario("Rate Limiting")
                .whenScenarioStateIs("Started")
                .willReturn(aResponse()
                        .withStatus(429)
                        .withHeader("Content-Type", "application/json")
                        .withHeader("Retry-After", "1")
                        .withBody('{"message":"API rate limit exceeded"}'))
                .willSetStateTo("First Retry"))

        stubFor(WireMock.get(urlEqualTo("/users/${username}"))
                .inScenario("Rate Limiting")
                .whenScenarioStateIs("First Retry")
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(userPayload)))

        stubFor(WireMock.get(urlEqualTo("/users/${username}/repos"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(repoPayload)))

        when: "Calling the merged user endpoint"
        def result = mockMvc.perform(get("/api/users/${username}")
                .contentType(MediaType.APPLICATION_JSON))

        then: "Request should succeed after retry"
        result.andExpect(status().isOk())

        and: "GitHub user API was called multiple times due to retry"
        verify(moreThanOrExactly(2), getRequestedFor(urlEqualTo("/users/${username}")))
    }

    private String loadJsonFile(String path) {
        return getClass().getResourceAsStream(path).text
    }
}
