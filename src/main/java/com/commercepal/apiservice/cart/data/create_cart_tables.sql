-- ============================================================================
-- Microsoft SQL Server T-SQL Script
-- Tables: carts, cart_items
-- Generated for CommercePal E-Commerce Platform
-- Author: Database Architect
-- Description: Production-ready shopping cart tables with guest cart support,
--              abandoned cart tracking, price monitoring, and stock validation
-- ============================================================================

USE CommercePal;
GO

-- ============================================================================
-- DROP EXISTING OBJECTS (Safe removal with dependency order)
-- ============================================================================

-- Drop foreign key constraints first (cart_items depends on carts)
IF EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = 'fk_cart_item_cart')
BEGIN
    ALTER TABLE cart_items DROP CONSTRAINT fk_cart_item_cart;
END
GO

-- Drop cart_items table
IF OBJECT_ID('dbo.cart_items', 'U') IS NOT NULL
BEGIN
    DROP TABLE dbo.cart_items;
END
GO

-- Drop carts table
IF OBJECT_ID('dbo.carts', 'U') IS NOT NULL
BEGIN
    DROP TABLE dbo.carts;
END
GO

-- ============================================================================
-- TABLE: carts
-- Description: Shopping cart entity supporting both authenticated and guest
--              users with abandoned cart detection and recovery
-- ============================================================================

CREATE TABLE dbo.carts
(
    -- PRIMARY KEY (inherited from BaseAuditEntity)
    id                              BIGINT              IDENTITY(1,1)   NOT NULL,

    -- AUDIT FIELDS (inherited from BaseAuditEntity)
    created_at                      DATETIME2(7)        NOT NULL,
    updated_at                      DATETIME2(7)        NULL,
    created_by                      NVARCHAR(100)       NOT NULL,
    updated_by                      NVARCHAR(100)       NULL,
    version                         BIGINT              NOT NULL        DEFAULT 0,
    is_deleted                      BIT                 NOT NULL        DEFAULT 0,
    deleted_at                      DATETIME2(7)        NULL,
    deleted_by                      NVARCHAR(100)       NULL,
    created_ip                      NVARCHAR(45)        NULL,
    updated_ip                      NVARCHAR(45)        NULL,
    remarks                         NVARCHAR(500)       NULL,

    -- RELATIONSHIPS
    customer_id                     BIGINT              NULL,

    -- SESSION TRACKING (for guest carts)
    session_id                      NVARCHAR(255)       NULL,

    -- CART STATUS & METADATA
    status                          NVARCHAR(20)        NOT NULL        DEFAULT 'ACTIVE',
    currency                        NVARCHAR(3)         NOT NULL,
    country                         NVARCHAR(4)         NULL,

    -- CART TOTALS
    total_items                     INT                 NOT NULL        DEFAULT 0,
    subtotal                        DECIMAL(19,2)       NULL            DEFAULT 0.00,
    estimated_total                 DECIMAL(19,2)       NULL            DEFAULT 0.00,

    -- ACTIVITY TRACKING
    last_activity_at                DATETIME2(7)        NULL,

    -- ABANDONED CART TRACKING
    abandoned_at                    DATETIME2(7)        NULL,
    abandoned_notification_sent     BIT                 NOT NULL        DEFAULT 0,
    last_notification_at            DATETIME2(7)        NULL,

    -- CONVERSION TRACKING
    converted_at                    DATETIME2(7)        NULL,
    order_number                    NVARCHAR(50)        NULL,

    -- CONSTRAINTS
    CONSTRAINT pk_carts PRIMARY KEY CLUSTERED (id),
    CONSTRAINT fk_cart_customer FOREIGN KEY (customer_id) REFERENCES dbo.customers(id),

    -- CHECK CONSTRAINTS
    CONSTRAINT chk_carts_status CHECK (status IN ('ACTIVE', 'ABANDONED', 'CONVERTED', 'EXPIRED', 'MERGED')),
    CONSTRAINT chk_carts_currency CHECK (currency IN ('ETB', 'USD', 'EUR', 'GBP', 'AED', 'SAR', 'CNY')),
    CONSTRAINT chk_carts_total_items_positive CHECK (total_items >= 0),
    CONSTRAINT chk_carts_subtotal_positive CHECK (subtotal IS NULL OR subtotal >= 0),
    CONSTRAINT chk_carts_estimated_total_positive CHECK (estimated_total IS NULL OR estimated_total >= 0),
    CONSTRAINT chk_carts_customer_or_session CHECK (customer_id IS NOT NULL OR session_id IS NOT NULL)
);
GO

