# SQL Server Setup

## Option 1 - Run the executable script in SSMS (recommended first install)

1. Open SQL Server Management Studio (or Azure Data Studio).
2. Connect to your local SQL Server instance.
3. Open `D:\chandran\docs\sql\financial_management_executable.sql`.
4. Press **Execute (F5)**.

The script is safe to re-run. It creates:

- database `financial_management`
- schema `finance`
- login tables (`UserAccount`, `UserGroup`, …)
- customers, loan products, loans, schedules, collections
- chits, settings, audit, roles, permissions, attachments
- default loan products (Daily / Weekly / Monthly)
- admin login and Flyway version rows so the Spring Boot app will not recreate the same objects

After a successful run, log in to the app with:

- **Username:** `admin`
- **Password:** `Admin@123`

Change that password after first login.

## Option 2 - Let Flyway create the schema

If you skip the SSMS script, start Spring Boot against an empty database. Flyway applies:

- `V1__financial_management_schema.sql`
- `V2__financial_phase_3_5_seed.sql`
- `V3__finance_attachments.sql`
- `V4__seed_login_products_and_indexes.sql`

Do **not** mix a half-run Flyway migrate with a later SSMS script on the same database unless you know the Flyway history already matches.

## Environment

```powershell
$env:SPRING_PROFILES_ACTIVE = "dev"
$env:DB_URL = "jdbc:sqlserver://localhost:1433;databaseName=financial_management;encrypt=true;trustServerCertificate=true"
$env:DB_USERNAME = "sa"
$env:DB_PASSWORD = "<local-password>"
$env:JWT_SECRET = "<at-least-64-random-characters>"
$env:JAVA_HOME = "D:\Softwares\jdk-17.0.6"
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
cd D:\chandran\LearnSpringBoot
mvn -DskipTests spring-boot:run
```

## Migration rules

- Never edit a Flyway file that has already run in a shared environment.
- Add later changes as `V5__description.sql` and keep `docs/sql/financial_management_executable.sql` in sync if you still use SSMS.
- Production uses `spring.jpa.hibernate.ddl-auto=validate`.
- Posted financial transactions must be reversed, never deleted.
