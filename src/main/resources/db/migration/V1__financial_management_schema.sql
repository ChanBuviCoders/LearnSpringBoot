IF NOT EXISTS (SELECT 1 FROM sys.schemas WHERE name = 'finance')
    EXEC('CREATE SCHEMA finance');

-- Legacy authentication tables retained while the UI is migrated.
IF OBJECT_ID('dbo.UserGroup', 'U') IS NULL
CREATE TABLE dbo.UserGroup (
    userGroupId BIGINT IDENTITY(1,1) PRIMARY KEY,
    userGroupName NVARCHAR(100) NULL
);

IF OBJECT_ID('dbo.UserAccount', 'U') IS NULL
CREATE TABLE dbo.UserAccount (
    userAccountId BIGINT IDENTITY(1,1) PRIMARY KEY,
    userName NVARCHAR(100) NULL,
    password NVARCHAR(255) NULL,
    firstName NVARCHAR(100) NULL,
    lastName NVARCHAR(100) NULL,
    gender NVARCHAR(30) NULL,
    fathersName NVARCHAR(150) NULL,
    email NVARCHAR(200) NULL,
    marriedStatus NVARCHAR(30) NULL,
    occupation NVARCHAR(100) NULL,
    qualification NVARCHAR(100) NULL,
    panNumber NVARCHAR(20) NULL,
    address NVARCHAR(500) NULL,
    adharImagePath NVARCHAR(500) NULL,
    panImagePath NVARCHAR(500) NULL,
    zipcode NVARCHAR(20) NULL,
    city NVARCHAR(100) NULL,
    state NVARCHAR(100) NULL,
    dob DATE NULL,
    mobileNumber BIGINT NULL,
    altMobileNumber BIGINT NULL,
    annualIncome BIGINT NULL,
    lastLoginDate DATE NULL,
    currentLoginDate DATE NULL,
    adharNumber BIGINT NULL,
    userGroupId BIGINT NULL,
    loginAttempt INT NULL,
    isActive BIT NOT NULL DEFAULT 1
);

IF OBJECT_ID('dbo.NavigationMenu', 'U') IS NULL
CREATE TABLE dbo.NavigationMenu (
    menuId BIGINT IDENTITY(1,1) PRIMARY KEY,
    menuName NVARCHAR(100) NULL
);

IF OBJECT_ID('dbo.ClientDocuments', 'U') IS NULL
CREATE TABLE dbo.ClientDocuments (
    fileId BIGINT IDENTITY(1,1) PRIMARY KEY,
    fileName NVARCHAR(255) NULL,
    fileSize NVARCHAR(50) NULL,
    fileType NVARCHAR(100) NULL,
    filePath NVARCHAR(500) NULL,
    uploadedBy NVARCHAR(100) NULL,
    uploadedDate DATE NULL,
    userAccountId BIGINT NOT NULL
);

IF OBJECT_ID('dbo.CustomerList', 'U') IS NULL
CREATE TABLE dbo.CustomerList (
    customerId BIGINT IDENTITY(1,1) PRIMARY KEY,
    firstName NVARCHAR(100) NULL,
    lastName NVARCHAR(100) NULL,
    gender NVARCHAR(30) NULL,
    loanAmount BIGINT NULL,
    loanType NVARCHAR(30) NULL,
    mobileNumber BIGINT NULL,
    startDate DATE NULL,
    endDate DATE NULL,
    userAccountId BIGINT NULL,
    totalPayable BIGINT NULL,
    totalPaid BIGINT NULL
);

IF OBJECT_ID('dbo.Payments', 'U') IS NULL
CREATE TABLE dbo.Payments (
    paymentId BIGINT IDENTITY(1,1) PRIMARY KEY,
    date DATE NULL,
    amount BIGINT NULL,
    paymentStatus BIT NULL DEFAULT 0,
    paymentMode NVARCHAR(30) NULL DEFAULT 'offline',
    customerId BIGINT NULL,
    loanType NVARCHAR(30) NULL,
    CONSTRAINT FK_LegacyPayments_Customer
        FOREIGN KEY (customerId) REFERENCES dbo.CustomerList(customerId)
);

