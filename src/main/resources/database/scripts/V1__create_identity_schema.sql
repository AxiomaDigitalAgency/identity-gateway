CREATE TABLE identity_context (
                                  id VARCHAR(36) PRIMARY KEY,
                                  tenant_id VARCHAR(100) NOT NULL,
                                  channel VARCHAR(50) NOT NULL,
                                  subject_type VARCHAR(50) NOT NULL,
                                  subject_value VARCHAR(150) NOT NULL,
                                  status VARCHAR(30) NOT NULL,
                                  display_name VARCHAR(150),
                                  metadata_json TEXT,
                                  create_date TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                  update_date TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                  active_reg_ind BOOLEAN NOT NULL DEFAULT TRUE,
                                  CONSTRAINT chk_identity_context_channel
                                      CHECK (channel IN ('WEB', 'WHATSAPP', 'INTERNAL', 'API')),
                                  CONSTRAINT chk_identity_context_subject_type
                                      CHECK (subject_type IN ('WIDGET', 'CHANNEL_CLIENT', 'INTERNAL_SERVICE', 'PROVIDER_ACCOUNT', 'USER')),
                                  CONSTRAINT chk_identity_context_status
                                      CHECK (status IN ('ACTIVE', 'INACTIVE', 'REVOKED', 'EXPIRED'))
);

CREATE TABLE identity_credential (
                                     id VARCHAR(36) PRIMARY KEY,
                                     identity_context_id VARCHAR(36) NOT NULL,
                                     credential_type VARCHAR(50) NOT NULL,
                                     credential_value_hash VARCHAR(255) NOT NULL,
                                     credential_hint VARCHAR(120),
                                     status VARCHAR(30) NOT NULL,
                                     expires_at TIMESTAMP WITH TIME ZONE,
                                     metadata_json TEXT,
                                     create_date TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                     update_date TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                     active_reg_ind BOOLEAN NOT NULL DEFAULT TRUE,
                                     CONSTRAINT fk_identity_credential_context
                                         FOREIGN KEY (identity_context_id)
                                             REFERENCES identity_context(id),
                                     CONSTRAINT chk_identity_credential_type
                                         CHECK (credential_type IN (
                                                                    'WIDGET_KEY_HASH',
                                                                    'API_KEY_HASH',
                                                                    'SHARED_SECRET_HASH',
                                                                    'CLIENT_SECRET_HASH',
                                                                    'SIGNING_KEY_REF'
                                             )),
                                     CONSTRAINT chk_identity_credential_status
                                         CHECK (status IN ('ACTIVE', 'INACTIVE', 'REVOKED', 'EXPIRED'))
);

CREATE TABLE identity_origin_rule (
                                      id VARCHAR(36) PRIMARY KEY,
                                      identity_context_id VARCHAR(36) NOT NULL,
                                      origin_type VARCHAR(30) NOT NULL,
                                      origin_value VARCHAR(255) NOT NULL,
                                      status VARCHAR(30) NOT NULL,
                                      priority INTEGER NOT NULL DEFAULT 0,
                                      create_date TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                      update_date TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                      active_reg_ind BOOLEAN NOT NULL DEFAULT TRUE,
                                      CONSTRAINT fk_identity_origin_rule_context
                                          FOREIGN KEY (identity_context_id)
                                              REFERENCES identity_context(id),
                                      CONSTRAINT chk_identity_origin_rule_type
                                          CHECK (origin_type IN ('EXACT', 'HOST', 'PATTERN')),
                                      CONSTRAINT chk_identity_origin_rule_status
                                          CHECK (status IN ('ACTIVE', 'INACTIVE', 'REVOKED', 'EXPIRED'))
);

CREATE TABLE identity_session (
                                  id VARCHAR(36) PRIMARY KEY,
                                  identity_context_id VARCHAR(36) NOT NULL,
                                  tenant_id VARCHAR(100) NOT NULL,
                                  channel VARCHAR(50) NOT NULL,
                                  session_token_id VARCHAR(120) NOT NULL,
                                  status VARCHAR(30) NOT NULL,
                                  issued_at TIMESTAMP WITH TIME ZONE NOT NULL,
                                  expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
                                  last_seen_at TIMESTAMP WITH TIME ZONE,
                                  client_ip VARCHAR(100),
                                  user_agent VARCHAR(500),
                                  metadata_json TEXT,
                                  create_date TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                  update_date TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                  active_reg_ind BOOLEAN NOT NULL DEFAULT TRUE,
                                  CONSTRAINT fk_identity_session_context
                                      FOREIGN KEY (identity_context_id)
                                          REFERENCES identity_context(id),
                                  CONSTRAINT chk_identity_session_channel
                                      CHECK (channel IN ('WEB', 'WHATSAPP', 'INTERNAL', 'API')),
                                  CONSTRAINT chk_identity_session_status
                                      CHECK (status IN ('ACTIVE', 'REVOKED', 'EXPIRED'))
);

CREATE TABLE identity_token_revocation (
                                           id VARCHAR(36) PRIMARY KEY,
                                           tenant_id VARCHAR(100) NOT NULL,
                                           token_id VARCHAR(120) NOT NULL,
                                           token_type VARCHAR(50) NOT NULL,
                                           reason VARCHAR(255),
                                           revoked_at TIMESTAMP WITH TIME ZONE NOT NULL,
                                           expires_at TIMESTAMP WITH TIME ZONE,
                                           create_date TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                           update_date TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                           active_reg_ind BOOLEAN NOT NULL DEFAULT TRUE,
                                           CONSTRAINT chk_identity_token_revocation_type
                                               CHECK (token_type IN ('SESSION_TOKEN', 'ACCESS_TOKEN', 'REFRESH_TOKEN'))
);

CREATE TABLE identity_audit_event (
                                      id VARCHAR(36) PRIMARY KEY,
                                      tenant_id VARCHAR(100),
                                      identity_context_id VARCHAR(36),
                                      session_id VARCHAR(36),
                                      event_type VARCHAR(80) NOT NULL,
                                      event_status VARCHAR(30) NOT NULL,
                                      channel VARCHAR(50),
                                      origin_value VARCHAR(255),
                                      client_ip VARCHAR(100),
                                      user_agent VARCHAR(500),
                                      detail_json TEXT,
                                      event_date TIMESTAMP WITH TIME ZONE NOT NULL,
                                      create_date TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                      update_date TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                      active_reg_ind BOOLEAN NOT NULL DEFAULT TRUE,
                                      CONSTRAINT fk_identity_audit_event_context
                                          FOREIGN KEY (identity_context_id)
                                              REFERENCES identity_context(id),
                                      CONSTRAINT fk_identity_audit_event_session
                                          FOREIGN KEY (session_id)
                                              REFERENCES identity_session(id),
                                      CONSTRAINT chk_identity_audit_event_channel
                                          CHECK (channel IN ('WEB', 'WHATSAPP', 'INTERNAL', 'API')),
                                      CONSTRAINT chk_identity_audit_event_status
                                          CHECK (event_status IN ('SUCCESS', 'FAILED'))
);