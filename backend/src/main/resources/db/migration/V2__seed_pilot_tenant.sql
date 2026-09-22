-- =============================================================
-- Pilot seed data
-- One tenant, one admin account, one reviewer account, stub Trello config.
-- Password hash below = BCrypt of 'changeit'
-- =============================================================

INSERT INTO tenants (id, name, slug)
VALUES ('00000000-0000-0000-0000-000000000001', 'Relaya Pilot', 'relaya-pilot');

-- Admin account (BCrypt of 'changeit')
INSERT INTO users (id, tenant_id, email, password_hash, role)
VALUES (
    '00000000-0000-0000-0000-000000000010',
    '00000000-0000-0000-0000-000000000001',
    'admin@relaya.demo',
    '$2a$10$mTt79siIUOn1nYvigBAda.puVs3rULNr/8OdOM1O6CBgn4sxIxtSm',
    'ROLE_ADMIN'
);

-- Default reviewer account (BCrypt of 'changeit')
INSERT INTO users (id, tenant_id, email, password_hash, role)
VALUES (
    '00000000-0000-0000-0000-000000000011',
    '00000000-0000-0000-0000-000000000001',
    'reviewer@relaya.demo',
    '$2a$10$mTt79siIUOn1nYvigBAda.puVs3rULNr/8OdOM1O6CBgn4sxIxtSm',
    'ROLE_REVIEWER'
);

-- Tenant adapter config (STUB mode)
INSERT INTO tenant_config (tenant_id, config_key, config_value)
VALUES
    ('00000000-0000-0000-0000-000000000001', 'GROQ_API_KEY',   'PLACEHOLDER'),
    ('00000000-0000-0000-0000-000000000001', 'GROQ_MODEL',     'llama-3.3-70b-versatile'),
    ('00000000-0000-0000-0000-000000000001', 'TRELLO_BOARD_ID','STUB');