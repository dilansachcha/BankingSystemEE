# 🏦 BankingSystemEE – Complete Enterprise Java Banking System

An **end-to-end secure banking system** built using **Jakarta EE 10**, **EJB**, **JPA (Hibernate)**, **JTA Transactions**, **Timer Services**, **WebSockets**, and **Admin-Customer Role Based Security**. Designed to handle **manual transfers**, **scheduled and recurring transactions**, **interest calculations**, **fixed deposit maturity handling**, **real-time admin notifications**, and **PDF reporting**, with a **strong focus on security, concurrency, and time-based business services**.

---

## 📌 Project Highlights

✅ Session-based login with role-specific dashboards (Customer & Admin)  
✅ **Admin approval workflow** with **email notifications**  
✅ **Manual Transfers** with balance and status validations  
✅ **Scheduled & Recurring Transfers** (Daily/Weekly cycles with retry logic)  
✅ **Daily Interest Application** and **Fixed Deposit Maturity Updates** using **EJB Timers**  
✅ **Real-time Notifications via WebSocket** for Admin  
✅ **PDF Receipt Generation** for transactions and admin reports  
✅ **Audit Logging, Performance Tracking, and General Method Logging** using Interceptors  
✅ **High volume, concurrency-safe transaction handling**

---

## 📚 Technologies Stack

| Layer              | Technology                                                                 |
|---------------------|---------------------------------------------------------------------------|
| Backend             | Jakarta EE 10, EJB (Stateless, Singleton), JPA (Hibernate), Servlets      |
| Database            | MySQL, JPA Entity Mapping, Transactions (JTA, BMT, CMT)                   |
| Frontend            | JSP, JSTL, HTML5                                                          |
| Real-time Messaging | WebSockets (Admin Notification System)                                    |
| Email Services      | JavaMail API                                                              |
| Scheduling          | EJB Timer Service with @Schedule                                          |
| Logging             | Custom Interceptors (Audit, Performance, General Logging), GlassFish logs |
| Server              | GlassFish 7.0.23                                                          |

---

## 📁 Project Structure

```plaintext
BankingSystemEE/
│
├── model/               # JPA Entity Classes
├── service/             # Stateless EJB Business Logic
├── singleton/           # Timer Polling & Scheduled Tasks
├── interceptor/         # Logging, Performance, Audit Interceptors
├── security/            # Session-based Access Control
├── servlet/             # Web Interface - JSP Controllers
├── webapp/              # JSP Pages, Static Resources
└── META-INF/
    └── persistence.xml  # EclipseLink (instead of Hibernate) & JPA Configuration
```

---

## 📝 Core Features Breakdown

### ✅ **Customer Operations**
- Register account → Pending status → Approval required
- Reset password via **email verification**
- View dashboard: list of all accounts (Checking, Savings, Fixed Deposit)
- **Manual Fund Transfers** between accounts with proper validations
- **Scheduled Transfers** (One-time and Recurring with retry logic)
- **Transaction History View** with **PDF download** (for debit transactions)
- **Create Fixed Deposits**, **Premature Closure** with interest forfeiture, **Withdraw after Maturity**
- **Real-time transaction logs**, **live balance updates**
- **Logout** securely invalidates session

### ✅ **Admin Features**
- Login to **admin dashboard**
- Approve/reject registered customers → Approval email sent
- Block/unblock customer accounts → Email notifications with reasons
- View all system transactions with filtering options
- Download **PDF transaction reports** with custom date filters
- Receive **real-time notifications** via WebSocket:
    - New customer registrations
    - High-value transactions (>= Rs. 50,000)
- Admin-only transaction insights and PDF export

---

## 🔐 **Security Architecture**

- **@RolesAllowed** used extensively in EJBs to secure methods
- Servlet session security ensures unauthorized access prevention
- **AppIdentityStore and AuthMechanism** for login handling
- JSP session guards for restricting direct page access
- Different pages and functionality for **Customer** and **Admin** roles
- Manual logout invalidates session completely

---

## ⏲️ **Timer Services**

| Timer Service | Purpose |
|-----------------|---------|
| `TimerSessionBean` | Daily interest application at 00:00 hrs and Fixed Deposit Maturity status updater at 00:30 hrs |
| `ScheduledTransactionPollingBean` | Polls every 5 minutes to process due scheduled/recurring transfers |
| **Failure Handling** | Retry failed transactions 3 times; mark `FAILED` after 3 unsuccessful attempts |
| **Downtime Recovery** | Any due transaction or interest is caught up immediately after server restarts |

---

