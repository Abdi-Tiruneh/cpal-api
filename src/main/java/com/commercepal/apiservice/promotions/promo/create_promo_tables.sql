-- ============================================================================
-- Microsoft SQL Server T-SQL Script
-- Tables: PromoCode, PromoCodeUsage, ProductPromoCode
-- Generated for CommercePal E-Commerce Platform
-- Author: Database Architect
-- Description: Production-ready promo code tables with flexible scope,
--              usage tracking, and product-specific promotions
-- ============================================================================

USE CommercePal;
GO

-- ============================================================================
-- DROP EXISTING OBJECTS (Safe removal with dependency order)
-- ============================================================================

-- Drop foreign key constraints first
IF EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = 'fk_promo_usage_promo_code')
BEGIN
    ALTER TABLE PromoCodeUsage DROP CONSTRAINT fk_promo_usage_promo_code;
END
GO

-- Drop tables in dependency order
IF OBJECT_ID('dbo.PromoCodeUsage', 'U') IS NOT NULL DROP TABLE dbo.PromoCodeUsage;
GO
IF OBJECT_ID('dbo.PromoCode', 'U') IS NOT NULL DROP TABLE dbo.PromoCode;
GO
IF OBJECT_ID('dbo.ProductPromoCode', 'U') IS NOT NULL DROP TABLE dbo.ProductPromoCode;
GO

-- ============================================================================
-- TABLE: PromoCode
-- Description: General promo codes with flexible scope (global, category,
--              product, customer) and usage limits
-- ============================================================================

CREATE TABLE dbo.PromoCode
(
    -- PRIMARY KEY
    id                              BIGINT              IDENTITY(1,1)   NOT NULL,

    -- PROMO CODE DETAILS
    code                            NVARCHAR(50)        NOT NULL,
    discount_type                   NVARCHAR(20)        NOT NULL,
    discount_value                  DECIMAL(15,2)       NOT NULL,
    minimum_order_amount            DECIMAL(15,2)       NULL,

    -- SCOPE CONFIGURATION
    scope                           NVARCHAR(20)        NULL,
    applicable_product_id           BIGINT              NULL,
    applicable_category_id          BIGINT              NULL,
    applicable_customer_id          BIGINT              NULL,

    -- VALIDITY PERIOD
    start_date                      DATETIME2(7)        NULL,
    end_date                        DATETIME2(7)        NULL,

    -- USAGE LIMITS
    total_usage_limit               INT                 NULL,
    per_customer_usage_limit        INT                 NULL,

    -- STATUS
    is_active                       BIT                 NOT NULL        DEFAULT 1,

    -- AUDIT
    created_by                      BIGINT              NULL,
    created_at                      DATETIME2(7)        NULL            DEFAULT GETUTCDATE(),
    updated_at                      DATETIME2(7)        NULL,

    -- CONSTRAINTS
    CONSTRAINT pk_promo_code PRIMARY KEY CLUSTERED (id),
    CONSTRAINT uk_promo_code_code UNIQUE NONCLUSTERED (code),

    -- CHECK CONSTRAINTS
    CONSTRAINT chk_promo_code_discount_type CHECK (discount_type IN ('PERCENTAGE', 'FIXED')),
    CONSTRAINT chk_promo_code_scope CHECK (scope IS NULL OR scope IN ('GLOBAL', 'CATEGORY', 'PRODUCT', 'CUSTOMER')),
    CONSTRAINT chk_promo_code_discount_value_positive CHECK (discount_value >= 0),
    CONSTRAINT chk_promo_code_min_order_positive CHECK (minimum_order_amount IS NULL OR minimum_order_amount >= 0),
    CONSTRAINT chk_promo_code_date_range CHECK (start_date IS NULL OR end_date IS NULL OR start_date <= end_date)
);
GO

-- ============================================================================
-- INDEXES: PromoCode
-- ============================================================================

