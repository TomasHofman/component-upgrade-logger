package org.jboss.set.dependencyalignment.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.sql.DataSource;
import javax.transaction.Transactional;

import org.apache.commons.lang3.StringUtils;
import org.jboss.logging.Logger;
import org.jboss.set.dependencyalignment.domain.ComponentUpgrade;

@ApplicationScoped
@Transactional
public class ComponentUpgradeService {

    private final Logger logger = Logger.getLogger(getClass());

    @Inject
    DataSource dataSource;

    public List<ComponentUpgrade> getAll(String project) {
        ArrayList<ComponentUpgrade> result = new ArrayList<>();
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            connection = dataSource.getConnection();
            preparedStatement = connection.prepareStatement("select * from component_upgrades where project = ?");
            preparedStatement.setString(1, project);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                result.add(createComponentUpgrade(resultSet));
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            safeClose(preparedStatement);
            safeClose(resultSet);
            safeClose(connection);
        }
    }

    public ComponentUpgrade getFirst(String project, String groupId, String artifactId, String newVersion) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            connection = dataSource.getConnection();
            preparedStatement = connection.prepareStatement("select * from component_upgrades " +
                            "where project = ? and group_id = ? and artifact_id = ? and new_version = ? " +
                            "order by created asc");
            preparedStatement.setString(1, project);
            preparedStatement.setString(2, groupId);
            preparedStatement.setString(3, artifactId);
            preparedStatement.setString(4, newVersion);
            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return createComponentUpgrade(resultSet);
            } else {
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            safeClose(preparedStatement);
            safeClose(resultSet);
            safeClose(connection);
        }
    }

    public void save(List<ComponentUpgrade> items) {
        // validate items
        items.forEach(ComponentUpgradeService::validateComponentUpgrade);
        List<ComponentUpgrade> filteredItems = items.stream().filter(item -> !findExistingRecord(item))
                .distinct()
                .collect(Collectors.toList());

        saveRecords(filteredItems);
    }

    boolean findExistingRecord(ComponentUpgrade item) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            connection = dataSource.getConnection();
            preparedStatement = connection.prepareStatement("select * from component_upgrades " +
                            "where project = ? and group_id = ? and artifact_id = ? and old_version = ? and new_version = ?");
            preparedStatement.setString(1, item.project);
            preparedStatement.setString(2, item.groupId);
            preparedStatement.setString(3, item.artifactId);
            preparedStatement.setString(4, item.oldVersion);
            preparedStatement.setString(5, item.newVersion);
            resultSet = preparedStatement.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            safeClose(resultSet);
            safeClose(preparedStatement);
            safeClose(connection);
        }
    }

    void saveRecords(List<ComponentUpgrade> records) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try {
            connection = dataSource.getConnection();
            preparedStatement = connection.prepareStatement("insert into component_upgrades"
                            + " (project, group_id, artifact_id, old_version, new_version) values (?, ?, ?, ?, ?)");

            for (ComponentUpgrade record: records) {
                preparedStatement.setString(1, record.project);
                preparedStatement.setString(2, record.groupId);
                preparedStatement.setString(3, record.artifactId);
                preparedStatement.setString(4, record.oldVersion);
                preparedStatement.setString(5, record.newVersion);
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            safeClose(preparedStatement);
            safeClose(connection);
        }
    }

    private static ComponentUpgrade createComponentUpgrade(ResultSet resultSet) throws SQLException {
        return new ComponentUpgrade(
                resultSet.getString("project"),
                resultSet.getString("group_id"),
                resultSet.getString("artifact_id"),
                resultSet.getString("old_version"),
                resultSet.getString("new_version"),
                resultSet.getTimestamp("created")
        );
    }

    private static void validateComponentUpgrade(ComponentUpgrade item) {
        if (StringUtils.isBlank(item.project) || StringUtils.isBlank(item.groupId)
                || StringUtils.isBlank(item.artifactId) || StringUtils.isBlank(item.oldVersion)
                || StringUtils.isBlank(item.newVersion)) {
            throw new IllegalArgumentException("Fields [project, groupId, artifactId, oldVersion, newVersion] must not be empty.");
        }
    }

    private static void safeClose(AutoCloseable resource) {
        if (resource != null) {
            try {
                resource.close();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
