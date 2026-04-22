-- =========================================================
-- IDENTITY DB - AION V2
-- PostgreSQL
-- =========================================================

-- =========================================================
-- Limpieza opcional (solo usar en entorno local/dev)
-- =========================================================
drop table if exists identity_audit_event cascade;
drop table if exists identity_token_revocation cascade;
drop table if exists identity_session cascade;
drop table if exists identity_origin_rule cascade;
drop table if exists identity_credential cascade;
drop table if exists identity_context cascade;

-- =========================================================
-- TABLA: identity_context
-- Representa el contexto base de identidad por tenant/canal
-- =========================================================
create table identity_context (
                                  id uuid primary key,
                                  tenant_id uuid not null,
                                  context_key varchar(100) not null,
                                  channel varchar(50) not null,
                                  status varchar(30) not null,
                                  create_date timestamp with time zone not null,
                                  update_date timestamp with time zone not null,
                                  active_reg_ind boolean not null,

                                  constraint chk_identity_context_status
                                      check (status in ('ACTIVE', 'INACTIVE'))
);

create unique index ux_identity_context_tenant_context_key
    on identity_context (tenant_id, context_key);

create index ix_identity_context_tenant_id
    on identity_context (tenant_id);

create index ix_identity_context_channel
    on identity_context (channel);

-- =========================================================
-- TABLA: identity_credential
-- Credenciales autenticables: widget, channel, service, oauth
-- =========================================================
create table identity_credential (
                                     id uuid primary key,
                                     identity_context_id uuid not null references identity_context(id),
                                     credential_type varchar(50) not null,
                                     credential_key varchar(255),
                                     secret_hash varchar(500),
                                     status varchar(30) not null,
                                     enabled boolean not null,
                                     create_date timestamp with time zone not null,
                                     update_date timestamp with time zone not null,
                                     active_reg_ind boolean not null,

                                     constraint chk_identity_credential_type
                                         check (credential_type in ('WIDGET_KEY', 'CHANNEL_KEY', 'SERVICE_CLIENT', 'OAUTH_CLIENT')),

                                     constraint chk_identity_credential_status
                                         check (status in ('ACTIVE', 'INACTIVE', 'REVOKED'))
);

create unique index ux_identity_credential_key
    on identity_credential (credential_key);

create index ix_identity_credential_context_id
    on identity_credential (identity_context_id);

create index ix_identity_credential_type
    on identity_credential (credential_type);

-- =========================================================
-- TABLA: identity_origin_rule
-- Origins permitidos por credencial
-- =========================================================
create table identity_origin_rule (
                                      id uuid primary key,
                                      identity_credential_id uuid not null references identity_credential(id),
                                      allowed_origin varchar(500) not null,
                                      status varchar(30) not null,
                                      create_date timestamp with time zone not null,
                                      update_date timestamp with time zone not null,
                                      active_reg_ind boolean not null,

                                      constraint chk_identity_origin_rule_status
                                          check (status in ('ACTIVE', 'INACTIVE'))
);

create index ix_identity_origin_rule_credential_id
    on identity_origin_rule (identity_credential_id);

create index ix_identity_origin_rule_allowed_origin
    on identity_origin_rule (allowed_origin);

-- =========================================================
-- TABLA: identity_session
-- Sesión materializada después de autenticación exitosa
-- =========================================================
create table identity_session (
                                  id uuid primary key,
                                  tenant_id uuid not null,
                                  credential_id uuid not null references identity_credential(id),
                                  subject varchar(255) not null,
                                  channel varchar(50) not null,
                                  authentication_type varchar(50) not null,
                                  status varchar(30) not null,
                                  authenticated_at timestamp with time zone not null,
                                  session_created_at timestamp with time zone not null,
                                  expires_at timestamp with time zone not null,
                                  revoked_at timestamp with time zone,
                                  create_date timestamp with time zone not null,
                                  update_date timestamp with time zone not null,
                                  active_reg_ind boolean not null,

                                  constraint chk_identity_session_auth_type
                                      check (authentication_type in ('WIDGET', 'CHANNEL', 'SERVICE', 'OAUTH', 'SESSION')),

                                  constraint chk_identity_session_status
                                      check (status in ('ACTIVE', 'REVOKED', 'EXPIRED', 'CLOSED'))
);

create index ix_identity_session_tenant_id
    on identity_session (tenant_id);

create index ix_identity_session_credential_id
    on identity_session (credential_id);

create index ix_identity_session_subject
    on identity_session (subject);

create index ix_identity_session_channel
    on identity_session (channel);

create index ix_identity_session_status
    on identity_session (status);

create index ix_identity_session_expires_at
    on identity_session (expires_at);

-- =========================================================
-- TABLA: identity_token_revocation
-- Revocación de tokens por jti
-- =========================================================
create table identity_token_revocation (
                                           id uuid primary key,
                                           session_id uuid not null references identity_session(id),
                                           token_jti varchar(255) not null,
                                           revoked_at timestamp with time zone not null,
                                           reason varchar(255),
                                           create_date timestamp with time zone not null,
                                           update_date timestamp with time zone not null,
                                           active_reg_ind boolean not null
);

create unique index ux_identity_token_revocation_jti
    on identity_token_revocation (token_jti);

create index ix_identity_token_revocation_session_id
    on identity_token_revocation (session_id);

-- =========================================================
-- TABLA: identity_audit_event
-- Auditoría de autenticación, sesiones y revocaciones
-- =========================================================
create table identity_audit_event (
                                      id uuid primary key,
                                      tenant_id uuid,
                                      credential_id uuid,
                                      session_id uuid,
                                      event_type varchar(100) not null,
                                      subject varchar(255),
                                      channel varchar(50),
                                      event_timestamp timestamp with time zone not null,
                                      detail_json jsonb,
                                      create_date timestamp with time zone not null,
                                      update_date timestamp with time zone not null,
                                      active_reg_ind boolean not null,

                                      constraint chk_identity_audit_event_type
                                          check (event_type in (
                                                                'AUTHENTICATION_SUCCEEDED',
                                                                'AUTHENTICATION_FAILED',
                                                                'SESSION_CREATED',
                                                                'SESSION_VALIDATED',
                                                                'SESSION_REVOKED'
                                              ))
);

create index ix_identity_audit_event_tenant_id
    on identity_audit_event (tenant_id);

create index ix_identity_audit_event_credential_id
    on identity_audit_event (credential_id);

create index ix_identity_audit_event_session_id
    on identity_audit_event (session_id);

create index ix_identity_audit_event_event_type
    on identity_audit_event (event_type);

create index ix_identity_audit_event_event_timestamp
    on identity_audit_event (event_timestamp);