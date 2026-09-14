# Financial Management System Implementation Plan

The application is being rebuilt as a normalized financial system. Existing
authentication and Azure document-storage code will be retained temporarily;
legacy customer and payment tables are not the source of truth for new modules.

Implementation status: all six planned phases are implemented. Environment
certificate trust must be configured before dependency-backed test and package
commands can finish on this workstation.

## Phase 1 - Platform and database foundation

- SQL Server schema managed by Flyway
- Auditable base entities and immutable audit log
- Configurable business settings
- Seed roles and default financial rules
- API namespace standardized under `/api/v2`

## Phase 2 - Customers, loans, and collections

- Customer master and financial profile
- Loan products and configurable calculation methods
- Daily upfront-interest loans
- Weekly flat-interest loans
- Monthly reducing-balance interest loans
- Generated schedules, payment allocation, reversals, and outstanding balances
- Angular customer, loan, and collection screens

## Phase 3 - Running chits

- Chit schemes and member enrollment
- Monthly rounds and contribution tracking
- Winner/bid, deduction, prize payout, and complete chit history
- Angular chit administration and collection screens

## Phase 4 - Dashboard and reports

- Operational and financial dashboard metrics
- Filtered loan, collection, outstanding, and chit reports
- Customer financial statement
- CSV/XLSX export endpoints and Angular report pages

## Phase 5 - Administration and hardening

- Admin, Manager, Collector, Accountant, and Viewer permissions
- Method-level API authorization and role-aware navigation
- Audit-history browser
- Business-settings UI
- Validation, optimistic locking, soft deletion, and transaction reversal controls

## Phase 6 - Verification and delivery

- Unit tests for every calculation strategy
- Service and controller integration tests
- Angular component/service tests
- Local SQL Server setup guide, environment templates, and deployment checklist

## API conventions

- New resources use REST endpoints below `/api/v2`.
- Money uses `decimal(19,2)` / `BigDecimal`; percentages use `decimal(9,4)`.
- Dates use ISO `yyyy-MM-dd`; timestamps are stored as UTC `datetime2`.
- Posted financial transactions are never deleted. Corrections are reversals
  referencing the original transaction.
- Mutable records use optimistic locking through a `version` column.