-- ============================================================================
-- INDEXES: carts
-- Optimized for cart retrieval, abandoned cart processing, and analytics
-- ============================================================================

-- Index for customer cart lookups (most common operation)
CREATE NONCLUSTERED INDEX idx_cart_customer
    ON dbo.carts (customer_id)
    INCLUDE (status, currency, total_items, subtotal, last_activity_at)
    WHERE customer_id IS NOT NULL AND is_deleted = 0;
GO

-- Index for guest cart lookups by session
CREATE NONCLUSTERED INDEX idx_cart_session
    ON dbo.carts (session_id)
    INCLUDE (status, currency, total_items, last_activity_at)
    WHERE session_id IS NOT NULL AND is_deleted = 0;
GO

-- Index for abandoned cart processing (batch jobs)
CREATE NONCLUSTERED INDEX idx_cart_status_activity
    ON dbo.carts (status, last_activity_at)
    INCLUDE (customer_id, session_id, abandoned_notification_sent)
    WHERE is_deleted = 0;
GO

-- Index for cart status filtering
CREATE NONCLUSTERED INDEX idx_cart_status
    ON dbo.carts (status)
    INCLUDE (customer_id, total_items, subtotal, last_activity_at)
    WHERE is_deleted = 0;
GO

-- Index for abandoned cart notification processing
CREATE NONCLUSTERED INDEX idx_cart_abandoned_notification
    ON dbo.carts (abandoned_notification_sent, abandoned_at)
    INCLUDE (customer_id, session_id, status)
    WHERE status = 'ABANDONED' AND abandoned_notification_sent = 0 AND is_deleted = 0;
GO

-- Index for conversion tracking and analytics
CREATE NONCLUSTERED INDEX idx_cart_converted
    ON dbo.carts (converted_at)
    INCLUDE (customer_id, order_number, subtotal)
    WHERE status = 'CONVERTED';
GO

-- Index for active cart lookups (real-time operations)
CREATE NONCLUSTERED INDEX idx_cart_active_customer
    ON dbo.carts (customer_id, status)
    INCLUDE (total_items, subtotal, last_activity_at)
    WHERE status = 'ACTIVE' AND is_deleted = 0;
GO

-- Index for soft delete filtering
CREATE NONCLUSTERED INDEX idx_carts_is_deleted
    ON dbo.carts (is_deleted)
    WHERE is_deleted = 1;
GO

-- ============================================================================
-- TABLE: cart_items
-- Description: Individual items in a shopping cart with price tracking,
--              stock validation, and variant support
-- ============================================================================

