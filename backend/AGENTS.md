# Repository Guidelines

## Project Structure & Module Organization

This backend uses Java 21, Spring Boot 3.5.6, and Maven. Application code lives in `src/main/java/com/purohitcommunity/portal/`:

- `web/`: REST controllers, request/response DTOs, and exception handling.
- `service/`: membership, registration, member ID, and QR code logic.
- `domain/` and `repository/`: JPA entities and persistence interfaces.
- `security/`, `config/`, and `storage/`: JWT authentication, application setup, and private file storage.

Configuration is in `src/main/resources/application*.yml`. Tests mirror the application package under `src/test/java/`, with test configuration in `src/test/resources/`. `target/` contains generated build output; `uploads/private/` contains runtime documents, not source assets.

## Build, Test, and Development Commands

Run commands from this directory with JDK 21 and Maven installed; no Maven wrapper is included.

- `mvn spring-boot:run`: start the API on port 8080 by default, using the `local` profile and MySQL.
- `mvn test`: run unit tests and available integration tests.
- `mvn "-Dtest=MemberIdServiceTest" test`: run the member ID unit tests only.
- `mvn clean verify`: rebuild, run tests, and package the application.
- `java -jar target/purohit-portal-api-0.0.1-SNAPSHOT.jar`: run the packaged API.

## Coding Style & Naming Conventions

Follow surrounding formatting: main Java sources commonly use four-space indentation; YAML and XML use two spaces. Use `PascalCase` for classes, `camelCase` for methods and fields, and `UPPER_SNAKE_CASE` for constants. Retain descriptive suffixes such as `Controller`, `Service`, and `Repository`. Keep business logic in services and persistence access in repositories. No formatter or lint plugin is configured in `pom.xml`.

## Testing Guidelines

Use JUnit 5 through Spring Boot Test. Name test classes `*Test` and methods after the behavior being checked. Add regression tests for changed business rules. The MySQL Testcontainers smoke test uses Docker and skips when Docker is unavailable; verify it runs for persistence changes. No coverage threshold is configured.

## Commit & Pull Request Guidelines

Git history is unavailable in this checkout, so existing commit conventions cannot be verified. Use concise, imperative messages, such as `Fix district normalization`. PRs should explain the change, link relevant issues, report test results and skipped tests, and document API or configuration changes.

## Security & Configuration Tips

Configure database access through `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD`. Set production values for `JWT_SECRET`, `APP_ADMIN_PASSWORD`, and `CORS_ALLOWED_ORIGINS`; never commit credentials or uploaded identity documents. The `prod` profile validates the database schema, so prepare schema changes before deployment.
