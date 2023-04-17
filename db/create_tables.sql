create table if not exists public.users
(
    id       bigint generated always as identity
        primary key,
    username varchar(20) not null
        unique
);

alter table public.users
    owner to postgres;

create table if not exists public.courses
(
    id   bigint generated always as identity
        primary key,
    name varchar(50) not null
        unique,
    code varchar(50) not null
        unique
);

alter table public.courses
    owner to postgres;

create table if not exists public.course_registrations
(
    id           bigint generated always as identity
        constraint registrations_pkey
            primary key,
    user_id      bigint      not null
        constraint registration_user_fkey
            references public.users
            on update cascade on delete restrict,
    course_id    bigint      not null
        constraint registration_course_fkey
            references public.courses
            on update cascade on delete restrict,
    access_level varchar(50) not null,
    unique (user_id, course_id)
);

alter table public.course_registrations
    owner to postgres;

create table if not exists public.groups
(
    id        bigint generated always as identity
        constraint course_groups_pkey
            primary key,
    course_id bigint      not null
        constraint groups_course_fkey
            references public.courses
            on update cascade on delete restrict,
    name      varchar(50) not null,
    constraint course_groups_name_course_unique
        unique (course_id, name),
    unique (course_id, name)
);

alter table public.groups
    owner to postgres;

create table if not exists public.group_registrations
(
    id                     bigint generated always as identity
        constraint course_group_registrations_pkey
            primary key,
    group_id               bigint not null
        constraint group_registrations_group_fkey
            references public.groups
            on update cascade on delete restrict,
    course_registration_id bigint not null
        constraint group_registrations_registration_fkey
            references public.course_registrations
            on update cascade on delete restrict,
    unique (group_id, course_registration_id)
);

alter table public.group_registrations
    owner to postgres;

create index fki_group_registrations_group_fkey
    on public.group_registrations (group_id);

create index fki_group_registrations_registration_fkey
    on public.group_registrations (course_registration_id);

