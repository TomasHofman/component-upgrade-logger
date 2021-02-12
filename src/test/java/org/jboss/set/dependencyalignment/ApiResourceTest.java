package org.jboss.set.dependencyalignment;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.assertj.core.groups.Tuple;
import org.jboss.set.dependencyalignment.domain.ComponentUpgrade;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;

@QuarkusTest
@QuarkusTestResource(PostgresResource.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ApiResourceTest {

    @Test
    @Order(1)
    public void testCreate() {
        ComponentUpgrade u1 = new ComponentUpgrade("unittest", "org.jboss", "lib1", "1.0.0", "1.0.1", null);
        ComponentUpgrade u2 = new ComponentUpgrade("unittest", "org.jboss", "lib2", "1.0.0", "1.0.1", null);
        // following duplicate record should not be stored
        ComponentUpgrade u3 = new ComponentUpgrade("unittest", "org.jboss", "lib2", "1.0.0", "1.0.1", null);
        List<ComponentUpgrade> payload = Arrays.asList(u1, u2, u3);

        // send request to create entities
        given().contentType(ContentType.JSON)
                .when()
                .body(payload).post("/api/component-upgrades")
                .then()
                .statusCode(Response.Status.CREATED.getStatusCode());

        // repeat the request, no duplicates should be created
        given().contentType(ContentType.JSON)
                .when()
                .body(payload).post("/api/component-upgrades")
                .then()
                .statusCode(Response.Status.CREATED.getStatusCode());
    }

    @Test
    @Order(2)
    public void testGetAll() {
        ComponentUpgrade[] componentUpgrades = given()
                .contentType(ContentType.JSON)
                .when().get("/api/component-upgrades/unittest")
                .then()
                .statusCode(200)
                .extract().as(ComponentUpgrade[].class);
        assertThat(componentUpgrades.length).isEqualTo(2);
        assertThat(componentUpgrades).extracting("project", "groupId", "artifactId")
                .containsOnly(
                        Tuple.tuple("unittest", "org.jboss", "lib1"),
                        Tuple.tuple("unittest", "org.jboss", "lib2")
                );
    }

    @Test
    @Order(3)
    public void testGetFirst() {
        given()
                .contentType(ContentType.JSON)
                .when().get("/api/component-upgrades/unittest/org.jboss/lib1/1.0.1")
                .then()
                .statusCode(200)
                .body("project", is("unittest"))
                .body("groupId", is("org.jboss"))
                .body("artifactId", is("lib1"));
    }

}