## 📊 **Logging & Interceptor Coverage**

| Interceptor               | Role |
|---------------------------|------|
| `@Audit`                  | Logs sensitive actions such as transfers, closures, admin activities |
| `@Performance`            | Logs execution time of critical methods |
| `@Logging`                | Logs entry and exit of EJB methods |
| GlassFish Application Logs | Full system logs including exceptions, rollbacks, timer executions |

---

## 📊 **Interest System Logic**

| Account Type  | Daily Interest Rate | Special Rules |
|----------------|---------------------|----------------|
| CHECKING       | 0.5% annually       | No minimum balance restriction |
| SAVINGS        | 4% annually         | Minimum balance Rs. 1000 enforced |
| FIXED DEPOSIT  | 10% annually        | Locked until maturity or premature closure |

---

## 💰 **Fixed Deposit Management**

- **Premature Closure**: Refund initial deposit only, interest forfeited
- **Matured Withdrawal**: Refund initial deposit + full interest earned
- **Status Auto-Updates**: MATURED/CLOSED handled by timer service

---

## 📨 **Email Notifications**

| Event | Recipient | Purpose |
|--------|-----------|---------|
| Registration | Customer | Pending account approval notice |
| Approval | Customer | Active status notification |
| Password Reset | Customer | Verification code for reset |
| Block/Unblock | Customer | Inform about status change with reason |
| High-Value Transfer | Admin & Customer | Notification of large transaction |
| Scheduled Transfers | None | Handled internally via logs |
| Maturity/Interest | None | Automatically handled in balance |

---

## ✅ **Testing Summary**

| Category                 | Tests Included |
|--------------------------|----------------|
| Login/Authentication      | ✅ Valid/Invalid Login, Sessions |
| Registration & Approval   | ✅ Full approval flow |
| Transfers                 | ✅ Manual, Scheduled, Recurring, Failures |
| Scheduled Transfers       | ✅ Retry logic, downtime recovery, recurrence |
| Fixed Deposit Flows       | ✅ Creation, Premature Closure, Maturity |
| Interest Calculation      | ✅ Daily, backdated, downtime recovery |
| Admin Dashboard           | ✅ Live notifications, actions, PDF reports |
| Concurrency Safety        | ✅ Safe multi-threaded transactions |
| Logging & Interceptors    | ✅ Auditing, performance timing logs |
| PDF Reports               | ✅ Debit Receipts, Admin Full Reports |

Test cases included cover **50+ scenarios** tested practically, validated under concurrency and failure conditions.

---

## 📜 **Setup & Deployment Guide**

### 1️⃣ Clone Repository
```bash
git clone https://github.com/dilansachcha/BankingSystemEE.git
```
### 2️⃣ Database Setup

### Create Database:
```sql
CREATE DATABASE bankingsys;
```
- Import provided SQL schema:
- Includes tables: users - accounts - transactions - scheduled_transaction

### 3️⃣ Configure GlassFish
- JDBC Connection Pool → bankingsysPool
- JDBC Resource → jdbc/bankingDS

### 4️⃣ Build Project
- Open in IntelliJ IDEA (or NetBeans)
- Build Maven project and deploy WAR via GlassFish Admin Console

### 5️⃣ Access App
- Customer Dashboard →
```bash http://localhost:8080/BankingSystemEE/index.jsp ```
- Admin Dashboard →
```bash http://localhost:8080/BankingSystemEE/admin-dashboard.jsp ```

---

## 🧪 High-Level Architecture Flow

```plaintext
Customer ➡️ Register ➡️ Admin Approval ➡️ Create Accounts ➡️ Transfer/Schedule Transactions ➡️ Logs Recorded
⬇️
Timer Services ➡️ Interest Updates & Maturity Tracking ➡️ Automatic Withdrawals & Retry Mechanisms
⬇️
Admin Notifications via WebSocket ➡️ PDF Reports ➡️ Secure Session Management
```

---

## 🚀 Project Objectives Covered

- Timer Services for banking operations
- Interceptor usage (logging, auditing)
- Transaction demarcation with both BMT and CMT
- Programmatic security & authorization enforcement
- Exception handling with rollback logic
- Split directory project structure with modular design
- Complete CRUD flows for all banking operations
- Downtime resilience and high concurrency performance

---

## ❤️ Acknowledgements
- This project was developed under Business Component Development II module following enterprise Java best practices with fully functional banking flows, real-time communications, and asynchronous operations using EJB capabilities.

---

## 📝 License
- This project is released under the MIT License [ © 2025 - Dilan Sachintha Manage ] and is intended for **educational purposes**.
