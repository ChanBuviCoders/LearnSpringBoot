# Financial Data Maintenance & Collection Management System

## 1. Project Overview

This project is a **Financial Data Maintenance and Collection Management System** designed to manage customer loans, interest calculations, daily/weekly/monthly collections, payment history, and running chit/committee schemes.

The system should maintain complete financial records for each customer and provide accurate tracking of loan amounts, interest, principal payments, outstanding balances, collections, and chit transactions.

---

# 2. Customer Types

Each client/customer can have one of the following three collection types:

1. **Daily**
2. **Weekly**
3. **Monthly**

The customer's collection type determines:

* Loan calculation
* Interest calculation
* Collection frequency
* Collection amount
* Principal repayment
* Interest repayment
* Outstanding balance
* Payment schedule

---

# 3. Daily Loan Type

For customers under the **Daily** category, the interest amount is deducted from the initial loan amount.

### Example

Loan amount:

**₹10,000**

Interest:

**12%**

Interest amount:

**₹1,200**

Amount given to customer:

**₹10,000 - ₹1,200 = ₹8,800**

The customer then repays the principal amount through daily collections.

### Example Daily Collection

If the repayment period requires ₹100 per day:

* Daily collection = ₹100
* Collection frequency = Every day
* Principal outstanding decreases with each payment
* Every payment must be recorded with date and transaction details

The system should maintain:

* Original loan amount
* Interest percentage
* Interest amount
* Disbursed amount
* Daily collection amount
* Number of collection days
* Amount collected
* Principal collected
* Outstanding principal
* Remaining collection days
* Payment history

---

# 4. Weekly Loan Type

For the **Weekly** category, the customer receives the full loan amount initially.

The customer repays the loan through weekly collections.

### Example

Loan amount:

**₹10,000**

Repayment period:

**12 weeks**

Interest:

Based on the configured weekly interest rate.

The system should calculate the applicable interest and generate a weekly repayment schedule.

### Weekly Collection

For every week, the system should maintain:

* Week number
* Collection date
* Principal amount
* Interest amount
* Total collection amount
* Paid amount
* Outstanding amount
* Payment status

The system should support different interest rates and repayment periods based on the configured loan product.

---

# 5. Monthly Loan Type

For the **Monthly** category, the customer primarily pays the monthly interest until the principal amount is repaid.

### Example

Principal:

**₹1,00,000**

Monthly interest:

**2%**

Monthly interest:

**₹2,000**

The customer pays:

**₹2,000 per month**

until the principal is completely repaid.

When the customer makes a principal payment, the outstanding principal should be reduced and future interest should be calculated based on the applicable outstanding principal.

### Example

Initial principal:

₹1,00,000

Monthly interest:

2%

Monthly interest payment:

₹2,000

If the customer pays ₹20,000 towards principal:

Remaining principal:

₹80,000

Next month's interest:

2% of ₹80,000 = ₹1,600

The system should maintain complete monthly payment history.

---

# 6. Loan Management

The system should support complete loan lifecycle management.

### Loan Creation

When creating a loan, maintain:

* Customer
* Loan type
* Loan amount
* Interest rate
* Interest calculation method
* Loan start date
* Repayment period
* Collection frequency
* Collection amount
* Principal amount
* Interest amount
* Disbursed amount
* Outstanding amount
* Loan status

### Loan Status

Possible statuses:

* Pending
* Approved
* Active
* Partially Paid
* Completed
* Overdue
* Closed
* Cancelled

---

# 7. Collection Management

The system should provide a collection module for recording customer payments.

For every collection, maintain:

* Customer
* Loan
* Collection type
* Collection date
* Due amount
* Paid amount
* Principal amount
* Interest amount
* Outstanding balance
* Payment mode
* Transaction/reference number
* Collector/employee
* Remarks

The system should automatically calculate the remaining balance after every payment.

---

# 8. Payment History

Every financial transaction must be maintained as a permanent history.

The system should provide:

* Customer-wise payment history
* Loan-wise payment history
* Daily collection history
* Weekly collection history
* Monthly collection history
* Date-wise collection report
* Employee/collector-wise collection report
* Interest collection report
* Principal collection report
* Outstanding amount report

No completed transaction should be deleted without proper authorization and audit tracking.

---

# 9. Running Chit Management

