package org.jboss.set.dependencyalignment;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import org.jboss.resteasy.annotations.jaxrs.PathParam;
import org.jboss.set.dependencyalignment.domain.ComponentUpgrade;
import org.jboss.set.dependencyalignment.services.ComponentUpgradeService;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.sql.DataSource;
import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;

@Path("/api")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class ApiResource {

    private final Logger logger = Logger.getLogger(this.getClass());

    @Inject
    DataSource dataSource;

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
        try (Connection connection = dataSource.getConnection()){
            URL resource = getClass().getResource("/init.sql");
            String sql = Files.readString(Paths.get(resource.toURI()));
            String[] statements = sql.split(";\\r?\\n");
            for (String statement : statements) {
                if (StringUtils.isNotBlank(statement)) {
                    PreparedStatement preparedStatement = connection.prepareStatement(statement);
                    preparedStatement.execute();
                    preparedStatement.close();
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Can't initialize database.", e);
        }
    }

    @GET
    @Path("/component-upgrades/{project}")
    public List<ComponentUpgrade> getAll(@PathParam String project) {
        return componentUpgradeService.getAll(project);
    }

    @GET
    @Path("/component-upgrades/{project}/{groupId}/{artifactId}/{newVersion}")
    public Response getFirst(@PathParam String project, @PathParam String groupId,
                                  @PathParam String artifactId, @PathParam String newVersion) {
        ComponentUpgrade first = componentUpgradeService.getFirst(project, groupId, artifactId, newVersion);
        if (first == null) {
            return Response.status(Status.NOT_FOUND).build();
        } else {
            return Response.ok(first).build();
        }
    }

    @POST
    @Path("/component-upgrades/")
    public Response create(List<ComponentUpgrade> componentUpgrades) {
        componentUpgradeService.save(componentUpgrades);
        return Response.status(Status.CREATED).build();
    }
}