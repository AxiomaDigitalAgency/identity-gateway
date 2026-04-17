CREATE INDEX idx_identity_context_tenant_id
    ON identity_context (tenant_id);

CREATE INDEX idx_identity_context_tenant_channel
    ON identity_context (tenant_id, channel);

CREATE UNIQUE INDEX uq_identity_context_tenant_channel_subject
    ON identity_context (tenant_id, channel, subject_type, subject_value);

CREATE INDEX idx_identity_credential_context
    ON identity_credential (identity_context_id);

CREATE INDEX idx_identity_credential_type_status
    ON identity_credential (credential_type, status);

CREATE INDEX idx_identity_origin_rule_context
    ON identity_origin_rule (identity_context_id);

CREATE INDEX idx_identity_origin_rule_value
    ON identity_origin_rule (origin_value);

CREATE INDEX idx_identity_session_tenant_id
    ON identity_session (tenant_id);

CREATE INDEX idx_identity_session_context
    ON identity_session (identity_context_id);

CREATE UNIQUE INDEX uq_identity_session_token_id
    ON identity_session (session_token_id);

CREATE INDEX idx_identity_session_expires_at
    ON identity_session (expires_at);

CREATE INDEX idx_identity_token_revocation_tenant
    ON identity_token_revocation (tenant_id);

CREATE UNIQUE INDEX uq_identity_token_revocation_token_id
    ON identity_token_revocation (token_id);

CREATE INDEX idx_identity_audit_event_tenant_date
    ON identity_audit_event (tenant_id, event_date);

CREATE INDEX idx_identity_audit_event_context
    ON identity_audit_event (identity_context_id);

CREATE INDEX idx_identity_audit_event_session
    ON identity_audit_event (session_id);