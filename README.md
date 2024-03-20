# deepmooc-backend

## Requirements

- JDK 21
- Docker

## Setup

The easiest way to run the application is by using docker compose:

1. Build the application `./gradlew shadowJar`
2. Run using `docker compose up`

To use authentication a SAML IdP needs to be configured as well.

## Documentation

Swagger UI can be found after starting the application at path `/swagger`.
