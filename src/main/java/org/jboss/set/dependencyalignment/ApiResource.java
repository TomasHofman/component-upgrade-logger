package org.jboss.set.dependencyalignment;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.Json;
import io.vertx.mutiny.pgclient.PgPool;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.resteasy.annotations.jaxrs.PathParam;
import org.jboss.set.dependencyalignment.domain.ComponentUpgrade;
import org.jboss.set.dependencyalignment.domain.ValidationErrorResponse;
import org.jboss.set.dependencyalignment.services.ComponentUpgradeService;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@Path("/api")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class ApiResource {

    @Inject
    PgPool client;

    @Inject
    ComponentUpgradeService componentUpgradeService;

    @Inject
    @ConfigProperty(name = "myapp.schema.create", defaultValue = "true")
    boolean schemaCreate;


    @PostConstruct
    void config() {
        if (schemaCreate) {
            initdb();
        }
    }

    private void initdb() {
        try {
            URL resource = getClass().getResource("/init.sql");
            String sql = Files.readString(Paths.get(resource.toURI()));
            String[] statements = sql.split(";\\r?\\n");
            for (String statement : statements) {
                if (StringUtils.isNotBlank(statement)) {
                    client.query(statement).execute();
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Can't initialize database.", e);
        }
    }

    @GET
    @Path("/component-upgrades/{project}")
    public Multi<ComponentUpgrade> getAll(@PathParam String project) {
        return componentUpgradeService.getAll(project);
    }

    @GET
    @Path("/component-upgrades/{project}/{groupId}/{artifactId}/{newVersion}")
    public Uni<Response> getFirst(@PathParam String project, @PathParam String groupId,
                                  @PathParam String artifactId, @PathParam String newVersion) {
        return componentUpgradeService.getFirst(project, groupId, artifactId, newVersion)
                .onItem().transform(item -> item != null ? Response.ok(item) : Response.status(Status.NOT_FOUND))
                .onItem().transform(ResponseBuilder::build);
    }

    @POST
    @Path("/component-upgrades/")
    public Uni<Response> create(List<ComponentUpgrade> componentUpgrades) {
        return componentUpgradeService.save(componentUpgrades)
                .onItem().transform(
                        res -> Response.status(Status.CREATED).build())
                .onFailure().recoverWithItem(
                        e -> Response.status(Status.BAD_REQUEST)
                                .entity(Json.encodePrettily(new ValidationErrorResponse(e.getMessage())))
                                .build());
    }
}