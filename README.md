# component-upgrade-logger project

Component Upgrade Logger is a simple REST service for recording and querying dependencies discovered by 
the [Maven Dependency Updater](https://github.com/jboss-set/maven-dependency-updater).

## Development

In order to run the application locally:

* prepare a PostgreSQL database and initialize it with the `src/main/resources/init.sql` script,
* update database connection parameters in the `src/main/resources/application.properties` file.

An easy way to get a PostgreSQL database running is to create a docker container:

```shell script
# run a container
docker run -d --name component-upgrade-logger-postgres \
  -p 5432:5432 \
  -e POSTGRES_DB=quarkus_test \
  -e POSTGRES_USER=quarkus_test \
  -e POSTGRES_PASSWORD=quarkus_test \
  postgres:latest

# create tables
psql -h localhost -U quarkus_test < src/main/resources/init.sql
```

Next time you need the container again, you can start it with:

```shell script
docker start component-upgrade-logger-postgres
```

and stop it with:

```shell script
docker stop component-upgrade-logger-postgres
```

### Development with Podman

This project uses the [Testcontainers](https://www.testcontainers.org/) library to start a containerized database
during the Maven test goal.
With Docker installed on the system, this should work out of the box. In order to use Podman rather than Docker,
use the following workaround:

```
export TESTCONTAINERS_RYUK_DISABLED=true
export DOCKER_HOST="unix:/run/user/$(id -u)/podman/podman.sock"
# start podman service for one hour
podman system service -t 3600 &

./mvnw test
```

## Quarkus README

This project uses Quarkus, the Supersonic Subatomic Java Framework.

If you want to learn more about Quarkus, please visit its website: https://quarkus.io/ .

### Running the application in dev mode

You can run your application in dev mode that enables live coding using:
```
./mvnw quarkus:dev
```

### Packaging and running the application

The application can be packaged using `./mvnw package`.
It produces the `component-upgrade-logger-1.0.0-SNAPSHOT-runner.jar` file in the `/target` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `target/lib` directory.

The application is now runnable using `java -jar target/component-upgrade-logger-1.0.0-SNAPSHOT-runner.jar`.

### Creating a native executable

You can create a native executable using: `./mvnw package -Pnative`.

Or, if you don't have GraalVM installed, you can run the native executable build in a container using: `./mvnw package -Pnative -Dquarkus.native.container-build=true`.

You can then execute your native executable with: `./target/component-upgrade-logger-1.0.0-SNAPSHOT-runner`

If you want to learn more about building native executables, please consult https://quarkus.io/guides/building-native-image.

### Tekton Pipeline

See the [pipeline/README.md](pipeline/README.md). 