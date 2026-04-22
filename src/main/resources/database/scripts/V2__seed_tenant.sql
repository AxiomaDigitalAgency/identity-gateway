-- =========================================================
-- SEED BASE
-- Tenants con los que vienes trabajando
-- =========================================================

-- ---------------------------------------------------------
-- UUIDS FIJOS DE TENANT
-- ---------------------------------------------------------
-- axioma-agency        -> 11111111-1111-1111-1111-111111111111
-- sonrisa-viva-dental  -> 22222222-2222-2222-2222-222222222222
-- novasmile-dental     -> 33333333-3333-3333-3333-333333333333
-- dermalux-clinic      -> 44444444-4444-4444-4444-444444444444

-- ---------------------------------------------------------
-- IDENTITY_CONTEXT (uno web por tenant)
-- ---------------------------------------------------------
insert into identity_context (
    id, tenant_id, context_key, channel, status, create_date, update_date, active_reg_ind
) values
      (
          'a1111111-1111-1111-1111-111111111111',
          '11111111-1111-1111-1111-111111111111',
          'web-widget-main',
          'web',
          'ACTIVE',
          now(),
          now(),
          true
      ),
      (
          'a2222222-2222-2222-2222-222222222222',
          '22222222-2222-2222-2222-222222222222',
          'web-widget-main',
          'web',
          'ACTIVE',
          now(),
          now(),
          true
      ),
      (
          'a3333333-3333-3333-3333-333333333333',
          '33333333-3333-3333-3333-333333333333',
          'web-widget-main',
          'web',
          'ACTIVE',
          now(),
          now(),
          true
      ),
      (
          'a4444444-4444-4444-4444-444444444444',
          '44444444-4444-4444-4444-444444444444',
          'web-widget-main',
          'web',
          'ACTIVE',
          now(),
          now(),
          true
      );

-- ---------------------------------------------------------
-- IDENTITY_CREDENTIAL
-- credential_key = lookup público/canónico de la credencial
-- secret_hash = hash bcrypt del secreto/widget key real si aplica
--
-- NOTA:
-- Aquí estoy dejando credential_key legible para lookup y
-- un secret_hash de ejemplo. Si tu flujo actual usa una sola key
-- sin separación key/id, puedes adaptar después.
-- ---------------------------------------------------------
insert into identity_credential (
    id, identity_context_id, credential_type, credential_key, secret_hash, status, enabled, create_date, update_date, active_reg_ind
) values
      (
          'b1111111-1111-1111-1111-111111111111',
          'a1111111-1111-1111-1111-111111111111',
          'WIDGET_KEY',
          'widget-axioma-agency-main',
          '$2a$10$ZN1MVAmOrVnZ1gHsCQ64WOj2ct4zWNAic.Ov.VUCmI1HgXdG6/Huu',
          'ACTIVE',
          true,
          now(),
          now(),
          true
      ),
      (
          'b2222222-2222-2222-2222-222222222222',
          'a2222222-2222-2222-2222-222222222222',
          'WIDGET_KEY',
          'widget-sonrisa-viva-main',
          '$2a$10$ZN1MVAmOrVnZ1gHsCQ64WOj2ct4zWNAic.Ov.VUCmI1HgXdG6/Huu',
          'ACTIVE',
          true,
          now(),
          now(),
          true
      ),
      (
          'b3333333-3333-3333-3333-333333333333',
          'a3333333-3333-3333-3333-333333333333',
          'WIDGET_KEY',
          'widget-novasmile-main',
          '$2a$10$ZN1MVAmOrVnZ1gHsCQ64WOj2ct4zWNAic.Ov.VUCmI1HgXdG6/Huu',
          'ACTIVE',
          true,
          now(),
          now(),
          true
      ),
      (
          'b4444444-4444-4444-4444-444444444444',
          'a4444444-4444-4444-4444-444444444444',
          'WIDGET_KEY',
          'widget-dermalux-main',
          '$2a$10$ZN1MVAmOrVnZ1gHsCQ64WOj2ct4zWNAic.Ov.VUCmI1HgXdG6/Huu',
          'ACTIVE',
          true,
          now(),
          now(),
          true
      );

