-- =============================================================
-- Relaya V1 Schema
-- All tables include tenant_id for SaaS forward-compatibility.
-- UUID v4 primary keys throughout.
-- =============================================================

CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- ── tenants ──────────────────────────────────────────────────
CREATE TABLE tenants (
    id         UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    name       VARCHAR(100) NOT NULL,
    slug       VARCHAR(50)  NOT NULL UNIQUE,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- ── tenant_config ─────────────────────────────────────────────
CREATE TABLE tenant_config (
    id           UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id    UUID         NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    config_key   VARCHAR(100) NOT NULL,
    config_value TEXT         NOT NULL,
    updated_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT uq_tenant_config_key UNIQUE (tenant_id, config_key)
);

-- ── users ─────────────────────────────────────────────────────
CREATE TABLE users (
    id            UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id     UUID         NOT NULL REFERENCES tenants(id),
    email         VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role          VARCHAR(50)  NOT NULL CHECK (role IN ('ROLE_REVIEWER','ROLE_ADMIN')),
    active        BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT uq_users_tenant_email UNIQUE (tenant_id, email)
);

-- ── intakes ───────────────────────────────────────────────────
CREATE TABLE intakes (
    id           UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id    UUID         NOT NULL REFERENCES tenants(id),
    client_label VARCHAR(150) NOT NULL,
    service_type VARCHAR(50)  NOT NULL DEFAULT 'WEBSITE_DELIVERY'
                              CHECK (service_type = 'WEBSITE_DELIVERY'),
    status       VARCHAR(50)  NOT NULL DEFAULT 'INTAKE_SUBMITTED'
                              CHECK (status IN (
                                  'INTAKE_SUBMITTED','ANALYSIS_PENDING','ANALYSIS_READY',
                                  'ANALYSIS_FAILED','DRAFT_EDITED','APPROVED',
                                  'WRITE_PENDING','WRITE_SUCCESS','WRITE_FAILED','INVALIDATED'
                              )),
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- ── intake_versions ───────────────────────────────────────────
CREATE TABLE intake_versions (
    id             UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id      UUID        NOT NULL REFERENCES tenants(id),
    intake_id      UUID        NOT NULL REFERENCES intakes(id) ON DELETE CASCADE,
    version_number INT         NOT NULL,
    raw_text       TEXT        NOT NULL,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_intake_version UNIQUE (intake_id, version_number)
);

-- ── analysis_drafts ───────────────────────────────────────────
CREATE TABLE analysis_drafts (
    id                UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id         UUID        NOT NULL REFERENCES tenants(id),
    intake_id         UUID        NOT NULL REFERENCES intakes(id) ON DELETE CASCADE,
    intake_version_id UUID        NOT NULL REFERENCES intake_versions(id),
    objectives        JSONB       NOT NULL DEFAULT '[]',
    deliverables      JSONB       NOT NULL DEFAULT '[]',
    constraints       JSONB       NOT NULL DEFAULT '[]',
    timeline_notes    TEXT,
    budget_notes      TEXT,
    unknowns          JSONB       NOT NULL DEFAULT '[]',
    proposed_tasks    JSONB       NOT NULL DEFAULT '[]',
    content_hash      VARCHAR(64) NOT NULL,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_draft_per_version UNIQUE (intake_version_id)
);

-- ── approvals ─────────────────────────────────────────────────
CREATE TABLE approvals (
    id                      UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id               UUID         NOT NULL REFERENCES tenants(id),
    draft_id                UUID         NOT NULL REFERENCES analysis_drafts(id) ON DELETE CASCADE,
    reviewer_user_id        UUID         NOT NULL REFERENCES users(id),
    content_hash_at_approval VARCHAR(64) NOT NULL,
    approved_at             TIMESTAMPTZ  NOT NULL DEFAULT now(),
    invalidated_at          TIMESTAMPTZ
);

-- ── board_writes ──────────────────────────────────────────────
CREATE TABLE board_writes (
    id               UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id        UUID         NOT NULL REFERENCES tenants(id),
    approval_id      UUID         NOT NULL UNIQUE REFERENCES approvals(id),
    idempotency_key  VARCHAR(128) NOT NULL UNIQUE,
    destination_board VARCHAR(100) NOT NULL DEFAULT 'TRELLO',
    trello_card_id   VARCHAR(100),
    trello_card_url  TEXT,
    status           VARCHAR(50)  NOT NULL DEFAULT 'PENDING'
                                  CHECK (status IN ('PENDING','SUCCESS','FAILED','STUB')),
    error_details    TEXT,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    completed_at     TIMESTAMPTZ
);

-- ── usage_ledger ──────────────────────────────────────────────
CREATE TABLE usage_ledger (
    id                  UUID           PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id           UUID           NOT NULL REFERENCES tenants(id),
    intake_id           UUID           REFERENCES intakes(id) ON DELETE SET NULL,
    provider            VARCHAR(50)    NOT NULL,
    model               VARCHAR(100)   NOT NULL,
    prompt_tokens       INT            NOT NULL CHECK (prompt_tokens >= 0),
    completion_tokens   INT            NOT NULL CHECK (completion_tokens >= 0),
    estimated_cost_usd  NUMERIC(10, 6) NOT NULL CHECK (estimated_cost_usd >= 0),
    called_at           TIMESTAMPTZ    NOT NULL DEFAULT now()
);

-- ── audit_events ──────────────────────────────────────────────
CREATE TABLE audit_events (
    id          UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id   UUID         NOT NULL REFERENCES tenants(id),
    intake_id   UUID         REFERENCES intakes(id) ON DELETE CASCADE,
    user_id     UUID         REFERENCES users(id),
    event_type  VARCHAR(100) NOT NULL,
    payload     JSONB,
    occurred_at TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- =============================================================
-- Indexes
-- =============================================================

CREATE INDEX idx_intakes_tenant_status   ON intakes(tenant_id, status);
CREATE INDEX idx_intakes_tenant_created  ON intakes(tenant_id, created_at DESC);
CREATE INDEX idx_audit_events_intake     ON audit_events(intake_id, occurred_at DESC);
CREATE INDEX idx_audit_events_tenant     ON audit_events(tenant_id, occurred_at DESC);
CREATE INDEX idx_usage_ledger_budget     ON usage_ledger(tenant_id, called_at);
CREATE INDEX idx_drafts_intake           ON analysis_drafts(intake_id);
CREATE INDEX idx_board_writes_status     ON board_writes(tenant_id, status);