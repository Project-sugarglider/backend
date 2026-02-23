ALTER TABLE kca_call_history
ADD COLUMN IF NOT EXISTS good_inspect_day VARCHAR(8);

UPDATE kca_call_history
SET good_inspect_day = kca_call_day
WHERE good_inspect_day IS NULL;

ALTER TABLE kca_call_history
ALTER COLUMN good_inspect_day SET NOT NULL;

ALTER TABLE kca_call_history DROP CONSTRAINT kca_call_history_pkey;

ALTER TABLE kca_call_history
ADD CONSTRAINT kca_call_history_pkey
PRIMARY KEY (kca_call_day, entp_id, good_inspect_day);