In addition to loans, the system should maintain **running chit/committee schemes**.

The system should support different chit amounts, for example:

* ₹1,00,000
* ₹2,00,000
* ₹5,00,000
* ₹10,00,000
* ₹15,00,000

The system should allow configurable chit amounts and should not be restricted to these examples.

---

# 10. Chit Members

A running chit can have multiple members.

Example:

**Chit Amount:** ₹1,00,000

**Number of Members:** 20

The system should maintain:

* Chit ID
* Chit amount
* Number of members
* Member details
* Monthly contribution
* Chit start date
* Chit duration
* Current month/round
* Member payment status
* Amount collected
* Amount pending
* Chit winner/bid information, if applicable
* Prize/chit amount
* Chit payment history
* Outstanding amount

---

# 11. Running Chit History

The system must maintain complete historical information for every running chit.

For each month/round, maintain:

* Round number
* Collection date
* Total collection
* Member-wise contribution
* Member payment status
* Winner/bidder
* Chit amount paid to winner
* Applicable deductions
* Remaining balance
* Pending members
* Transaction details

The system should allow users to view the complete history of a chit from its starting date.

---

# 12. Customer Financial Profile

Each customer should have a centralized financial profile containing:

### Customer Information

* Customer ID
* Name
* Contact details
* Address
* Identification details
* Customer status

### Loan Information

* Active loans
* Completed loans
* Outstanding loans
* Total borrowed amount
* Total principal paid
* Total interest paid
* Outstanding principal

### Chit Information

* Active chits
* Completed chits
* Monthly contributions
* Pending contributions
* Chit history

---

# 13. Dashboard

The system should provide a financial dashboard showing:

* Total customers
* Active customers
* Active loans
* Total loan amount
* Total amount collected
* Total principal collected
* Total interest collected
* Total outstanding amount
* Today's collection
* Today's pending collection
* Daily collection
* Weekly collection
* Monthly collection
* Active chits
* Total chit collection
* Pending chit payments

---

# 14. Reports

The system should provide downloadable reports such as:

### Loan Reports

* Customer-wise loan report
* Active loan report
* Completed loan report
* Outstanding loan report
* Overdue loan report

### Collection Reports

* Daily collection report
* Weekly collection report
* Monthly collection report
* Date-range collection report
* Collector-wise collection report

### Financial Reports

* Principal collection
* Interest collection
* Outstanding balance
* Customer financial statement

### Chit Reports

* Active chit report
* Member-wise chit report
* Monthly chit collection
* Pending chit payments
* Complete chit history

Reports should support filtering by:

* Date
* Customer
* Loan type
* Chit
* Collector
* Status

---

# 15. Business Rules

The application should use configurable business rules instead of hard-coded calculations.

For example:

* Interest percentage should be configurable.
* Loan duration should be configurable.
* Collection frequency should be configurable.
* Daily/weekly/monthly calculation methods should be configurable.
* Chit amount should be configurable.
* Number of chit members should be configurable.
* Payment schedules should be generated automatically.
* Outstanding balances should be recalculated after every payment.
* All financial transactions should maintain an audit history.

---

# 16. Audit and Security

Because this system handles financial information, every important operation should be tracked.

Maintain:

* Created by
* Created date/time
* Updated by
* Updated date/time
* Payment created by
* Payment date/time
* Transaction reference
* Modification history

User roles can include:

* Admin
* Manager
* Collector
* Accountant
* Viewer

Each role should have appropriate permissions.

---

# 17. Main Modules

The application should contain the following major modules:

1. **Dashboard**
2. **Customer Management**
3. **Loan Management**
4. **Daily Collection**
5. **Weekly Collection**
6. **Monthly Collection**
7. **Payment Management**
8. **Running Chit Management**
9. **Chit Member Management**
10. **Reports**
11. **User & Role Management**
12. **Audit History**
13. **Settings / Business Configuration**

---

## 18. Technology Direction

The application can be designed as a modern full-stack business application with:

### Frontend

* Angular
* Angular Material
* Reactive Forms
* REST API integration

### Backend

* Java
* Spring Boot
* Spring Data JPA
* REST APIs
* Spring Security

### Database

* SQL Server

### File Storage

* Azure Blob Storage for documents and attachments

The system should be designed with a **scalable database structure, configurable financial calculations, proper transaction handling, and complete audit history**.
