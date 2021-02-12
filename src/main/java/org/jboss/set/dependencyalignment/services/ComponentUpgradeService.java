package org.jboss.set.dependencyalignment.services;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowSet;
import io.vertx.mutiny.sqlclient.Tuple;
import org.apache.commons.lang3.StringUtils;
import org.jboss.set.dependencyalignment.domain.ComponentUpgrade;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.StreamSupport;

@ApplicationScoped
@Transactional
public class ComponentUpgradeService {

    @Inject
    PgPool client;

    public Multi<ComponentUpgrade> getAll(String project) {
        return client.preparedQuery("select * from component_upgrades where project = $1")
                .execute(Tuple.of(project))
                .onItem().transformToMulti(rows -> Multi.createFrom().items(() -> StreamSupport.stream(rows.spliterator(), false)))
                .onItem().transform(ComponentUpgradeService::createComponentUpgrade);

    }

    public Uni<ComponentUpgrade> getFirst(String project, String groupId, String artifactId, String newVersion) {
        return client.preparedQuery("select * from component_upgrades " +
                "where project = $1 and group_id = $2 and artifact_id = $3 and new_version = $4 " +
                "order by created asc")
                .execute(Tuple.of(project, groupId, artifactId, newVersion))
                .onItem().transform(RowSet::iterator)
                .onItem().transform(iterator -> iterator.hasNext() ? createComponentUpgrade(iterator.next()) : null);
    }

    public Uni<Void> save(List<ComponentUpgrade> items) {
        Multi<ComponentUpgrade> upgrades = Multi.createFrom().iterable(items)
                // validate items
                .onItem().invoke(ComponentUpgradeService::validateComponentUpgrade)
                // filter out already existing items
                .transform().byTestingItemsWith(item -> findExistingRecord(item).onItem().transform(b -> !b))
                .transform().byDroppingDuplicates();
        return saveRecords(upgrades);
    }

    Uni<Boolean> findExistingRecord(ComponentUpgrade item) {
        return client.preparedQuery("select * from component_upgrades " +
                "where project = $1 and group_id = $2 and artifact_id = $3 and old_version = $4 and new_version = $5")
                .execute(Tuple.of(item.project, item.groupId, item.artifactId, item.oldVersion, item.newVersion))
                .onItem().transform(rs -> rs.rowCount() > 0);
    }

    Uni<Void> saveRecords(Multi<ComponentUpgrade> records) {
        return records
                // convert ComponentUpgrade objects into Tuples
                .onItem().transform(i -> Tuple.of(i.project, i.groupId, i.artifactId, i.oldVersion, i.newVersion))
                // convert into uni containing the whole list
                .collectItems().asList()
                // batch insert
                .onItem().call(items
                        -> client.preparedQuery("insert into component_upgrades"
                        + " (project, group_id, artifact_id, old_version, new_version) values ($1, $2, $3, $4, $5)")
                        .executeBatch(items))
                .map(rowSetUni -> null);
    }

    private static ComponentUpgrade createComponentUpgrade(Row row) {
        return new ComponentUpgrade(
                row.getString("project"),
                row.getString("group_id"),
                row.getString("artifact_id"),
                row.getString("old_version"),
                row.getString("new_version"),
                row.getLocalDateTime("created")
        );
    }

    private static void validateComponentUpgrade(ComponentUpgrade item) {
        if (StringUtils.isBlank(item.project) || StringUtils.isBlank(item.groupId)
                || StringUtils.isBlank(item.artifactId) || StringUtils.isBlank(item.oldVersion)
                || StringUtils.isBlank(item.newVersion)) {
            throw new IllegalArgumentException("Fields [project, groupId, artifactId, oldVersion, newVersion] must not be empty.");
        }
    }
}