CREATE TABLE dbo.cart_items
(
    -- PRIMARY KEY (inherited from BaseAuditEntity)
    id                              BIGINT              IDENTITY(1,1)   NOT NULL,

    -- AUDIT FIELDS (inherited from BaseAuditEntity)
    created_at                      DATETIME2(7)        NOT NULL,
    updated_at                      DATETIME2(7)        NULL,
    created_by                      NVARCHAR(100)       NOT NULL,
    updated_by                      NVARCHAR(100)       NULL,
    version                         BIGINT              NOT NULL        DEFAULT 0,
    is_deleted                      BIT                 NOT NULL        DEFAULT 0,
    deleted_at                      DATETIME2(7)        NULL,
    deleted_by                      NVARCHAR(100)       NULL,
    created_ip                      NVARCHAR(45)        NULL,
    updated_ip                      NVARCHAR(45)        NULL,
    remarks                         NVARCHAR(500)       NULL,

    -- RELATIONSHIPS
    cart_id                         BIGINT              NOT NULL,

    -- PRODUCT IDENTIFICATION
    product_id                      NVARCHAR(100)       NOT NULL,
    config_id                       NVARCHAR(100)       NULL,

    -- CACHED PRODUCT INFO (for display performance)
    product_name                    NVARCHAR(255)       NULL,
    product_image_url               NVARCHAR(500)       NULL,

    -- QUANTITY
    quantity                        INT                 NOT NULL,

    -- PRICING & CURRENCY
    currency                        NVARCHAR(3)         NOT NULL,
    country                         NVARCHAR(4)         NULL,
    unit_price                      DECIMAL(19,2)       NOT NULL,
    price_when_added                DECIMAL(19,2)       NOT NULL,
    current_price                   DECIMAL(19,2)       NULL,
    base_price_in_usd               DECIMAL(19,2)       NULL,
    exchange_rate                   DECIMAL(19,6)       NULL,

    -- PROVIDER INFORMATION
    provider                        NVARCHAR(50)        NULL,

    -- STOCK & AVAILABILITY
    stock_status                    NVARCHAR(20)        NULL            DEFAULT 'UNKNOWN',
    is_available                    BIT                 NOT NULL        DEFAULT 1,

    -- TIMESTAMPS
    added_at                        DATETIME2(7)        NOT NULL,
    last_price_check_at             DATETIME2(7)        NULL,

    -- PRICE DROP TRACKING
    price_dropped                   BIT                 NOT NULL        DEFAULT 0,
    price_drop_notified             BIT                 NOT NULL        DEFAULT 0,

    -- CONSTRAINTS
    CONSTRAINT pk_cart_items PRIMARY KEY CLUSTERED (id),
    CONSTRAINT fk_cart_item_cart FOREIGN KEY (cart_id) REFERENCES dbo.carts(id),

    -- CHECK CONSTRAINTS
    CONSTRAINT chk_cart_items_currency CHECK (currency IN ('ETB', 'USD', 'EUR', 'GBP', 'AED', 'SAR', 'CNY')),
    CONSTRAINT chk_cart_items_stock_status CHECK (stock_status IS NULL OR stock_status IN ('IN_STOCK', 'LOW_STOCK', 'OUT_OF_STOCK', 'UNKNOWN')),
    CONSTRAINT chk_cart_items_quantity_positive CHECK (quantity > 0),
    CONSTRAINT chk_cart_items_unit_price_positive CHECK (unit_price >= 0),
    CONSTRAINT chk_cart_items_price_when_added_positive CHECK (price_when_added >= 0)
);
GO

-- ============================================================================
-- INDEXES: cart_items
-- Optimized for cart operations, price monitoring, and stock validation
-- ============================================================================

-- Index for cart item retrieval (primary access pattern)
CREATE NONCLUSTERED INDEX idx_cart_item_cart
    ON dbo.cart_items (cart_id)
    INCLUDE (product_id, product_name, quantity, unit_price, is_available, stock_status)
    WHERE is_deleted = 0;
GO

-- Index for product lookups across carts (analytics, inventory)
CREATE NONCLUSTERED INDEX idx_cart_item_product
    ON dbo.cart_items (product_id)
    INCLUDE (cart_id, config_id, quantity, unit_price)
    WHERE is_deleted = 0;
GO

-- Index for unique product+config per cart (duplicate detection)
CREATE NONCLUSTERED INDEX idx_cart_item_cart_product
    ON dbo.cart_items (cart_id, product_id, config_id)
    INCLUDE (quantity, unit_price)
    WHERE is_deleted = 0;
GO

-- Index for price drop notification processing
CREATE NONCLUSTERED INDEX idx_cart_item_price_drop
    ON dbo.cart_items (price_dropped, price_drop_notified)
    INCLUDE (cart_id, product_id, price_when_added, current_price)
    WHERE price_dropped = 1 AND price_drop_notified = 0 AND is_deleted = 0;
GO

-- Index for stock status updates (batch processing)
CREATE NONCLUSTERED INDEX idx_cart_item_stock_check
    ON dbo.cart_items (last_price_check_at, is_available)
    INCLUDE (cart_id, product_id, config_id, provider)
    WHERE is_deleted = 0;
GO

-- Index for provider-specific queries
CREATE NONCLUSTERED INDEX idx_cart_item_provider
    ON dbo.cart_items (provider)
    INCLUDE (product_id, config_id, unit_price, stock_status)
    WHERE provider IS NOT NULL AND is_deleted = 0;
