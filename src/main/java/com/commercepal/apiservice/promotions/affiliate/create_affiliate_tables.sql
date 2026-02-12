-- ============================================================================
-- Microsoft SQL Server T-SQL Script
-- Tables: Affiliate, AffiliateReferral, AffiliateCommission, AffiliateWithdrawal
-- Generated for CommercePal E-Commerce Platform
-- Author: Database Architect
-- Description: Production-ready affiliate management tables with referral
--              tracking, commission management, and withdrawal processing
-- ============================================================================

USE CommercePal;
GO

-- ============================================================================
-- DROP EXISTING OBJECTS (Safe removal with dependency order)
-- ============================================================================

-- Drop foreign key constraints first (child tables depend on Affiliate)
IF EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = 'fk_affiliate_referral_affiliate')
BEGIN
    ALTER TABLE AffiliateReferral DROP CONSTRAINT fk_affiliate_referral_affiliate;
END
GO

IF EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = 'fk_affiliate_commission_affiliate')
BEGIN
    ALTER TABLE AffiliateCommission DROP CONSTRAINT fk_affiliate_commission_affiliate;
END
GO

IF EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = 'fk_affiliate_withdrawal_affiliate')
BEGIN
    ALTER TABLE AffiliateWithdrawal DROP CONSTRAINT fk_affiliate_withdrawal_affiliate;
END
GO

IF EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = 'fk_affiliate_credential')
BEGIN
    ALTER TABLE Affiliate DROP CONSTRAINT fk_affiliate_credential;
END
GO

-- Drop tables in dependency order
IF OBJECT_ID('dbo.AffiliateWithdrawal', 'U') IS NOT NULL DROP TABLE dbo.AffiliateWithdrawal;
GO
IF OBJECT_ID('dbo.AffiliateCommission', 'U') IS NOT NULL DROP TABLE dbo.AffiliateCommission;
GO
IF OBJECT_ID('dbo.AffiliateReferral', 'U') IS NOT NULL DROP TABLE dbo.AffiliateReferral;
GO
IF OBJECT_ID('dbo.Affiliate', 'U') IS NOT NULL DROP TABLE dbo.Affiliate;
GO

-- ============================================================================
-- TABLE: Affiliate
-- Description: Affiliate users with referral codes, commission rates, and
--              linked account credentials
-- ============================================================================

CREATE TABLE dbo.Affiliate
(
    -- PRIMARY KEY
    id                              BIGINT              IDENTITY(1,1)   NOT NULL,

    -- REFERRAL CODE
    referral_code                   NVARCHAR(50)        NOT NULL,

    -- PERSONAL INFORMATION
    first_name                      NVARCHAR(100)       NOT NULL,
    last_name                       NVARCHAR(100)       NOT NULL,
    email                           NVARCHAR(255)       NOT NULL,
    phone_number                    NVARCHAR(20)        NOT NULL,

    -- COMMISSION CONFIGURATION
    commission_type                 NVARCHAR(20)        NULL,
    commission_rate                 DECIMAL(10,4)       NULL,

    -- STATUS
    is_active                       BIT                 NOT NULL        DEFAULT 0,

    -- CREDENTIAL LINK
    account_credential_id           BIGINT              NOT NULL,

    -- AUDIT
    created_at                      DATETIME2(7)        NULL            DEFAULT GETUTCDATE(),
    updated_at                      DATETIME2(7)        NULL,

    -- CONSTRAINTS
    CONSTRAINT pk_affiliate PRIMARY KEY CLUSTERED (id),
    CONSTRAINT uk_affiliate_referral_code UNIQUE NONCLUSTERED (referral_code),
    CONSTRAINT uk_affiliate_email UNIQUE NONCLUSTERED (email),
    CONSTRAINT uk_affiliate_phone UNIQUE NONCLUSTERED (phone_number),
    CONSTRAINT uk_affiliate_credential UNIQUE NONCLUSTERED (account_credential_id),
    CONSTRAINT fk_affiliate_credential FOREIGN KEY (account_credential_id) REFERENCES dbo.account_credential(id),

    -- CHECK CONSTRAINTS
    CONSTRAINT chk_affiliate_commission_type CHECK (commission_type IS NULL OR commission_type IN ('PERCENTAGE', 'FIXED')),
    CONSTRAINT chk_affiliate_commission_rate_positive CHECK (commission_rate IS NULL OR commission_rate >= 0)
);
GO

