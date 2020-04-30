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
