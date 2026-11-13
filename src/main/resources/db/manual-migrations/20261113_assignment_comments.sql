-- PostgreSQL: run against an existing HelpBridge database before deploying this change.
-- This script is manual; no Flyway/Liquibase runner is configured in this project.
-- Existing assignments keep a NULL closing_comment because it was not previously stored.
ALTER TABLE fundraiser_assignment ADD COLUMN IF NOT EXISTS closing_comment VARCHAR(500);
ALTER TABLE fundraiser_assignment ALTER COLUMN return_reason TYPE VARCHAR(500);
