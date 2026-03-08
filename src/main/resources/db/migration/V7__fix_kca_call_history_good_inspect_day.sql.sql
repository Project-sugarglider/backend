-- V7__fix_kca_call_history_good_inspect_day.sql

UPDATE kca_call_history
SET good_inspect_day = kca_call_day
WHERE good_inspect_day IS NULL
  AND kca_call_day IS NOT NULL;

ALTER TABLE kca_call_history
ALTER COLUMN good_inspect_day SET NOT NULL;

DROP INDEX IF EXISTS ux_kca_call_history_call_entp_inspect;

CREATE UNIQUE INDEX IF NOT EXISTS ux_kca_call_history_call_entp_inspect
ON kca_call_history (kca_call_day, entp_id, good_inspect_day);