# API GUIDE

This guide provides a deep-dive into the API endpoints, their access requirements, and the underlying service implementation logic, including database actions, side effects, and calculations.

---

## 1. Authentication & Account Management (`AuthService`)

| Endpoint | Method | Role | Service Logic & Detailed Functionality |
| :--- | :--- | :--- | :--- |
| `/login` | `POST` | Public | **Auth Initiation:** Validates email existence. Authenticates via `AuthenticationManager`. Generates a 6-digit OTP via `OtpService` (stored in `otp_tokens`). Triggers `NotificationService` to send the OTP email. Returns a challenge message. |
| `/register/borrower` | `POST` | Public | **Borrower Creation:** Hashes the PAN for privacy/lookup. Creates a new `User` (role: BORROWER, status: INACTIVE). Initializes a `BorrowerProfile` (zero loans, custom income). Generates/sends registration OTP. Logs a `CREATE` audit action. |
| `/verify-registration`| `POST` | Public | **Account Activation:** Validates OTP against the database. If correct, flips `is_active` to TRUE. Triggers a "Welcome" notification. Logs an `UPDATE` audit action. |
| `/verify-login` | `POST` | Public | **2FA Completion:** Validates OTP. Sets `SecurityContext`. Generates a final JWT (valid for 24h). Dispatches a "Login Alert" notification. Logs a `LOGIN` audit action. |
| `/me` | `GET` | Auth | **Session Integrity:** Fetches current user metadata using the security context. Useful for UI role-checking and profile synchronization. |
| `/delete` | `DELETE` | Admin | **Soft Delete:** Flips user to `deleted=true` and `is_active=false`. Records `deleted_at` timestamp and `deleted_by` (Admin ID). Deactivates associated `EmployeeProfile` if the user was staff. |
| `/recover` | `POST` | Admin | **State Restoration:** Restores a soft-deleted account by resetting status flags. Re-authenticates the user and returns a fresh JWT. Logs an `UPDATE` audit action. |

---

## 2. Loan Lifecycle (`LoanService`)

| Endpoint | Method | Role | Service Logic & Detailed Functionality |
| :--- | :--- | :--- | :--- |
| `/` | `GET` | Borrower | **Personal Portfolio:** Queries `LoanRepository` for all records where `borrower_email` matches the session. Supports pagination and sorting by start date. |
| `/{loanCode}` | `GET` | All | **Instance Lookup:** Fetches the complete entity graph for a specific loan code. Includes principal, active interest rate, and dates. |
| `/{loanCode}/summary` | `GET` | Borrower | **Financial Position:** Calculates "Total Paid to Date" and "Principal Remaining". Queries the `EMI_Schedule` table to find the single *earliest* installment with `PENDING` status. |
| `/{loanCode}/schedule`| `GET` | All | **Repayment Matrix:** Full fetch of all `EmiSchedule` rows for the loan. Returns principal/interest breakdowns and payment status (PAID, OVERDUE, etc.). |
| `/all` | `GET` | Staff | **Master Ledger:** Global paginated search across all loans. Allows staff to filter by `LoanStatus` (ACTIVE, CLOSED, etc.). |
| `/applications/{code}/decision`| `PUT` | Officer | **Credit Adjudication:** Validates the reviewer is NOT the borrower. Checks system strategy engine status. Persists officer remarks. If **APPROVED**: Triggers `generateAndPersistLoan` which builds the `Loan` entity and generates the full 60-month (max) schedule in a single transaction. Logs `StrategyAudit` if the officer overrode the system's suggested rate. |
| `/{loanCode}/status`| `PATCH` | Officer | **Manual Override:** Allows staff to manually flag a loan as `DEFAULTED` or `PAID_OFF` if edge cases occur. Logs both old and new states to the audit log. |
| `/{loanCode}/audit-history`| `GET` | Staff | **Change Tracking:** Fetches all rows from `audit_logs` where `entity_type = 'LOAN'` and `entity_id` matches the target loan. Essential for compliance review. |

---

## 3. Loan Applications (`LoanApplicationService`)

| Endpoint | Method | Role | Service Logic & Detailed Functionality |
| :--- | :--- | :--- | :--- |
| `/apply` | `POST` | Borrower | **Validation & Scoring:** Enforces system limits (₹5M max, 60m tenure). Checks concurrency limit (Max 3 active/pending). Calls `DtiCalculationEngine` (Debt-to-Income). If DTI > 0.5, the `StrategySelectionEngine` tags it for **AUTO-REJECTION**. Logs the submission and sends an alert to the user. |
| `/` | `GET` | All | **Smart Listing:** Staff see the global list sorted by application date. Borrowers are automatically filtered to see only their own submissions. |
| `/{applicationCode}/details`| `GET` | Staff | **Official Appraisal:** Deep-fetches the borrower's full profile, financial metrics (income, existing EMI), and PII (masked PAN) for decision-making. |
| `/{applicationCode}/withdraw`| `PATCH` | Borrower | **Cancellation:** Only valid if status is `PENDING`. Updates status to `WITHDRAWN`. Sends cancellation notification. Logs as an `UPDATE` audit action. |

---

## 4. Payment Processing (`PaymentService`)

| Endpoint | Method | Role | Service Logic & Detailed Functionality |
| :--- | :--- | :--- | :--- |
| `/pay` | `POST` | Borrower | **Sequential Enforcement:** Validates that previous installments are PAID before processing the current one. Prevents overpayment. Uses `PaymentStrategyFactory` to process specific gateway logic (UPI/Card). On **SUCCESS**: Updates `EmiSchedule` status; if all installments are clear, it triggers a `LoanStatus.CLOSED` update automatically. |
| `/foreclose` | `POST` | Borrower | **Early Settlement:** Calculates (Remaining Principal + Outstanding Interest). On successful payment, it **waives and deletes all future/unaccrued EMI records** and marks the loan as `CLOSED` immediately. |
| `/history/my` | `GET` | Borrower | **Wallet Activity:** Fetches all payment attempts (Successful or Failed) for the user. aggregates total successful payments for transparency. |
| `/history/all` | `GET` | Staff | **Master Cashflow:** Comprehensive ledger of every payment transaction attempted system-wide. |

---

## 5. Staff & System Performance (`EmployeeService` / `AuditService`)

| Endpoint | Method | Role | Service Logic & Detailed Functionality |
| :--- | :--- | :--- | :--- |
| `/dashboard` | `GET` | Staff | **Operational Pulse:** Quick counts of `ACTIVE` loans, `PENDING` applications, and total disbursed amounts across the entire system. |
| `/admin/register/officer`| `POST` | Admin | **Staff Provisioning:** Creates an active `User` account with `LOAN_OFFICER` authority. Initializes an `EmployeeProfile` tracking salary and joining date. |
| `/audit/all` | `GET` | Staff | **Global Audit:** Accesses the `audit_logs` table. Returns actor, action, timestamp, and JSON representations of the modified objects (Old vs New). |
| `/audit/strategy-overrides`| `GET` | Staff | **Policy Compliance:** Specifically filters audit logs for instances where an officer deviated from the automated interest rate recommendation. |