GO

-- Index for unavailable items (notification processing)
CREATE NONCLUSTERED INDEX idx_cart_item_unavailable
    ON dbo.cart_items (is_available, stock_status)
    INCLUDE (cart_id, product_id, product_name)
    WHERE is_available = 0 AND is_deleted = 0;
GO

-- Index for soft delete filtering
CREATE NONCLUSTERED INDEX idx_cart_items_is_deleted
    ON dbo.cart_items (is_deleted)
    WHERE is_deleted = 1;
GO

-- ============================================================================
-- DOCUMENTATION
-- ============================================================================

-- Add extended properties for documentation
EXEC sp_addextendedproperty 
    @name = N'MS_Description', 
    @value = N'Shopping cart table supporting both authenticated and guest users. Features abandoned cart detection, price tracking, multi-currency support, and conversion tracking.',
    @level0type = N'SCHEMA', @level0name = N'dbo',
    @level1type = N'TABLE', @level1name = N'carts';
GO

EXEC sp_addextendedproperty 
    @name = N'MS_Description', 
    @value = N'Cart items table with product snapshot, price tracking for price drop notifications, stock status monitoring, and multi-provider support.',
    @level0type = N'SCHEMA', @level0name = N'dbo',
    @level1type = N'TABLE', @level1name = N'cart_items';
GO

EXEC sp_addextendedproperty 
    @name = N'MS_Description', 
    @value = N'Session identifier for guest carts. Used when customer_id is null.',
    @level0type = N'SCHEMA', @level0name = N'dbo',
    @level1type = N'TABLE', @level1name = N'carts',
    @level2type = N'COLUMN', @level2name = N'session_id';
GO

EXEC sp_addextendedproperty 
    @name = N'MS_Description', 
    @value = N'Last activity timestamp used for abandoned cart detection. Updated on any cart modification.',
    @level0type = N'SCHEMA', @level0name = N'dbo',
    @level1type = N'TABLE', @level1name = N'carts',
    @level2type = N'COLUMN', @level2name = N'last_activity_at';
GO

EXEC sp_addextendedproperty 
    @name = N'MS_Description', 
    @value = N'Price when item was first added to cart. Used for price drop detection.',
    @level0type = N'SCHEMA', @level0name = N'dbo',
    @level1type = N'TABLE', @level1name = N'cart_items',
    @level2type = N'COLUMN', @level2name = N'price_when_added';
GO

EXEC sp_addextendedproperty 
    @name = N'MS_Description', 
    @value = N'Current price updated periodically. Compared with price_when_added for price drop detection.',
    @level0type = N'SCHEMA', @level0name = N'dbo',
    @level1type = N'TABLE', @level1name = N'cart_items',
    @level2type = N'COLUMN', @level2name = N'current_price';
GO

-- ============================================================================
-- VERIFICATION
-- ============================================================================

-- Display created objects summary
SELECT 'Table Created' AS [Status], 'carts' AS [Object], COUNT(*) AS [Columns]
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_NAME = 'carts' AND TABLE_SCHEMA = 'dbo'
UNION ALL
SELECT 'Table Created', 'cart_items', COUNT(*)
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_NAME = 'cart_items' AND TABLE_SCHEMA = 'dbo';
GO

-- Display indexes
SELECT 
    t.name AS [Table],
    i.name AS [Index],
    i.type_desc AS [Type]
FROM sys.indexes i
INNER JOIN sys.tables t ON i.object_id = t.object_id
WHERE t.name IN ('carts', 'cart_items')
    AND i.name IS NOT NULL
ORDER BY t.name, i.name;
GO

-- Display constraints
SELECT 
    t.name AS [Table],
    tc.CONSTRAINT_NAME,
    tc.CONSTRAINT_TYPE
FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS tc
INNER JOIN sys.tables t ON tc.TABLE_NAME = t.name
WHERE tc.TABLE_NAME IN ('carts', 'cart_items')
ORDER BY t.name, tc.CONSTRAINT_TYPE, tc.CONSTRAINT_NAME;
GO

PRINT 'Cart and CartItem tables created successfully.';
GO