-- ============================================================================
-- INDEXES: Affiliate
-- ============================================================================

CREATE NONCLUSTERED INDEX idx_affiliate_referral_code
    ON dbo.Affiliate (referral_code);
GO

CREATE NONCLUSTERED INDEX idx_affiliate_email
    ON dbo.Affiliate (email);
GO

CREATE NONCLUSTERED INDEX idx_affiliate_phone
    ON dbo.Affiliate (phone_number);
GO

CREATE NONCLUSTERED INDEX idx_affiliate_active
    ON dbo.Affiliate (is_active)
    INCLUDE (referral_code, first_name, last_name, email, commission_type, commission_rate)
    WHERE is_active = 1;
GO

CREATE NONCLUSTERED INDEX idx_affiliate_credential
    ON dbo.Affiliate (account_credential_id);
GO

-- ============================================================================
-- TABLE: AffiliateReferral
-- Description: Tracks referral sessions with attribution window, signup and
--              order conversion tracking
-- ============================================================================

CREATE TABLE dbo.AffiliateReferral
(
    -- PRIMARY KEY
    id                              BIGINT              IDENTITY(1,1)   NOT NULL,

    -- RELATIONSHIPS
    affiliate_id                    BIGINT              NOT NULL,

    -- SESSION IDENTIFICATION
    session_id                      NVARCHAR(128)       NOT NULL,

    -- ANALYTICS / FRAUD REVIEW
    ip_address                      NVARCHAR(64)        NULL,
    user_agent                      NVARCHAR(512)       NULL,
    referred_url                    NVARCHAR(1024)      NULL,

    -- ATTRIBUTION WINDOW
    attribution_window_days         INT                 NOT NULL        DEFAULT 30,

    -- ACTIVITY TRACKING
    first_seen_at                   DATETIME2(7)        NOT NULL        DEFAULT GETUTCDATE(),
    last_seen_at                    DATETIME2(7)        NOT NULL        DEFAULT GETUTCDATE(),
    view_count                      INT                 NOT NULL        DEFAULT 0,

    -- SIGNUP CONVERSION
    customer_id                     BIGINT              NULL,
    signup_converted                BIT                 NOT NULL        DEFAULT 0,
    signup_at                       DATETIME2(7)        NULL,

    -- ORDER CONVERSION
    order_ref                       NVARCHAR(100)       NULL,
    order_amount                    DECIMAL(19,2)       NULL,
    order_converted                 BIT                 NOT NULL        DEFAULT 0,
    order_at                        DATETIME2(7)        NULL,

    -- CONSTRAINTS
    CONSTRAINT pk_affiliate_referral PRIMARY KEY CLUSTERED (id),
    CONSTRAINT uq_affiliate_session UNIQUE NONCLUSTERED (affiliate_id, session_id),
    CONSTRAINT fk_affiliate_referral_affiliate FOREIGN KEY (affiliate_id) REFERENCES dbo.Affiliate(id),

    -- CHECK CONSTRAINTS
    CONSTRAINT chk_referral_attribution_window_positive CHECK (attribution_window_days > 0),
    CONSTRAINT chk_referral_view_count_positive CHECK (view_count >= 0)
);
GO

-- ============================================================================
-- INDEXES: AffiliateReferral
-- ============================================================================

CREATE NONCLUSTERED INDEX idx_affiliate_ref_session
    ON dbo.AffiliateReferral (session_id)
    INCLUDE (affiliate_id, first_seen_at, signup_converted, order_converted);
GO

CREATE NONCLUSTERED INDEX idx_affiliate_ref_affiliate
    ON dbo.AffiliateReferral (affiliate_id)
    INCLUDE (session_id, signup_converted, order_converted, order_amount);