CREATE NONCLUSTERED INDEX idx_promocode_code
    ON dbo.PromoCode (code);
GO

CREATE NONCLUSTERED INDEX idx_promocode_active
    ON dbo.PromoCode (is_active)
    INCLUDE (code, discount_type, discount_value, start_date, end_date)
    WHERE is_active = 1;
GO

CREATE NONCLUSTERED INDEX idx_promocode_scope
    ON dbo.PromoCode (scope)
    INCLUDE (code, applicable_product_id, applicable_category_id, applicable_customer_id)
    WHERE scope IS NOT NULL;
GO

CREATE NONCLUSTERED INDEX idx_promocode_validity
    ON dbo.PromoCode (start_date, end_date)
    INCLUDE (code, is_active)
    WHERE is_active = 1;
GO

-- ============================================================================
-- TABLE: PromoCodeUsage
-- Description: Tracks promo code usage per customer for enforcement of
--              per-customer usage limits
-- ============================================================================

CREATE TABLE dbo.PromoCodeUsage
(
    -- PRIMARY KEY
    id                              BIGINT              IDENTITY(1,1)   NOT NULL,

    -- RELATIONSHIPS
    promo_code_id                   BIGINT              NOT NULL,
    customer_id                     BIGINT              NULL,

    -- USAGE TRACKING
    usage_count                     INT                 NOT NULL        DEFAULT 0,
    last_used_at                    DATETIME2(7)        NULL,

    -- CONSTRAINTS
    CONSTRAINT pk_promo_code_usage PRIMARY KEY CLUSTERED (id),
    CONSTRAINT fk_promo_usage_promo_code FOREIGN KEY (promo_code_id) REFERENCES dbo.PromoCode(id),

    -- CHECK CONSTRAINTS
    CONSTRAINT chk_promo_usage_count_positive CHECK (usage_count >= 0)
);
GO

-- ============================================================================
-- INDEXES: PromoCodeUsage
-- ============================================================================

CREATE NONCLUSTERED INDEX idx_promo_usage_promo_code
    ON dbo.PromoCodeUsage (promo_code_id)
    INCLUDE (customer_id, usage_count);
GO

CREATE NONCLUSTERED INDEX idx_promo_usage_customer
    ON dbo.PromoCodeUsage (customer_id)
    INCLUDE (promo_code_id, usage_count)
    WHERE customer_id IS NOT NULL;
GO

CREATE UNIQUE NONCLUSTERED INDEX idx_promo_usage_promo_customer
    ON dbo.PromoCodeUsage (promo_code_id, customer_id)
    WHERE customer_id IS NOT NULL;
GO

-- ============================================================================
-- TABLE: ProductPromoCode
-- Description: Product-specific promo codes with merchant/owner tracking
-- ============================================================================

CREATE TABLE dbo.ProductPromoCode
(
    -- PRIMARY KEY
    Id                              BIGINT              IDENTITY(1,1)   NOT NULL,

    -- PROMO CODE DETAILS
    Code                            NVARCHAR(50)        NULL,
    promoCodeDescription            NVARCHAR(MAX)       NULL,
    DiscountType                    NVARCHAR(20)        NULL,
    DiscountAmount                  DECIMAL(15,2)       NULL,

    -- VALIDITY PERIOD
    StartDate                       DATETIME2(7)        NULL,
    EndDate                         DATETIME2(7)        NULL,

    -- STATUS
    PromoCodeStatus                 NVARCHAR(20)        NULL,

    -- PRODUCT ASSOCIATION
    ProductId                       BIGINT              NULL,
    SubProductId                    BIGINT              NULL,

    -- OWNERSHIP
    Owner                           NVARCHAR(20)        NULL,
    MerchantId                      BIGINT              NULL,

    -- AUDIT
    CreatedDate                     DATETIME2(7)        NULL            DEFAULT GETUTCDATE(),
    UpdatedDate                     DATETIME2(7)        NULL,
    UpdatedBy                       BIGINT              NULL,

    -- CONSTRAINTS
    CONSTRAINT pk_product_promo_code PRIMARY KEY CLUSTERED (Id),
    CONSTRAINT uk_product_promo_code_code UNIQUE NONCLUSTERED (Code),

    -- CHECK CONSTRAINTS
    CONSTRAINT chk_product_promo_discount_type CHECK (DiscountType IS NULL OR DiscountType IN ('PERCENTAGE', 'FIXED')),
    CONSTRAINT chk_product_promo_status CHECK (PromoCodeStatus IS NULL OR PromoCodeStatus IN ('ACTIVE', 'INACTIVE', 'EXPIRED', 'DELETED')),
    CONSTRAINT chk_product_promo_owner CHECK (Owner IS NULL OR Owner IN ('MERCHANT', 'PLATFORM', 'ADMIN'))
);
GO