-- Core customer and lending model.
IF OBJECT_ID('finance.customers', 'U') IS NULL
CREATE TABLE finance.customers (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    customer_code NVARCHAR(30) NOT NULL UNIQUE,
    first_name NVARCHAR(100) NOT NULL,
    last_name NVARCHAR(100) NULL,
    mobile_number NVARCHAR(20) NOT NULL,
    alternate_mobile_number NVARCHAR(20) NULL,
    email NVARCHAR(200) NULL,
    gender NVARCHAR(30) NULL,
    date_of_birth DATE NULL,
    address_line1 NVARCHAR(250) NULL,
    address_line2 NVARCHAR(250) NULL,
    city NVARCHAR(100) NULL,
    state NVARCHAR(100) NULL,
    postal_code NVARCHAR(20) NULL,
    identification_type NVARCHAR(50) NULL,
    identification_number NVARCHAR(100) NULL,
    status NVARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    version BIGINT NOT NULL DEFAULT 0,
    created_by NVARCHAR(100) NOT NULL,
    created_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
    updated_by NVARCHAR(100) NOT NULL,
    updated_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
    deleted BIT NOT NULL DEFAULT 0
);

IF OBJECT_ID('finance.loan_products', 'U') IS NULL
CREATE TABLE finance.loan_products (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    product_code NVARCHAR(30) NOT NULL UNIQUE,
    product_name NVARCHAR(150) NOT NULL,
    collection_frequency NVARCHAR(20) NOT NULL,
    interest_method NVARCHAR(40) NOT NULL,
    default_interest_rate DECIMAL(9,4) NOT NULL,
    default_term_count INT NULL,
    min_amount DECIMAL(19,2) NULL,
    max_amount DECIMAL(19,2) NULL,
    active BIT NOT NULL DEFAULT 1,
    version BIGINT NOT NULL DEFAULT 0,
    created_by NVARCHAR(100) NOT NULL,
    created_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
    updated_by NVARCHAR(100) NOT NULL,
    updated_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME()
);

IF OBJECT_ID('finance.loans', 'U') IS NULL
CREATE TABLE finance.loans (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    loan_number NVARCHAR(40) NOT NULL UNIQUE,
    customer_id BIGINT NOT NULL,
    loan_product_id BIGINT NOT NULL,
    collection_frequency NVARCHAR(20) NOT NULL,
    interest_method NVARCHAR(40) NOT NULL,
    principal_amount DECIMAL(19,2) NOT NULL,
    interest_rate DECIMAL(9,4) NOT NULL,
    interest_amount DECIMAL(19,2) NOT NULL DEFAULT 0,
    disbursed_amount DECIMAL(19,2) NOT NULL,
    collection_amount DECIMAL(19,2) NULL,
    term_count INT NULL,
    remaining_term_count INT NULL,
    start_date DATE NOT NULL,
    maturity_date DATE NULL,
    outstanding_principal DECIMAL(19,2) NOT NULL,
    outstanding_interest DECIMAL(19,2) NOT NULL DEFAULT 0,
    total_collected DECIMAL(19,2) NOT NULL DEFAULT 0,
    status NVARCHAR(30) NOT NULL DEFAULT 'PENDING',
    approved_by NVARCHAR(100) NULL,
    approved_at DATETIME2 NULL,
    version BIGINT NOT NULL DEFAULT 0,
    created_by NVARCHAR(100) NOT NULL,
    created_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
    updated_by NVARCHAR(100) NOT NULL,
    updated_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
    CONSTRAINT FK_Loans_Customer FOREIGN KEY (customer_id) REFERENCES finance.customers(id),
    CONSTRAINT FK_Loans_Product FOREIGN KEY (loan_product_id) REFERENCES finance.loan_products(id)
);

