-- =============================================================
-- Relaya V3 Migration
-- Add ON DELETE CASCADE to board_writes.approval_id to ensure
-- retention purge cascades cleanly through the entire entity graph.
-- =============================================================

ALTER TABLE board_writes
    DROP CONSTRAINT board_writes_approval_id_fkey,
    ADD CONSTRAINT board_writes_approval_id_fkey
        FOREIGN KEY (approval_id) REFERENCES approvals(id) ON DELETE CASCADE;