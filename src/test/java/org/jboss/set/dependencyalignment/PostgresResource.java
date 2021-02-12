package org.jboss.set.dependencyalignment;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.Map;

public class PostgresResource implements QuarkusTestResourceLifecycleManager {

    private static final String CONTAINER_NAME = "postgres:12.1";
    private static final String QUARKUS_TEST = "quarkus_test";

    PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(CONTAINER_NAME);

    @Override
    public Map<String, String> start() {
        try {
            postgres.withDatabaseName(QUARKUS_TEST)
                    .withUsername(QUARKUS_TEST)
                    .withPassword(QUARKUS_TEST)
                    .withInitScript("/init.sql");
            postgres.getPortBindings().add("5431:5432/tcp");
            postgres.start();

            return null;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public void stop() {
        postgres.stop();
        postgres.close();
    }
}
