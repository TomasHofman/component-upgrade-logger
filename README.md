# component-upgrade-logger project

Component Upgrade Logger is a simple REST service for recording and querying dependencies discovered by 
the Maven Dependency Updator (TODO link).

## Running tests with Podman

Project uses `testcontainers` to start a containerized database during tests. With Docker installed on the system, this
should work out of the box. In order to use Podman rather than Docker, do the following:

```
export TESTCONTAINERS_RYUK_DISABLED=true
export DOCKER_HOST="unix:/run/user/$(id -u)/podman/podman.sock"
# start podman service for one hour
podman system service -t 3600

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