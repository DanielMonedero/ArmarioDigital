CREATE TABLE users (
    id           BIGSERIAL    PRIMARY KEY,
    username     VARCHAR(64)  NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    display_name VARCHAR(120),
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE garments (
    id              BIGSERIAL         PRIMARY KEY,
    owner_id        BIGINT            NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name            VARCHAR(160)      NOT NULL,
    description     TEXT,
    size            VARCHAR(40)       NOT NULL,
    category        VARCHAR(40)       NOT NULL,
    color           VARCHAR(60),
    brand           VARCHAR(120),
    condition       VARCHAR(20)       NOT NULL,
    status          VARCHAR(20)       NOT NULL,
    sale_price      NUMERIC(12, 2),
    purchase_price  NUMERIC(12, 2),
    notes           TEXT,
    sold_at         TIMESTAMPTZ,
    created_at      TIMESTAMPTZ       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMPTZ       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_garments_status CHECK (status IN ('WARDROBE', 'FOR_SALE', 'SOLD')),
    CONSTRAINT chk_garments_condition CHECK (condition IN ('NEW', 'LIKE_NEW', 'GOOD', 'USED'))
);

CREATE INDEX idx_garments_owner_id ON garments(owner_id);
CREATE INDEX idx_garments_owner_status ON garments(owner_id, status);
CREATE INDEX idx_garments_owner_category ON garments(owner_id, category);
CREATE INDEX idx_garments_owner_size ON garments(owner_id, size);

CREATE TABLE garment_images (
    id                 BIGSERIAL    PRIMARY KEY,
    garment_id         BIGINT       NOT NULL REFERENCES garments(id) ON DELETE CASCADE,
    filename           VARCHAR(255) NOT NULL,
    original_filename  VARCHAR(255) NOT NULL,
    content_type       VARCHAR(80)  NOT NULL,
    size_bytes         BIGINT       NOT NULL,
    sort_order         INTEGER      NOT NULL DEFAULT 0,
    created_at         TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_garment_images_garment_id ON garment_images(garment_id);
CREATE INDEX idx_garment_images_garment_sort ON garment_images(garment_id, sort_order);
