-- V4__add_maleta.sql
-- Per-user "Maleta" (travel suitcase): a join table that references a
-- garment by id. Adding a garment to the maleta does NOT change its
-- status and does NOT remove it from the wardrobe -- the maleta is a
-- pure reference layer on top.

CREATE TABLE maleta_items (
    id          BIGSERIAL    PRIMARY KEY,
    owner_id    BIGINT       NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    garment_id  BIGINT       NOT NULL REFERENCES garments(id) ON DELETE CASCADE,
    added_at    TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_maleta_owner_garment UNIQUE (owner_id, garment_id)
);

CREATE INDEX idx_maleta_items_owner_id ON maleta_items(owner_id);
CREATE INDEX idx_maleta_items_garment_id ON maleta_items(garment_id);