-- Guard indexes if an earlier migration created tables but failed before indexes.
IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = 'IX_Loans_Customer_Status' AND object_id = OBJECT_ID('finance.loans')
)
CREATE INDEX IX_Loans_Customer_Status ON finance.loans(customer_id, status);

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = 'IX_Schedules_Due_Status' AND object_id = OBJECT_ID('finance.loan_schedules')
)
CREATE INDEX IX_Schedules_Due_Status ON finance.loan_schedules(due_date, status);

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = 'IX_Collections_Date' AND object_id = OBJECT_ID('finance.collections')
)
CREATE INDEX IX_Collections_Date ON finance.collections(collection_date);

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = 'IX_Collections_Collector' AND object_id = OBJECT_ID('finance.collections')
)
CREATE INDEX IX_Collections_Collector ON finance.collections(collector_id, collection_date);

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = 'IX_ChitContributions_Status' AND object_id = OBJECT_ID('finance.chit_contributions')
)
CREATE INDEX IX_ChitContributions_Status ON finance.chit_contributions(status);

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = 'IX_AuditLogs_Entity' AND object_id = OBJECT_ID('finance.audit_logs')
)
CREATE INDEX IX_AuditLogs_Entity ON finance.audit_logs(entity_type, entity_id, performed_at);

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = 'IX_Attachments_Entity' AND object_id = OBJECT_ID('finance.attachments')
)
CREATE INDEX IX_Attachments_Entity ON finance.attachments(entity_type, entity_id);

IF NOT EXISTS (SELECT 1 FROM dbo.UserGroup WHERE userGroupName = N'Admin')
    INSERT INTO dbo.UserGroup (userGroupName) VALUES (N'Admin');

IF NOT EXISTS (SELECT 1 FROM dbo.UserAccount WHERE userName = N'admin')
INSERT INTO dbo.UserAccount (
    userName, password, firstName, lastName, email, userGroupId, isActive, loginAttempt
)
SELECT N'admin', N'Admin@123', N'System', N'Admin', N'admin@local',
       (SELECT TOP 1 userGroupId FROM dbo.UserGroup WHERE userGroupName = N'Admin' ORDER BY userGroupId),
       1, 0;

INSERT INTO finance.user_roles (user_account_id, role_id)
SELECT u.userAccountId, r.id
FROM dbo.UserAccount u
JOIN finance.roles r ON r.role_code = N'ADMIN'
WHERE u.userName = N'admin'
  AND NOT EXISTS (
        SELECT 1 FROM finance.user_roles ur
        WHERE ur.user_account_id = u.userAccountId AND ur.role_id = r.id
  );

INSERT INTO finance.loan_products (
    product_code, product_name, collection_frequency, interest_method,
    default_interest_rate, default_term_count, min_amount, max_amount, active,
    version, created_by, updated_by
)
SELECT seed.product_code, seed.product_name, seed.collection_frequency, seed.interest_method,
       seed.default_interest_rate, seed.default_term_count, seed.min_amount, seed.max_amount, 1,
       0, N'SYSTEM', N'SYSTEM'
FROM (VALUES
    (N'DAILY-UPFRONT', N'Daily loan', N'DAILY', N'UPFRONT', 12.0000, 100, 1000.00, 100000.00),
    (N'WEEKLY-FLAT', N'Weekly loan', N'WEEKLY', N'FLAT', 20.0000, 12, 1000.00, 200000.00),
    (N'MONTHLY-REDUCING', N'Monthly loan', N'MONTHLY', N'REDUCING_BALANCE', 2.0000, 12, 5000.00, 500000.00)
) seed(product_code, product_name, collection_frequency, interest_method,
      default_interest_rate, default_term_count, min_amount, max_amount)
WHERE NOT EXISTS (
    SELECT 1 FROM finance.loan_products existing WHERE existing.product_code = seed.product_code
);
