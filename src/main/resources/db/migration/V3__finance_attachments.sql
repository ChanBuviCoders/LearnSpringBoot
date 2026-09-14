IF OBJECT_ID('finance.attachments', 'U') IS NULL
CREATE TABLE finance.attachments (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    entity_type NVARCHAR(80) NOT NULL,
    entity_id NVARCHAR(80) NOT NULL,
    file_name NVARCHAR(255) NOT NULL,
    content_type NVARCHAR(120) NULL,
    file_size BIGINT NULL,
    storage_path NVARCHAR(500) NOT NULL,
    uploaded_by NVARCHAR(100) NOT NULL,
    uploaded_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME()
);

CREATE INDEX IX_Attachments_Entity ON finance.attachments(entity_type, entity_id);