-- ---------------------------------------------------------
-- IDENTITY_ORIGIN_RULE
-- Origins permitidos por credencial
-- ---------------------------------------------------------
insert into identity_origin_rule (
    id, identity_credential_id, allowed_origin, status, create_date, update_date, active_reg_ind
) values
      (
          'c1111111-1111-1111-1111-111111111111',
          'b1111111-1111-1111-1111-111111111111',
          'https://axiomagency.mx',
          'ACTIVE',
          now(),
          now(),
          true
      ),
      (
          'c1111111-1111-1111-1111-111111111112',
          'b1111111-1111-1111-1111-111111111111',
          'https://www.axiomagency.mx',
          'ACTIVE',
          now(),
          now(),
          true
      ),
      (
          'c1111111-1111-1111-1111-111111111113',
          'b1111111-1111-1111-1111-111111111111',
          'http://localhost:3000',
          'ACTIVE',
          now(),
          now(),
          true
      ),
      (
          'c1111111-1111-1111-1111-111111111114',
          'b1111111-1111-1111-1111-111111111111',
          'http://localhost:5173',
          'ACTIVE',
          now(),
          now(),
          true
      ),

      (
          'c2222222-2222-2222-2222-222222222221',
          'b2222222-2222-2222-2222-222222222222',
          'https://sonrisa-viva.mx',
          'ACTIVE',
          now(),
          now(),
          true
      ),
      (
          'c2222222-2222-2222-2222-222222222222',
          'b2222222-2222-2222-2222-222222222222',
          'http://localhost:3000',
          'ACTIVE',
          now(),
          now(),
          true
      ),

      (
          'c3333333-3333-3333-3333-333333333331',
          'b3333333-3333-3333-3333-333333333333',
          'https://novasmile.mx',
          'ACTIVE',
          now(),
          now(),
          true
      ),
      (
          'c3333333-3333-3333-3333-333333333332',
          'b3333333-3333-3333-3333-333333333333',
          'http://localhost:3000',
          'ACTIVE',
          now(),
          now(),
          true
      ),

      (
          'c4444444-4444-4444-4444-444444444441',
          'b4444444-4444-4444-4444-444444444444',
          'https://dermalux.mx',
          'ACTIVE',
          now(),
          now(),
          true
      ),
      (
          'c4444444-4444-4444-4444-444444444442',
          'b4444444-4444-4444-4444-444444444444',
          'http://localhost:3000',
          'ACTIVE',
          now(),
          now(),
          true
      );

-- =========================================================
-- SEED OPCIONAL DE AUDITORÍA INICIAL
-- =========================================================
insert into identity_audit_event (
    id, tenant_id, credential_id, session_id, event_type, subject, channel, event_timestamp, detail_json, create_date, update_date, active_reg_ind
) values
      (
          'd1111111-1111-1111-1111-111111111111',
          '11111111-1111-1111-1111-111111111111',
          'b1111111-1111-1111-1111-111111111111',
          null,
          'AUTHENTICATION_SUCCEEDED',
          'bootstrap',
          'web',
          now(),
          '{"note":"seed event axioma-agency"}'::jsonb,
          now(),
          now(),
          true
      ),
      (
          'd2222222-2222-2222-2222-222222222222',
          '22222222-2222-2222-2222-222222222222',
          'b2222222-2222-2222-2222-222222222222',
          null,
          'AUTHENTICATION_SUCCEEDED',
          'bootstrap',
          'web',
          now(),
          '{"note":"seed event sonrisa-viva-dental"}'::jsonb,
          now(),
          now(),
          true
      ),
      (
          'd3333333-3333-3333-3333-333333333333',
          '33333333-3333-3333-3333-333333333333',
          'b3333333-3333-3333-3333-333333333333',
          null,
          'AUTHENTICATION_SUCCEEDED',
          'bootstrap',
          'web',
          now(),
          '{"note":"seed event novasmile-dental"}'::jsonb,
          now(),
          now(),
          true
      ),
      (
          'd4444444-4444-4444-4444-444444444444',
          '44444444-4444-4444-4444-444444444444',
          'b4444444-4444-4444-4444-444444444444',
          null,
          'AUTHENTICATION_SUCCEEDED',
          'bootstrap',
          'web',
          now(),
          '{"note":"seed event dermalux-clinic"}'::jsonb,
          now(),
          now(),
          true
      );