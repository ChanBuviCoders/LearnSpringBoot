INSERT INTO finance.business_settings
    (setting_key, setting_value, value_type, category, description, created_by, updated_by)
SELECT seed.setting_key, seed.setting_value, seed.value_type, seed.category, seed.description, 'SYSTEM', 'SYSTEM'
FROM (VALUES
    ('DEFAULT_CURRENCY', 'INR', 'STRING', 'ORGANISATION', 'Default display and transaction currency'),
    ('TIMEZONE', 'Asia/Kolkata', 'STRING', 'ORGANISATION', 'Business timezone'),
    ('GRACE_PERIOD_DAYS', '0', 'INTEGER', 'LOAN', 'Default payment grace period'),
    ('DEFAULT_LOAN_DURATION', '12', 'INTEGER', 'LOAN', 'Default loan duration'),
    ('DEFAULT_CHIT_AMOUNT', '100000.00', 'DECIMAL', 'CHIT', 'Default chit amount'),
    ('DEFAULT_CHIT_MEMBERS', '20', 'INTEGER', 'CHIT', 'Default chit member count'),
    ('RECEIPT_PREFIX', 'RCT', 'STRING', 'ORGANISATION', 'Receipt reference prefix')
) seed(setting_key, setting_value, value_type, category, description)
WHERE NOT EXISTS (
    SELECT 1 FROM finance.business_settings existing WHERE existing.setting_key = seed.setting_key
);

INSERT INTO finance.permissions (permission_code, description)
SELECT seed.permission_code, seed.description
FROM (VALUES
    ('FINANCE_READ', 'Read financial records and dashboard'),
    ('COLLECTION_WRITE', 'Post loan and chit collections'),
    ('FINANCE_WRITE', 'Create and maintain financial records'),
    ('FINANCE_APPROVE', 'Approve loans, winners, payouts and reversals'),
    ('REPORT_READ', 'Run and export financial reports'),
    ('SETTINGS_READ', 'Read business settings'),
    ('SETTINGS_WRITE', 'Change business settings'),
    ('AUDIT_READ', 'Read immutable audit history')
) seed(permission_code, description)
WHERE NOT EXISTS (
    SELECT 1 FROM finance.permissions existing WHERE existing.permission_code = seed.permission_code
);

INSERT INTO finance.role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM finance.roles r
JOIN finance.permissions p ON
       r.role_code = 'ADMIN'
    OR r.role_code = 'MANAGER' AND p.permission_code IN
       ('FINANCE_READ','COLLECTION_WRITE','FINANCE_WRITE','FINANCE_APPROVE','REPORT_READ','SETTINGS_READ','SETTINGS_WRITE','AUDIT_READ')
    OR r.role_code = 'ACCOUNTANT' AND p.permission_code IN
       ('FINANCE_READ','FINANCE_WRITE','REPORT_READ','SETTINGS_READ','AUDIT_READ')
    OR r.role_code = 'COLLECTOR' AND p.permission_code IN
       ('FINANCE_READ','COLLECTION_WRITE','FINANCE_WRITE')
    OR r.role_code = 'VIEWER' AND p.permission_code IN
       ('FINANCE_READ','REPORT_READ')
WHERE NOT EXISTS (
    SELECT 1 FROM finance.role_permissions existing
    WHERE existing.role_id = r.id AND existing.permission_id = p.id
);

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = 'UX_ChitContribution_Reference'
      AND object_id = OBJECT_ID('finance.chit_contributions')
)
CREATE UNIQUE INDEX UX_ChitContribution_Reference
ON finance.chit_contributions(transaction_reference)
WHERE transaction_reference IS NOT NULL;

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = 'UX_ChitPayout_Reference'
      AND object_id = OBJECT_ID('finance.chit_rounds')
)
CREATE UNIQUE INDEX UX_ChitPayout_Reference
ON finance.chit_rounds(payout_reference)
WHERE payout_reference IS NOT NULL;