GO

CREATE NONCLUSTERED INDEX idx_affiliate_ref_customer
    ON dbo.AffiliateReferral (customer_id)
    INCLUDE (affiliate_id, signup_at, order_converted)
    WHERE customer_id IS NOT NULL;
GO

CREATE NONCLUSTERED INDEX idx_affiliate_ref_last_seen
    ON dbo.AffiliateReferral (last_seen_at)
    INCLUDE (affiliate_id, session_id, attribution_window_days);
GO

CREATE NONCLUSTERED INDEX idx_affiliate_ref_conversion
    ON dbo.AffiliateReferral (signup_converted, order_converted)
    INCLUDE (affiliate_id, customer_id, order_amount);
GO

CREATE NONCLUSTERED INDEX idx_affiliate_ref_first_seen
    ON dbo.AffiliateReferral (first_seen_at)
    INCLUDE (affiliate_id, attribution_window_days)
    WHERE signup_converted = 0;
GO

-- ============================================================================
-- TABLE: AffiliateCommission
-- Description: Commission records for affiliates with support for signup
--              bonuses and order-based commissions
-- ============================================================================

CREATE TABLE dbo.AffiliateCommission
(
    -- PRIMARY KEY
    id                              BIGINT              IDENTITY(1,1)   NOT NULL,

    -- RELATIONSHIPS
    affiliate_id                    BIGINT              NOT NULL,

    -- COMMISSION TYPE
    commission_type                 NVARCHAR(20)        NOT NULL,

    -- RELATED ENTITIES
    order_id                        BIGINT              NULL,
    customer_id                     BIGINT              NOT NULL,

    -- AMOUNTS
    base_amount                     DECIMAL(19,4)       NULL,
    commission_amount               DECIMAL(19,4)       NOT NULL,

    -- PAYMENT STATUS
    paid                            BIT                 NOT NULL        DEFAULT 0,

    -- AUDIT
    created_at                      DATETIME2(7)        NOT NULL        DEFAULT GETUTCDATE(),
    updated_at                      DATETIME2(7)        NULL,

    -- CONSTRAINTS
    CONSTRAINT pk_affiliate_commission PRIMARY KEY CLUSTERED (id),
    CONSTRAINT uq_aff_commission_order UNIQUE NONCLUSTERED (affiliate_id, order_id),
    CONSTRAINT uq_aff_commission_signup UNIQUE NONCLUSTERED (affiliate_id, customer_id, commission_type),
    CONSTRAINT fk_affiliate_commission_affiliate FOREIGN KEY (affiliate_id) REFERENCES dbo.Affiliate(id),

    -- CHECK CONSTRAINTS
    CONSTRAINT chk_commission_type CHECK (commission_type IN ('SIGNUP', 'ORDER')),
    CONSTRAINT chk_commission_amount_positive CHECK (commission_amount >= 0)
);
GO

-- ============================================================================
-- INDEXES: AffiliateCommission
-- ============================================================================

CREATE NONCLUSTERED INDEX idx_aff_commission_affiliate
    ON dbo.AffiliateCommission (affiliate_id)
    INCLUDE (commission_type, commission_amount, paid, created_at);
GO

CREATE NONCLUSTERED INDEX idx_aff_commission_order
    ON dbo.AffiliateCommission (order_id)
    INCLUDE (affiliate_id, commission_amount, paid)
    WHERE order_id IS NOT NULL;
GO

CREATE NONCLUSTERED INDEX idx_aff_commission_customer
    ON dbo.AffiliateCommission (customer_id)
    INCLUDE (affiliate_id, commission_type, commission_amount);
GO

CREATE NONCLUSTERED INDEX idx_aff_commission_type
    ON dbo.AffiliateCommission (commission_type)
    INCLUDE (affiliate_id, commission_amount, paid);
GO

CREATE NONCLUSTERED INDEX idx_aff_commission_unpaid
    ON dbo.AffiliateCommission (paid, affiliate_id)
    INCLUDE (commission_amount, created_at)
    WHERE paid = 0;
