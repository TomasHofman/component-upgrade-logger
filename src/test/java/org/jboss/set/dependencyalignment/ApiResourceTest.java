package org.jboss.set.dependencyalignment;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.ws.rs.core.Response;
import org.assertj.core.groups.Tuple;
import org.jboss.set.dependencyalignment.domain.ComponentUpgrade;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
@QuarkusTestResource(PostgresResource.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ApiResourceTest {

    @TestHTTPResource("/")
    URL url;

    private final Jsonb jsonb = JsonbBuilder.create();

    @Test
    @Order(1)
    public void testCreate() throws Exception {
        ComponentUpgrade u1 = new ComponentUpgrade("unittest", "org.jboss", "lib1", "1.0.0", "1.0.1", null);
        ComponentUpgrade u2 = new ComponentUpgrade("unittest", "org.jboss", "lib2", "1.0.0", "1.0.1", null);
        // following duplicate record should not be stored
        ComponentUpgrade u3 = new ComponentUpgrade("unittest", "org.jboss", "lib2", "1.0.0", "1.0.1", null);
        List<ComponentUpgrade> payload = Arrays.asList(u1, u2, u3);

        // send request to create entities
        HttpResponse<?> response = post("/api/component-upgrades", payload);
        assertEquals(Response.Status.CREATED.getStatusCode(), response.statusCode());

        // repeat the request, no duplicates should be created
        response = post("/api/component-upgrades", payload);
        assertEquals(Response.Status.CREATED.getStatusCode(), response.statusCode());
    }

    @Test
    @Order(2)
    public void testGetAll() throws Exception {
        HttpResponse<String> response = get("/api/component-upgrades/unittest");
        assertEquals(Response.Status.OK.getStatusCode(), response.statusCode());
        ComponentUpgrade[] componentUpgrades = jsonb.fromJson(response.body(), ComponentUpgrade[].class);

        assertThat(componentUpgrades.length).isEqualTo(2);
        assertThat(componentUpgrades).extracting("project", "groupId", "artifactId")
                .containsOnly(
                        Tuple.tuple("unittest", "org.jboss", "lib1"),
                        Tuple.tuple("unittest", "org.jboss", "lib2")
                );
    }

    @Test
    @Order(3)
    public void testGetFirst() throws Exception {
        HttpResponse<String> response = get("/api/component-upgrades/unittest/org.jboss/lib1/1.0.1");
        assertEquals(Response.Status.OK.getStatusCode(), response.statusCode());

        ComponentUpgrade componentUpgrade = jsonb.fromJson(response.body(), ComponentUpgrade.class);
        assertEquals("unittest", componentUpgrade.project);
        assertEquals("org.jboss", componentUpgrade.groupId);
        assertEquals("lib1", componentUpgrade.artifactId);
    }

    private HttpResponse<String> post(String path, Object payload) throws Exception {
        String jsonPayload = jsonb.toJson(payload);

        HttpRequest.Builder builder = HttpRequest.newBuilder();
        HttpRequest request = builder.uri(url.toURI().resolve(path))
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .headers("Content-Type", "application/json")
                .build();
        HttpClient client = HttpClient.newHttpClient();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }


    private HttpResponse<String> get(String path) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder();
        HttpRequest request = builder.uri(url.toURI().resolve(path))
                .GET()
                .headers("Content-Type", "application/json")
                .build();
        HttpClient client = HttpClient.newHttpClient();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }
}