IF OBJECT_ID('finance.loan_schedules', 'U') IS NULL
CREATE TABLE finance.loan_schedules (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    loan_id BIGINT NOT NULL,
    installment_number INT NOT NULL,
    due_date DATE NOT NULL,
    principal_due DECIMAL(19,2) NOT NULL DEFAULT 0,
    interest_due DECIMAL(19,2) NOT NULL DEFAULT 0,
    total_due DECIMAL(19,2) NOT NULL,
    paid_amount DECIMAL(19,2) NOT NULL DEFAULT 0,
    status NVARCHAR(30) NOT NULL DEFAULT 'PENDING',
    paid_at DATETIME2 NULL,
    version BIGINT NOT NULL DEFAULT 0,
    created_by NVARCHAR(100) NOT NULL,
    created_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
    updated_by NVARCHAR(100) NOT NULL,
    updated_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
    CONSTRAINT UQ_LoanSchedule UNIQUE (loan_id, installment_number),
    CONSTRAINT FK_Schedule_Loan FOREIGN KEY (loan_id) REFERENCES finance.loans(id)
);

IF OBJECT_ID('finance.collections', 'U') IS NULL
CREATE TABLE finance.collections (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    transaction_reference NVARCHAR(80) NOT NULL UNIQUE,
    loan_id BIGINT NOT NULL,
    customer_id BIGINT NOT NULL,
    schedule_id BIGINT NULL,
    collection_date DATE NOT NULL,
    due_amount DECIMAL(19,2) NOT NULL,
    paid_amount DECIMAL(19,2) NOT NULL,
    principal_amount DECIMAL(19,2) NOT NULL DEFAULT 0,
    interest_amount DECIMAL(19,2) NOT NULL DEFAULT 0,
    outstanding_balance DECIMAL(19,2) NOT NULL,
    payment_mode NVARCHAR(30) NOT NULL,
    collector_id BIGINT NULL,
    remarks NVARCHAR(500) NULL,
    status NVARCHAR(30) NOT NULL DEFAULT 'COMPLETED',
    reversal_of_id BIGINT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    created_by NVARCHAR(100) NOT NULL,
    created_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
    updated_by NVARCHAR(100) NOT NULL,
    updated_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
    CONSTRAINT FK_Collection_Loan FOREIGN KEY (loan_id) REFERENCES finance.loans(id),
    CONSTRAINT FK_Collection_Customer FOREIGN KEY (customer_id) REFERENCES finance.customers(id),
    CONSTRAINT FK_Collection_Schedule FOREIGN KEY (schedule_id) REFERENCES finance.loan_schedules(id),
    CONSTRAINT FK_Collection_Reversal FOREIGN KEY (reversal_of_id) REFERENCES finance.collections(id)
);

-- Running chit model.
IF OBJECT_ID('finance.chit_schemes', 'U') IS NULL
CREATE TABLE finance.chit_schemes (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    chit_code NVARCHAR(40) NOT NULL UNIQUE,
    chit_name NVARCHAR(150) NOT NULL,
    chit_amount DECIMAL(19,2) NOT NULL,
    member_count INT NOT NULL,
    monthly_contribution DECIMAL(19,2) NOT NULL,
    start_date DATE NOT NULL,
    duration_months INT NOT NULL,
    current_round INT NOT NULL DEFAULT 0,
    status NVARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    version BIGINT NOT NULL DEFAULT 0,
    created_by NVARCHAR(100) NOT NULL,
    created_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
    updated_by NVARCHAR(100) NOT NULL,
    updated_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME()
);

IF OBJECT_ID('finance.chit_members', 'U') IS NULL
CREATE TABLE finance.chit_members (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    chit_scheme_id BIGINT NOT NULL,
    customer_id BIGINT NOT NULL,
    member_number INT NOT NULL,
    joined_date DATE NOT NULL,
    status NVARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    total_contributed DECIMAL(19,2) NOT NULL DEFAULT 0,
    outstanding_amount DECIMAL(19,2) NOT NULL DEFAULT 0,
    version BIGINT NOT NULL DEFAULT 0,
    created_by NVARCHAR(100) NOT NULL,
    created_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
    updated_by NVARCHAR(100) NOT NULL,
    updated_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
    CONSTRAINT UQ_ChitMemberNumber UNIQUE (chit_scheme_id, member_number),
    CONSTRAINT UQ_ChitCustomer UNIQUE (chit_scheme_id, customer_id),
    CONSTRAINT FK_ChitMember_Scheme FOREIGN KEY (chit_scheme_id) REFERENCES finance.chit_schemes(id),
    CONSTRAINT FK_ChitMember_Customer FOREIGN KEY (customer_id) REFERENCES finance.customers(id)
);