-- ============================================================================
-- INDEXES: ProductPromoCode
-- ============================================================================

CREATE NONCLUSTERED INDEX idx_product_promo_code
    ON dbo.ProductPromoCode (Code)
    WHERE Code IS NOT NULL;
GO

CREATE NONCLUSTERED INDEX idx_product_promo_product
    ON dbo.ProductPromoCode (ProductId)
    INCLUDE (Code, DiscountType, DiscountAmount, PromoCodeStatus)
    WHERE ProductId IS NOT NULL;
GO

CREATE NONCLUSTERED INDEX idx_product_promo_merchant
    ON dbo.ProductPromoCode (MerchantId)
    INCLUDE (Code, ProductId, PromoCodeStatus)
    WHERE MerchantId IS NOT NULL;
GO

CREATE NONCLUSTERED INDEX idx_product_promo_status
    ON dbo.ProductPromoCode (PromoCodeStatus)
    INCLUDE (Code, ProductId, StartDate, EndDate)
    WHERE PromoCodeStatus = 'ACTIVE';
GO

-- ============================================================================
-- DOCUMENTATION
-- ============================================================================

EXEC sp_addextendedproperty 
    @name = N'MS_Description', 
    @value = N'General promo codes with flexible scope (GLOBAL, CATEGORY, PRODUCT, CUSTOMER) and per-customer usage limits.',
    @level0type = N'SCHEMA', @level0name = N'dbo',
    @level1type = N'TABLE', @level1name = N'PromoCode';
GO

EXEC sp_addextendedproperty 
    @name = N'MS_Description', 
    @value = N'Tracks promo code usage per customer for enforcement of usage limits.',
    @level0type = N'SCHEMA', @level0name = N'dbo',
    @level1type = N'TABLE', @level1name = N'PromoCodeUsage';
GO

EXEC sp_addextendedproperty 
    @name = N'MS_Description', 
    @value = N'Product-specific promo codes with merchant/platform ownership tracking.',
    @level0type = N'SCHEMA', @level0name = N'dbo',
    @level1type = N'TABLE', @level1name = N'ProductPromoCode';
GO

-- ============================================================================
-- VERIFICATION
-- ============================================================================

SELECT 'Table Created' AS [Status], name AS [Object], 
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS c WHERE c.TABLE_NAME = t.name) AS [Columns]
FROM sys.tables t
WHERE t.name IN ('PromoCode', 'PromoCodeUsage', 'ProductPromoCode')
ORDER BY t.name;
GO

SELECT 
    t.name AS [Table],
    i.name AS [Index],
    i.type_desc AS [Type]
FROM sys.indexes i
INNER JOIN sys.tables t ON i.object_id = t.object_id
WHERE t.name IN ('PromoCode', 'PromoCodeUsage', 'ProductPromoCode')
    AND i.name IS NOT NULL
ORDER BY t.name, i.name;
GO

PRINT 'PromoCode tables created successfully.';
GO
