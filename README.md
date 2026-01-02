# Branch App Demo

A Spring Boot REST API service that integrates with GitHub's public API to fetch and merge user and repository data into a simplified, unified response format.

## Table of Contents

- [Overview](#overview)
- [Architecture](#architecture)
- [Key Decisions](#key-decisions)
- [Prerequisites](#prerequisites)
- [Installation](#installation)
- [Running the Application](#running-the-application)
- [API Documentation](#api-documentation)
- [Testing](#testing)
- [Configuration](#configuration)
- [Project Structure](#project-structure)

## Overview

This service provides a REST endpoint that aggregates GitHub user profile data with their repository information, transforming the response into a cleaner, more focused format. The service handles rate limiting with automatic retries and exponential backoff.

### Key Features

- **Unified GitHub Data Endpoint**: Single API call to fetch merged user and repository data
- **Intelligent Rate Limiting**: Automatic retry mechanism with exponential backoff
- **Type-Safe Mapping**: MapStruct for compile-time safe transformations
- **Declarative HTTP Client**: OpenFeign for simplified REST communication
- **Comprehensive Testing**: Integration and contract tests using Spock and WireMock
- **Production-Ready**: Actuator endpoints for health monitoring

## Architecture

### High-Level Architecture

```
┌─────────────┐       ┌──────────────────┐       ┌────────────────┐
│   Client    │──────▶│  REST Controller │──────▶│    Service     │
└─────────────┘       └──────────────────┘       └────────────────┘
                                                           │
                                                           ▼
                      ┌──────────────────┐       ┌────────────────┐
                      │   MapStruct      │◀──────│  Feign Client  │
                      │    Mapper        │       └────────────────┘
                      └──────────────────┘               │
                               │                         │
                               ▼                         ▼
                      ┌──────────────────────────────────────────┐
                      │         GitHub API                       │
                      │  • GET /users/{username}                 │
                      │  • GET /users/{username}/repos           │
                      └──────────────────────────────────────────┘
```

### Component Responsibilities

#### 1. **REST Controller** (`GitHubController`)
- Exposes the public API endpoint: `GET /api/users/{username}`
- Handles HTTP request/response lifecycle
- Delegates business logic to the service layer

#### 2. **Service Layer** (`GitHubService`)
- Orchestrates calls to GitHub API via Feign client
- Coordinates the mapping of responses using MapStruct
- Business logic encapsulation

#### 3. **Feign Client** (`GitHubClient`)
- Declarative REST client for GitHub API
- Handles HTTP communication with retry logic
- Configurable base URL for testing

#### 4. **MapStruct Mapper** (`GitHubMapper`)
- Type-safe, compile-time mapping between DTOs
- Field transformation (e.g., `login` → `user_name`)
- Custom date formatting (ISO 8601 → RFC 1123)

#### 5. **DTOs** (Data Transfer Objects)
- **GitHubUserResponse**: Raw GitHub user API response
- **GitHubRepoResponse**: Raw GitHub repository API response
- **UserRepoResponse**: Merged, simplified response format
- **RepoInfo**: Minimal repository information (name, url)

#### 6. **Configuration**
- **GitHubFeignConfiguration**: Retry logic with exponential backoff
- **GitHubRetryProperties**: Externalized retry configuration

## Key Decisions

### 1. **Technology Stack**

| Technology | Rationale |
|------------|-----------|
| **Spring Boot 3.4.1** | Latest stable version with Jakarta EE support, production-ready features |
| **OpenFeign** | Declarative HTTP client simplifies REST communication and reduces boilerplate |
| **MapStruct 1.6.3** | Compile-time code generation ensures type safety and performance |
| **Lombok** | Reduces boilerplate for DTOs and models |
| **Spock + Groovy** | Expressive BDD-style testing with excellent readability |
| **WireMock** | Reliable HTTP mocking for integration tests |

### 2. **Architectural Patterns**

**Layered Architecture**
- Clear separation of concerns (Controller → Service → Client)
- Each layer has a single, well-defined responsibility
- Easier to test, maintain, and extend

**DTO Pattern**
- Separates internal models from external API contracts
- Allows API evolution without breaking internal logic
- MapStruct handles transformations efficiently

**Configuration Externalization**
- All tunable parameters in `application.yml`
- Environment-specific overrides supported
- Easy to adjust without code changes

### 3. **Retry Strategy**

Implemented Feign's built-in `Retryer.Default` with exponential backoff:

- **Initial Interval**: 1 second
- **Max Interval**: 10 seconds (prevents excessive waiting)
- **Multiplier**: 2.0 (doubles wait time after each retry)
- **Max Attempts**: 5 retries

**Why this approach?**
- GitHub API has rate limits (60 requests/hour for unauthenticated)
- Exponential backoff is industry best practice for rate limiting
- Configurable values allow tuning based on actual API behavior
- Feign's built-in retry is production-tested and reliable

### 4. **Date Formatting**

GitHub returns dates in ISO 8601 format (`2011-01-25T18:44:36Z`), but the spec requires RFC 1123 format (`Tue, 25 Jan 2011 18:44:36 GMT`).

**Implementation:**
- Custom MapStruct method converts between formats
- Explicitly converts to GMT timezone
- Uses Java 8 Time API for reliable date handling

### 5. **Field Ordering**

JSON field order is typically not guaranteed, but the spec shows a specific order. Used Jackson's `@JsonPropertyOrder` annotation to ensure consistent serialization.

**Why this matters:**
- Consistent API responses improve debugging
- Easier to diff responses in tests
- More predictable for client consumers

### 6. **Testing Strategy**

**Two-tier testing approach:**

1. **Integration Tests** (`GitHubControllerSpec`)
   - Full Spring context with WireMock
   - Tests end-to-end flow with mocked GitHub API
   - Validates exact JSON response matching
   - Tests retry behavior

2. **Contract Tests** (`GitHubControllerContractSpec`)
   - Fast unit tests with mocked service
   - Validates API contract (endpoints, methods, structure)
   - Tests field presence, types, and ordering
   - No external dependencies

## Prerequisites

- **Java 21** or higher
- **Gradle 8.11.1** (wrapper included)
- **Git** (for cloning the repository)

## Installation

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd branch-app-demo
   ```

2. **Build the project**
   ```bash
   ./gradlew clean build
   ```

   This will:
   - Compile the application
   - Generate MapStruct implementation classes
   - Run all tests (Spock + integration tests)
   - Create executable JAR in `build/libs/`

## Running the Application

### Option 1: Using Gradle

```bash
./gradlew bootRun
```

### Option 2: Using the JAR

```bash
java -jar build/libs/branch-app-demo-0.0.1-SNAPSHOT.jar
```

### Option 3: With Custom Configuration

```bash
java -jar build/libs/branch-app-demo-0.0.1-SNAPSHOT.jar \
  --github.retry.max-attempts=3 \
  --github.retry.initial-interval-ms=500
```

The application will start on **port 8080** by default.

### Verify the application is running

```bash
# Health check
curl http://localhost:8080/actuator/health

# Expected response
{"status":"UP"}
```

## API Documentation

### Endpoint: Get User with Repositories

Fetches GitHub user profile and repository data, returning a merged, simplified response.

#### Request

```http
GET /api/users/{username}
```

**Path Parameters:**
- `username` (string, required): GitHub username

**Example:**
```bash
curl http://localhost:8080/api/users/octocat
```

#### Response

**Status Code:** `200 OK`

**Content-Type:** `application/json`

**Response Body:**
```json
{
  "user_name": "octocat",
  "display_name": "The Octocat",
  "avatar": "https://avatars.githubusercontent.com/u/583231?v=4",
  "geo_location": "San Francisco",
  "email": null,
  "url": "https://api.github.com/users/octocat",
  "created_at": "Tue, 25 Jan 2011 18:44:36 GMT",
  "repos": [
    {
      "name": "Hello-World",
      "url": "https://api.github.com/repos/octocat/Hello-World"
    },
    {
      "name": "Spoon-Knife",
      "url": "https://api.github.com/repos/octocat/Spoon-Knife"
    }
  ]
}
```

#### Field Descriptions

| Field | Type | Description |
|-------|------|-------------|
| `user_name` | string | GitHub username (login) |
| `display_name` | string | User's full/display name |
| `avatar` | string | URL to user's avatar image |
| `geo_location` | string | User's location (can be null) |
| `email` | string | User's public email (can be null) |
| `url` | string | GitHub API URL for the user |
| `created_at` | string | Account creation date in RFC 1123 format |
| `repos` | array | Array of repository objects |
| `repos[].name` | string | Repository name |
| `repos[].url` | string | GitHub API URL for the repository |

#### Error Responses

**User Not Found**
```http
HTTP/1.1 404 Not Found
Content-Type: application/json

{
  "timestamp": "2026-01-02T20:30:00.000+00:00",
  "status": 404,
  "error": "Not Found",
  "path": "/api/users/nonexistentuser"
}
```

**Rate Limit Exceeded (after retries)**
```http
HTTP/1.1 429 Too Many Requests
Content-Type: application/json

{
  "timestamp": "2026-01-02T20:30:00.000+00:00",
  "status": 429,
  "error": "Too Many Requests",
  "path": "/api/users/octocat"
}
```

## Testing

### Run All Tests

```bash
./gradlew test
```

### Run Specific Test Suite

```bash
# Integration tests
./gradlew test --tests "GitHubControllerSpec"

# Contract tests
./gradlew test --tests "GitHubControllerContractSpec"
```

### View Test Reports

After running tests, open the HTML report:

```bash
open build/reports/tests/test/index.html
```

### Test Coverage

The project includes:
- **2 Integration Tests**: Full end-to-end testing with WireMock
- **9 Contract Tests**: API contract validation
- **Test Data**: Sample JSON payloads in `src/test/resources/contracts/`

## Configuration

### Application Properties

**File:** `src/main/resources/application.yml`

```yaml
# Server Configuration
server:
  port: 8080

# GitHub API Configuration
github:
  api:
    url: https://api.github.com
  retry:
    max-attempts: 5           # Maximum retry attempts
    initial-interval-ms: 1000 # Initial wait time (1 second)
    max-interval-ms: 10000    # Maximum wait time (10 seconds)
    multiplier: 2.0           # Exponential backoff multiplier

# Feign Client Configuration
feign:
  client:
    config:
      default:
        connectTimeout: 5000
        readTimeout: 5000
        loggerLevel: basic
      github-client:
        connectTimeout: 5000
        readTimeout: 10000
        loggerLevel: full
```

### Environment-Specific Configuration

Create `application-{profile}.yml` files for different environments:

**Development** (`application-dev.yml`):
```yaml
github:
  retry:
    max-attempts: 3
    initial-interval-ms: 500
```

**Production** (`application-prod.yml`):
```yaml
github:
  retry:
    max-attempts: 5
    initial-interval-ms: 2000
```

Run with specific profile:
```bash
java -jar build/libs/branch-app-demo-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

### Configurable Properties

| Property | Default | Description |
|----------|---------|-------------|
| `server.port` | 8080 | HTTP server port |
| `github.api.url` | https://api.github.com | GitHub API base URL |
| `github.retry.max-attempts` | 5 | Maximum retry attempts |
| `github.retry.initial-interval-ms` | 1000 | Initial retry delay (ms) |
| `github.retry.max-interval-ms` | 10000 | Maximum retry delay (ms) |
| `github.retry.multiplier` | 2.0 | Exponential backoff multiplier |

## Project Structure

```
branch-app-demo/
├── src/
│   ├── main/
│   │   ├── java/com/example/demo/
│   │   │   ├── DemoApplication.java              # Spring Boot application entry point
│   │   │   ├── client/
│   │   │   │   └── GitHubClient.java             # Feign client for GitHub API
│   │   │   ├── config/
│   │   │   │   ├── GitHubFeignConfiguration.java # Retry configuration
│   │   │   │   └── GitHubRetryProperties.java    # Externalized retry properties
│   │   │   ├── controller/
│   │   │   │   └── GitHubController.java         # REST endpoint controller
│   │   │   ├── dto/
│   │   │   │   ├── GitHubRepoResponse.java       # GitHub repo API DTO
│   │   │   │   ├── GitHubUserResponse.java       # GitHub user API DTO
│   │   │   │   ├── RepoInfo.java                 # Simplified repo DTO
│   │   │   │   └── UserRepoResponse.java         # Merged response DTO
│   │   │   ├── mapper/
│   │   │   │   └── GitHubMapper.java             # MapStruct mapper interface
│   │   │   └── service/
│   │   │       └── GitHubService.java            # Business logic service
│   │   └── resources/
│   │       └── application.yml                   # Main configuration file
│   └── test/
│       ├── groovy/com/example/demo/controller/
│       │   ├── GitHubControllerSpec.groovy       # Integration tests
│       │   └── GitHubControllerContractSpec.groovy # Contract tests
│       └── resources/
│           ├── application-test.yml              # Test configuration
│           └── contracts/                        # Test data files
│               ├── merged-payload.json
│               ├── repo-payload.json
│               ├── repo-payload-single.json
│               └── user-payload.json
├── build.gradle.kts                              # Gradle build configuration
├── settings.gradle.kts                           # Gradle settings
└── README.md                                     # This file
```

## Development

### Adding New Features

1. **Add new endpoint**
   - Create controller method in `GitHubController`
   - Add corresponding service method
   - Write contract and integration tests

2. **Modify response structure**
   - Update DTOs in `dto/` package
   - Update MapStruct mappings in `GitHubMapper`
   - Update test expectations

3. **Change GitHub API integration**
   - Modify `GitHubClient` interface
   - Update DTOs to match new API contract
   - Adjust mapper accordingly

### Code Quality

**Build with all checks:**
```bash
./gradlew clean build
```

**Run tests only:**
```bash
./gradlew test
```

**Check dependencies:**
```bash
./gradlew dependencies
```

## Troubleshooting

### Issue: GitHub API Rate Limiting

**Symptom:** 429 Too Many Requests errors

**Solution:**
1. Reduce `github.retry.max-attempts` temporarily
2. Increase `github.retry.initial-interval-ms`
3. Consider adding GitHub API token for authenticated requests (60/hour → 5000/hour)

### Issue: Tests Failing

**Symptom:** WireMock tests fail with connection errors

**Solution:**
1. Ensure no other service is using port 0 (random)
2. Run tests in isolation: `./gradlew clean test`
3. Check test logs in `build/reports/tests/test/`

### Issue: MapStruct Not Generating Code

**Symptom:** Compilation errors about missing mapper implementation

**Solution:**
1. Run `./gradlew clean build` (not just compile)
2. Check `build/generated/sources/annotationProcessor/` for generated code
3. Ensure Lombok and MapStruct annotation processors are configured correctly

## Future Enhancements

Potential improvements for production deployment:

1. **Authentication**: Add support for GitHub API tokens to increase rate limits
2. **Caching**: Implement Redis/Caffeine caching to reduce API calls
3. **Pagination**: Handle large repository lists with pagination
4. **Error Handling**: More granular exception handling and custom error responses
5. **Monitoring**: Add Micrometer metrics for observability
6. **API Documentation**: Add OpenAPI/Swagger documentation
7. **Circuit Breaker**: Implement Resilience4j circuit breaker for fault tolerance
8. **Database**: Cache responses in database for offline capability

## License

This project is created for demonstration purposes.

## Contact

For questions or issues, please contact the development team.