IF OBJECT_ID('finance.chit_rounds', 'U') IS NULL
CREATE TABLE finance.chit_rounds (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    chit_scheme_id BIGINT NOT NULL,
    round_number INT NOT NULL,
    collection_date DATE NOT NULL,
    total_collection DECIMAL(19,2) NOT NULL DEFAULT 0,
    winner_member_id BIGINT NULL,
    bid_amount DECIMAL(19,2) NULL,
    deduction_amount DECIMAL(19,2) NOT NULL DEFAULT 0,
    prize_amount DECIMAL(19,2) NULL,
    payout_reference NVARCHAR(80) NULL,
    payout_date DATE NULL,
    status NVARCHAR(30) NOT NULL DEFAULT 'OPEN',
    version BIGINT NOT NULL DEFAULT 0,
    created_by NVARCHAR(100) NOT NULL,
    created_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
    updated_by NVARCHAR(100) NOT NULL,
    updated_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
    CONSTRAINT UQ_ChitRound UNIQUE (chit_scheme_id, round_number),
    CONSTRAINT FK_ChitRound_Scheme FOREIGN KEY (chit_scheme_id) REFERENCES finance.chit_schemes(id),
    CONSTRAINT FK_ChitRound_Winner FOREIGN KEY (winner_member_id) REFERENCES finance.chit_members(id)
);

IF OBJECT_ID('finance.chit_contributions', 'U') IS NULL
CREATE TABLE finance.chit_contributions (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    chit_round_id BIGINT NOT NULL,
    chit_member_id BIGINT NOT NULL,
    due_amount DECIMAL(19,2) NOT NULL,
    paid_amount DECIMAL(19,2) NOT NULL DEFAULT 0,
    paid_date DATE NULL,
    payment_mode NVARCHAR(30) NULL,
    transaction_reference NVARCHAR(80) NULL,
    collector_id BIGINT NULL,
    remarks NVARCHAR(500) NULL,
    status NVARCHAR(30) NOT NULL DEFAULT 'PENDING',
    version BIGINT NOT NULL DEFAULT 0,
    created_by NVARCHAR(100) NOT NULL,
    created_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
    updated_by NVARCHAR(100) NOT NULL,
    updated_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
    CONSTRAINT UQ_RoundMemberContribution UNIQUE (chit_round_id, chit_member_id),
    CONSTRAINT FK_Contribution_Round FOREIGN KEY (chit_round_id) REFERENCES finance.chit_rounds(id),
    CONSTRAINT FK_Contribution_Member FOREIGN KEY (chit_member_id) REFERENCES finance.chit_members(id)
);

-- Configurable rules and immutable audit history.
IF OBJECT_ID('finance.business_settings', 'U') IS NULL
CREATE TABLE finance.business_settings (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    setting_key NVARCHAR(120) NOT NULL UNIQUE,
    setting_value NVARCHAR(1000) NOT NULL,
    value_type NVARCHAR(30) NOT NULL,
    category NVARCHAR(50) NOT NULL,
    description NVARCHAR(500) NULL,
    active BIT NOT NULL DEFAULT 1,
    version BIGINT NOT NULL DEFAULT 0,
    created_by NVARCHAR(100) NOT NULL,
    created_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
    updated_by NVARCHAR(100) NOT NULL,
    updated_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME()
);

IF OBJECT_ID('finance.audit_logs', 'U') IS NULL
CREATE TABLE finance.audit_logs (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    entity_type NVARCHAR(120) NOT NULL,
    entity_id NVARCHAR(80) NOT NULL,
    action NVARCHAR(30) NOT NULL,
    old_values NVARCHAR(MAX) NULL,
    new_values NVARCHAR(MAX) NULL,
    performed_by NVARCHAR(100) NOT NULL,
    performed_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
    ip_address NVARCHAR(64) NULL,
    correlation_id NVARCHAR(80) NULL
);