GO

CREATE NONCLUSTERED INDEX idx_aff_commission_created
    ON dbo.AffiliateCommission (created_at)
    INCLUDE (affiliate_id, commission_type, commission_amount, paid);
GO

-- ============================================================================
-- TABLE: AffiliateWithdrawal
-- Description: Withdrawal requests from affiliates with multiple payment
--              methods and approval workflow
-- ============================================================================

CREATE TABLE dbo.AffiliateWithdrawal
(
    -- PRIMARY KEY
    id                              BIGINT              IDENTITY(1,1)   NOT NULL,

    -- RELATIONSHIPS
    affiliate_id                    BIGINT              NOT NULL,

    -- WITHDRAWAL DETAILS
    amount                          DECIMAL(19,4)       NOT NULL,
    payment_method                  NVARCHAR(50)        NOT NULL,
    account_number                  NVARCHAR(100)       NOT NULL,
    bank_name                       NVARCHAR(100)       NULL,

    -- STATUS
    status                          NVARCHAR(20)        NOT NULL        DEFAULT 'PENDING',

    -- TIMESTAMPS
    requested_at                    DATETIME2(7)        NOT NULL        DEFAULT GETUTCDATE(),
    processed_at                    DATETIME2(7)        NULL,

    -- NOTES
    notes                           NVARCHAR(500)       NULL,
    rejection_reason                NVARCHAR(500)       NULL,
    admin_notes                     NVARCHAR(500)       NULL,

    -- AUDIT
    created_at                      DATETIME2(7)        NOT NULL        DEFAULT GETUTCDATE(),
    updated_at                      DATETIME2(7)        NULL,
    processed_by                    NVARCHAR(100)       NULL,
    created_by                      NVARCHAR(100)       NULL,
    updated_by                      NVARCHAR(100)       NULL,

    -- CONSTRAINTS
    CONSTRAINT pk_affiliate_withdrawal PRIMARY KEY CLUSTERED (id),
    CONSTRAINT uq_aff_withdraw_request UNIQUE NONCLUSTERED (affiliate_id, requested_at),
    CONSTRAINT fk_affiliate_withdrawal_affiliate FOREIGN KEY (affiliate_id) REFERENCES dbo.Affiliate(id),

    -- CHECK CONSTRAINTS
    CONSTRAINT chk_withdrawal_status CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED', 'PAID')),
    CONSTRAINT chk_withdrawal_payment_method CHECK (payment_method IN ('BANK', 'TELEBIRR', 'EBIRR', 'OTHER')),
    CONSTRAINT chk_withdrawal_amount_positive CHECK (amount > 0)
);
GO

-- ============================================================================
-- INDEXES: AffiliateWithdrawal
-- ============================================================================

CREATE NONCLUSTERED INDEX idx_aff_withdraw_affiliate
    ON dbo.AffiliateWithdrawal (affiliate_id)
    INCLUDE (amount, status, requested_at);
GO

CREATE NONCLUSTERED INDEX idx_aff_withdraw_status
    ON dbo.AffiliateWithdrawal (status)
    INCLUDE (affiliate_id, amount, requested_at, payment_method);
GO

CREATE NONCLUSTERED INDEX idx_aff_withdraw_requested
    ON dbo.AffiliateWithdrawal (requested_at)
    INCLUDE (affiliate_id, status, amount);
GO

CREATE NONCLUSTERED INDEX idx_aff_withdraw_pending
    ON dbo.AffiliateWithdrawal (status, requested_at)
    INCLUDE (affiliate_id, amount, payment_method, account_number)
    WHERE status = 'PENDING';
GO

CREATE NONCLUSTERED INDEX idx_aff_withdraw_payment_method
    ON dbo.AffiliateWithdrawal (payment_method)
    INCLUDE (affiliate_id, amount, status)
    WHERE status IN ('APPROVED', 'PAID');
GO

