# deepmooc-backend

## Requirements

- JDK 11
- Gradle
- PostgreSQL

## Setup

1. Database
   1. Create a local PostgreSQL database
   2. Run `db/create_tables.sql` script to create tables and keys
2. Application
   1. Configure application to use created database in `conf/application.conf`
   2. (optional) Configure new keystore and SAML IdP
   3. Build using `./gradlew build`
   4. Run using `./gradlew joobyRun`

## Documentation

Swagger UI can be found after starting the application at path `/swagger`.
