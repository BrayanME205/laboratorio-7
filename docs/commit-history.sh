#!/usr/bin/env bash
set -e

git init -b main
git config user.name >/dev/null || { echo "Configure git user.name and user.email first"; exit 1; }

git add .gitignore backend/pom.xml backend/lombok.config backend/application.properties.template \
    backend/src/main/resources backend/src/main/java/cr/ac/ucr/paraiso/ie/c5h753/expresofast/ExpresoFastApplication.java
git commit -m "chore: initialize Spring Boot project with Maven, JPA, Security and JJWT"

git add database
git commit -m "feat: add SQL Server schema, security extension and seed data scripts"

git add backend/src/main/java/cr/ac/ucr/paraiso/ie/c5h753/expresofast/domain \
    backend/src/main/java/cr/ac/ucr/paraiso/ie/c5h753/expresofast/repository
git commit -m "feat: add JPA entities, BitacoraEnvio and optimized JOIN FETCH repositories"

git add backend/src/main/java/cr/ac/ucr/paraiso/ie/c5h753/expresofast/dto
git commit -m "feat: add request and response DTOs with jakarta validation"

git add backend/src/main/java/cr/ac/ucr/paraiso/ie/c5h753/expresofast/exception
git commit -m "feat: add business exceptions and RFC 7807 global exception handler"

git add backend/src/main/java/cr/ac/ucr/paraiso/ie/c5h753/expresofast/security \
    backend/src/main/java/cr/ac/ucr/paraiso/ie/c5h753/expresofast/config
git commit -m "feat: implement JWT security, authentication filter and RBAC rules"

git add backend/src/main/java/cr/ac/ucr/paraiso/ie/c5h753/expresofast/service \
    backend/src/main/java/cr/ac/ucr/paraiso/ie/c5h753/expresofast/controller
git commit -m "feat: add business services, audit log and REST controllers"

git add frontend
git commit -m "feat: add authenticated frontend with role based views and audit log modal"

git add backend/src/test/java/cr/ac/ucr/paraiso/ie/c5h753/expresofast/service \
    backend/src/test/java/cr/ac/ucr/paraiso/ie/c5h753/expresofast/domain
git commit -m "test: add service unit tests and parameterized tariff tests with Mockito"

git add backend/src/test/java/cr/ac/ucr/paraiso/ie/c5h753/expresofast/controller \
    backend/src/test/java/cr/ac/ucr/paraiso/ie/c5h753/expresofast/security
git commit -m "test: add MockMvc controller slice tests and JWT provider tests"

git add docs README.md
git commit -m "docs: add Postman collection and README with setup instructions"

git log --oneline
