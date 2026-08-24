-- V3__add_locations_and_outfits.sql
-- New per-user Locations table and saved-outfits feature.

CREATE TABLE locations (
    id          BIGSERIAL    PRIMARY KEY,
    owner_id    BIGINT       NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name        VARCHAR(120) NOT NULL,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Case-insensitive unique constraint per owner. Postgres does not allow
-- expressions in UNIQUE constraints, only in unique indexes.
CREATE UNIQUE INDEX uq_locations_owner_name ON locations(owner_id, lower(name));

CREATE INDEX idx_locations_owner_id ON locations(owner_id);

-- Garments now reference an optional location. Existing rows keep location_id = NULL.
ALTER TABLE garments
    ADD COLUMN location_id BIGINT REFERENCES locations(id) ON DELETE SET NULL;

CREATE INDEX idx_garments_owner_location ON garments(owner_id, location_id);

-- Saved outfits.
CREATE TABLE saved_outfits (
    id                   BIGSERIAL    PRIMARY KEY,
    owner_id             BIGINT       NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name                 VARCHAR(160) NOT NULL,
    season               VARCHAR(20)  NOT NULL,
    include_outerwear    BOOLEAN      NOT NULL DEFAULT FALSE,
    include_accessories  BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at           TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_saved_outfits_season CHECK (season IN ('SUMMER', 'WINTER'))
);

CREATE INDEX idx_saved_outfits_owner_id ON saved_outfits(owner_id);

CREATE TABLE saved_outfit_items (
    id                       BIGSERIAL    PRIMARY KEY,
    outfit_id                BIGINT       NOT NULL REFERENCES saved_outfits(id) ON DELETE CASCADE,
    garment_id               BIGINT       NOT NULL REFERENCES garments(id) ON DELETE CASCADE,
    category                 VARCHAR(40)  NOT NULL,
    snapshot_name            VARCHAR(160) NOT NULL,
    snapshot_cover_image_id  VARCHAR(32),
    sort_order               INTEGER      NOT NULL DEFAULT 0,
    CONSTRAINT chk_outfit_item_category CHECK (category IN (
        'TOP', 'SWEATER', 'OUTERWEAR', 'BOTTOM', 'SKIRT',
        'DRESS', 'SHOES', 'ACCESSORIES', 'OTHER'
    ))
);

CREATE INDEX idx_outfit_items_outfit_id ON saved_outfit_items(outfit_id);
CREATE INDEX idx_outfit_items_garment_id ON saved_outfit_items(garment_id);