CREATE NONCLUSTERED INDEX idx_aff_withdraw_processed
    ON dbo.AffiliateWithdrawal (processed_at)
    INCLUDE (affiliate_id, status, amount, processed_by)
    WHERE processed_at IS NOT NULL;
GO

-- ============================================================================
-- DOCUMENTATION
-- ============================================================================

EXEC sp_addextendedproperty 
    @name = N'MS_Description', 
    @value = N'Affiliate users with unique referral codes, commission configuration, and linked account credentials.',
    @level0type = N'SCHEMA', @level0name = N'dbo',
    @level1type = N'TABLE', @level1name = N'Affiliate';
GO

EXEC sp_addextendedproperty 
    @name = N'MS_Description', 
    @value = N'Unique referral code for tracking and attribution.',
    @level0type = N'SCHEMA', @level0name = N'dbo',
    @level1type = N'TABLE', @level1name = N'Affiliate',
    @level2type = N'COLUMN', @level2name = N'referral_code';
GO

EXEC sp_addextendedproperty 
    @name = N'MS_Description', 
    @value = N'Referral session tracking with attribution window, view counting, signup and order conversion tracking.',
    @level0type = N'SCHEMA', @level0name = N'dbo',
    @level1type = N'TABLE', @level1name = N'AffiliateReferral';
GO

EXEC sp_addextendedproperty 
    @name = N'MS_Description', 
    @value = N'Number of days within which a conversion is attributed to this referral.',
    @level0type = N'SCHEMA', @level0name = N'dbo',
    @level1type = N'TABLE', @level1name = N'AffiliateReferral',
    @level2type = N'COLUMN', @level2name = N'attribution_window_days';
GO

EXEC sp_addextendedproperty 
    @name = N'MS_Description', 
    @value = N'Commission records for affiliates supporting both signup bonuses and order-based commissions.',
    @level0type = N'SCHEMA', @level0name = N'dbo',
    @level1type = N'TABLE', @level1name = N'AffiliateCommission';
GO

EXEC sp_addextendedproperty 
    @name = N'MS_Description', 
    @value = N'Type of commission: SIGNUP for registration bonus, ORDER for purchase-based commission.',
    @level0type = N'SCHEMA', @level0name = N'dbo',
    @level1type = N'TABLE', @level1name = N'AffiliateCommission',
    @level2type = N'COLUMN', @level2name = N'commission_type';
GO

EXEC sp_addextendedproperty 
    @name = N'MS_Description', 
    @value = N'Withdrawal requests from affiliates with support for multiple Ethiopian payment methods (Bank, TeleBirr, eBirr).',
    @level0type = N'SCHEMA', @level0name = N'dbo',
    @level1type = N'TABLE', @level1name = N'AffiliateWithdrawal';
GO

EXEC sp_addextendedproperty 
    @name = N'MS_Description', 
    @value = N'Payment channel: BANK for traditional bank transfer, TELEBIRR/EBIRR for mobile money.',
    @level0type = N'SCHEMA', @level0name = N'dbo',
    @level1type = N'TABLE', @level1name = N'AffiliateWithdrawal',
    @level2type = N'COLUMN', @level2name = N'payment_method';
GO

-- ============================================================================
-- VERIFICATION
-- ============================================================================

SELECT 'Table Created' AS [Status], name AS [Object], 
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS c WHERE c.TABLE_NAME = t.name) AS [Columns]
FROM sys.tables t
WHERE t.name IN ('Affiliate', 'AffiliateReferral', 'AffiliateCommission', 'AffiliateWithdrawal')
ORDER BY t.name;
GO

SELECT 
    t.name AS [Table],
    i.name AS [Index],
    i.type_desc AS [Type]
FROM sys.indexes i
INNER JOIN sys.tables t ON i.object_id = t.object_id
WHERE t.name IN ('Affiliate', 'AffiliateReferral', 'AffiliateCommission', 'AffiliateWithdrawal')
    AND i.name IS NOT NULL
ORDER BY t.name, i.name;
GO

PRINT 'Affiliate tables created successfully.';
GO