IF OBJECT_ID('finance.roles', 'U') IS NULL
CREATE TABLE finance.roles (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    role_code NVARCHAR(40) NOT NULL UNIQUE,
    role_name NVARCHAR(100) NOT NULL,
    description NVARCHAR(300) NULL,
    active BIT NOT NULL DEFAULT 1
);

IF OBJECT_ID('finance.permissions', 'U') IS NULL
CREATE TABLE finance.permissions (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    permission_code NVARCHAR(80) NOT NULL UNIQUE,
    description NVARCHAR(300) NULL
);

IF OBJECT_ID('finance.role_permissions', 'U') IS NULL
CREATE TABLE finance.role_permissions (
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    PRIMARY KEY (role_id, permission_id),
    CONSTRAINT FK_RolePermission_Role FOREIGN KEY (role_id) REFERENCES finance.roles(id),
    CONSTRAINT FK_RolePermission_Permission FOREIGN KEY (permission_id) REFERENCES finance.permissions(id)
);

IF OBJECT_ID('finance.user_roles', 'U') IS NULL
CREATE TABLE finance.user_roles (
    user_account_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_account_id, role_id),
    CONSTRAINT FK_UserRole_User FOREIGN KEY (user_account_id) REFERENCES dbo.UserAccount(userAccountId),
    CONSTRAINT FK_UserRole_Role FOREIGN KEY (role_id) REFERENCES finance.roles(id)
);

CREATE INDEX IX_Loans_Customer_Status ON finance.loans(customer_id, status);
CREATE INDEX IX_Schedules_Due_Status ON finance.loan_schedules(due_date, status);
CREATE INDEX IX_Collections_Date ON finance.collections(collection_date);
CREATE INDEX IX_Collections_Collector ON finance.collections(collector_id, collection_date);
CREATE INDEX IX_ChitContributions_Status ON finance.chit_contributions(status);
CREATE INDEX IX_AuditLogs_Entity ON finance.audit_logs(entity_type, entity_id, performed_at);

INSERT INTO finance.roles (role_code, role_name, description)
SELECT seed.role_code, seed.role_name, seed.description
FROM (VALUES
    ('ADMIN', 'Admin', 'Full system administration'),
    ('MANAGER', 'Manager', 'Approvals and operational management'),
    ('COLLECTOR', 'Collector', 'Loan and chit collections'),
    ('ACCOUNTANT', 'Accountant', 'Financial review and reporting'),
    ('VIEWER', 'Viewer', 'Read-only access')
) seed(role_code, role_name, description)
WHERE NOT EXISTS (
    SELECT 1 FROM finance.roles existing WHERE existing.role_code = seed.role_code
);

INSERT INTO finance.business_settings
    (setting_key, setting_value, value_type, category, description, created_by, updated_by)
SELECT seed.setting_key, seed.setting_value, seed.value_type, seed.category, seed.description, 'SYSTEM', 'SYSTEM'
FROM (VALUES
    ('DAILY_DEFAULT_INTEREST_RATE', '12.00', 'DECIMAL', 'LOAN', 'Default upfront daily-loan interest percentage'),
    ('DAILY_DEFAULT_TERM_COUNT', '100', 'INTEGER', 'LOAN', 'Default number of daily installments'),
    ('WEEKLY_DEFAULT_INTEREST_RATE', '20.00', 'DECIMAL', 'LOAN', 'Default flat weekly-loan interest percentage'),
    ('WEEKLY_DEFAULT_TERM_COUNT', '12', 'INTEGER', 'LOAN', 'Default number of weekly installments'),
    ('MONTHLY_DEFAULT_INTEREST_RATE', '2.00', 'DECIMAL', 'LOAN', 'Default monthly reducing-balance interest percentage'),
    ('CHIT_ALLOW_MULTIPLE_MEMBERSHIPS', 'false', 'BOOLEAN', 'CHIT', 'Allow one customer to occupy multiple slots in a chit'),
    ('TRANSACTION_REVERSAL_REQUIRES_MANAGER', 'true', 'BOOLEAN', 'SECURITY', 'Require manager approval for transaction reversals')
) seed(setting_key, setting_value, value_type, category, description)
WHERE NOT EXISTS (
    SELECT 1 FROM finance.business_settings existing WHERE existing.setting_key = seed.setting_key
);
