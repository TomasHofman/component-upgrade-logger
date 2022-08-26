package org.jboss.set.dependencyalignment.domain;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.sql.Timestamp;
import java.time.LocalDateTime;

public class ComponentUpgrade {

    public String project;
    public String groupId;
    public String artifactId;
    public String oldVersion;
    public String newVersion;
    public LocalDateTime created;

    @SuppressWarnings("unused")
    public ComponentUpgrade() {
    }

    public ComponentUpgrade(String project, String groupId, String artifactId, String oldVersion, String newVersion, Timestamp created) {
        this.project = project;
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.oldVersion = oldVersion;
        this.newVersion = newVersion;
        if (created != null) {
            this.created = created.toLocalDateTime();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        ComponentUpgrade that = (ComponentUpgrade) o;

        return new EqualsBuilder()
                .append(project, that.project)
                .append(groupId, that.groupId)
                .append(artifactId, that.artifactId)
                .append(oldVersion, that.oldVersion)
                .append(newVersion, that.newVersion)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(project)
                .append(groupId)
                .append(artifactId)
                .append(oldVersion)
                .append(newVersion)
                .toHashCode();
    }
}
