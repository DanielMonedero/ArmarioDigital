-- V2__refactor_category_color_season.sql
-- Refactor: hierarchical category/subcategory, Color enum, Season enum.
-- Existing garments are deleted (decided by the user) so the schema can
-- switch to stricter constraints without backfills.

-- 1. Wipe existing data. garment_images has FK ON DELETE CASCADE, but we
--    delete explicitly to keep the migration readable.
DELETE FROM garment_images;
DELETE FROM garments;

-- 2. Drop the old category CHECK; the new one replaces it.
ALTER TABLE garments DROP CONSTRAINT IF EXISTS chk_garments_category;

-- 3. Resize color column. Existing data is gone, so we don't need a USING clause.
ALTER TABLE garments ALTER COLUMN color TYPE VARCHAR(20);

-- 4. New columns. Table is empty, so NOT NULL without a default is safe.
ALTER TABLE garments
    ADD COLUMN subcategory VARCHAR(40),
    ADD COLUMN season     VARCHAR(20) NOT NULL;

-- 5. New CHECK constraints.
ALTER TABLE garments
    ADD CONSTRAINT chk_garments_category CHECK (
        category IN ('TOP', 'SWEATER', 'OUTERWEAR', 'BOTTOM', 'SKIRT',
                     'DRESS', 'SHOES', 'ACCESSORIES', 'OTHER')
    );

ALTER TABLE garments
    ADD CONSTRAINT chk_garments_color CHECK (
        color IS NULL OR color IN (
            'WHITE', 'BLACK', 'GRAY', 'BEIGE', 'RED',
            'ORANGE', 'YELLOW', 'GREEN', 'BLUE', 'MULTICOLOR'
        )
    );

ALTER TABLE garments
    ADD CONSTRAINT chk_garments_subcategory CHECK (
        subcategory IS NULL OR subcategory IN (
            'T_SHIRT', 'SHIRT', 'POLO', 'TANK_TOP', 'BLOUSE',
            'SWEATER', 'HOODIE', 'CARDIGAN',
            'JACKET', 'COAT', 'BLAZER', 'VEST',
            'JEANS', 'CHINOS', 'DRESS_PANTS', 'JOGGERS', 'LINEN_PANTS', 'SHORTS', 'LEGGINGS',
            'MINI_SKIRT', 'MIDI_SKIRT', 'MAXI_SKIRT',
            'SHORT_DRESS', 'LONG_DRESS',
            'SNEAKERS', 'BOOTS', 'SANDALS', 'HEELED', 'FLATS',
            'BELT', 'BAG', 'HAT', 'SCARF',
            'OTHER'
        )
    );

ALTER TABLE garments
    ADD CONSTRAINT chk_garments_season CHECK (season IN ('SUMMER', 'WINTER'));

-- 6. Indexes that help the new filters.
CREATE INDEX IF NOT EXISTS idx_garments_owner_subcategory ON garments(owner_id, subcategory);
CREATE INDEX IF NOT EXISTS idx_garments_owner_color       ON garments(owner_id, color);
CREATE INDEX IF NOT EXISTS idx_garments_owner_season      ON garments(owner_id, season);

-- The old category index is no longer optimal (category+subcategory is more selective);
-- keep idx_garments_owner_category as-is, it is still useful for category-only filters.
