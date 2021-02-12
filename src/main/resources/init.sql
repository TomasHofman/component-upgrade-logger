drop table if exists component_upgrades;

create table component_upgrades (
    id serial primary key,
    created timestamp not null default now(),
    project varchar(255) not null,
    group_id varchar(255) not null,
    artifact_id varchar(255) not null,
    old_version varchar(255) not null,
    new_version varchar(255) not null
);

create index component_upgrades_project_idx on component_upgrades (project);

create index component_upgrades_idx on component_upgrades (group_id, artifact_id, new_version);
