# Loan EMI Scheduler - API Documentation

Welcome to the **Loan EMI Scheduler API**. This documentation provides comprehensive details for integrating with the loan management system, covering borrower registration, loan application, automated strategy selection, approval workflows, and EMI payments.

---

## Getting Started

### Base URL
All API requests must be made to the following base URL:
`http://localhost:8080`

### Authentication & Security
The API uses **JWT (JSON Web Token)** for authentication.
1.  **Obtain Token**: Use the `/api/v1/auth/login` endpoint.
2.  **Authorize**: Include the token in the `Authorization` header for all protected requests.
    `Authorization: Bearer <your_jwt_token>`

### Roles and Access Control (RBAC)
| Role | Description |
| :--- | :--- |
| **BORROWER** | Create applications, view personal profile, view schedules, and make EMI payments. |
| **LOAN_OFFICER** | Review applications, process decisions (approve/reject), and view all system payments. |
| **ADMIN** | Full system access, including registering new LOAN_OFFICER accounts. |

---

## Common Response Format
The API returns a consistent response structure for both success and error cases.

```json
{
  "status": "OK | CREATED | BAD_REQUEST | UNAUTHORIZED",
  "message": "Human-readable message explaining the result",
  "timestamp": "2026-03-31T20:00:00.000Z",
  "path": "/api/v1/resource",
  "data": { ... } // Optional: Contains the requested resource or result
}
```

---

## 1. Authentication Endpoints

### 1.1 Login
Authenticate a user and retrieve a JWT token.
- **Method**: `POST`
- **URL**: `/api/v1/auth/login`
- **Payload**:
  ```json
  {
    "email": "user@example.com",
    "password": "securePassword123"
  }
  ```

### 1.2 Register Borrower
Create a new borrower profile.
- **Method**: `POST`
- **URL**: `/api/v1/auth/register/borrower`
- **Payload**:
  ```json
  {
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@example.com",
    "password": "password123",
    "phone": "9876543210",
    "pan": "ABCDE1234F",
    "monthlyIncome": 75000.00
  }
  ```

---

## 2. Loan Application Endpoints

### 2.1 Submit Application
Apply for a new loan. The system assesses eligibility based on DTI and existing loans.
- **Method**: `POST`
- **URL**: `/api/v1/loan/applications/apply`
- **Payload**:
  ```json
  {
    "requestedAmount": 500000.00,
    "tenureMonths": 24,
    "existingEmi": 5000.00
  }
  ```

### 2.2 List Applications
Retrieve a paginated list of applications. Borrowers see their own, while Officers/Admins can see all applications (optionally filtered by status).
- **Method**: `GET`
- **URL**: `/api/v1/loan/applications?pageNumber=0&pageSize=10&status=PENDING`

### 2.3 Get Application by Code
Retrieve basic response details for a specific application.
- **Method**: `GET`
- **URL**: `/api/v1/loan/applications/{applicationCode}`

### 2.4 Get Detailed Application
Retrieve comprehensive application data including borrower profile and identity verification (partially masked).
- **Method**: `GET`
- **URL**: `/api/v1/loan/applications/{applicationCode}/details`

---

## 3. Loan Management Endpoints

### 3.1 List My Loans
Retrieve all active and closed loans for the authenticated borrower.
- **Method**: `GET`
- **URL**: `/api/v1/loans/my`

### 3.2 Get Loan Details
Retrieve the specific details of a loan record.
- **Method**: `GET`
- **URL**: `/api/v1/loans/{loanCode}`

### 3.3 Get Loan Summary
Retrieve a financial summary of the loan, including next due date and outstanding balance.
- **Method**: `GET`
- **URL**: `/api/v1/loans/{loanCode}/summary`

### 3.4 Get Repayment Schedule
Retrieve the complete list of EMI installments (past, current, and future).
- **Method**: `GET`
- **URL**: `/api/v1/loans/{loanCode}/schedule`

### 3.5 Master Loan Directory (Officer/Admin only)
Retrieve a paginated list of all loans in the system.
- **Method**: `GET`
- **URL**: `/api/v1/loans/all?pageNumber=0&pageSize=10&status=ACTIVE`

### 3.6 Process Application Decision (Officer/Admin only)
Review and approve or reject a pending loan application.
- **Method**: `PUT`
- **URL**: `/api/v1/loans/applications/{applicationCode}/decision`
- **Payload**:
  ```json
  {
    "status": "APPROVED",
    "interestRate": 10.5,
    "officerStrategy": "REDUCING_BALANCE",
    "remarks": "Stable income and good credit history."
  }
  ```

### 3.7 Update Loan Status (Officer/Admin only)
Update the operational status of a loan (e.g., mark as CLOSED or DEFAULTED).
- **Method**: `PATCH`
- **URL**: `/api/v1/loans/{loanCode}/status`
- **Payload**:
  ```json
  {
    "status": "CLOSED"
  }
  ```

---

## 4. Payment Endpoints

### 4.1 Process EMI Payment
Pay a specific EMI installment using various payment modes.
- **Method**: `POST`
- **URL**: `/api/v1/payments/pay`
- **Payload (UPI Example)**:
  ```json
  {
    "emiId": "uuid-v4",
    "amount": 2500.00,
    "paymentMode": "UPI",
    "methodDetails": {
      "type": "UPI",
      "upiId": "borrower@okhdfc"
    }
  }
  ```

### 4.2 Loan Payment History
Retrieve all payments made against a specific loan.
- **Method**: `GET`
- **URL**: `/api/v1/payments/loan/{loanCode}`

### 4.3 Personal Payment History
Retrieve the complete payment history for the logged-in borrower across all their loans.
- **Method**: `GET`
- **URL**: `/api/v1/payments/history`

### 4.4 Master Payment Records (Officer/Admin only)
Retrieve system-wide payment records.
- **Method**: `GET`
- **URL**: `/api/v1/payments/all`

---

## 5. Auditing and Monitoring Endpoints (Officer/Admin only)

### 5.1 Entity Audit History
Retrieve the change logs for a specific entity (e.g., LOAN, APPLICATION, USER).
- **Method**: `GET`
- **URL**: `/api/v1/audit/entity/{entityType}/{entityId}`
- **EntityTypes**: `APPLICATION`, `LOAN`, `USER`, `EMI_SCHEDULE`, `PAYMENT`.

### 5.2 Strategy Override History
Retrieve all instances where an officer overrode the system-suggested EMI strategy.
- **Method**: `GET`
- **URL**: `/api/v1/audit/strategy-overrides`

### 5.3 Master Audit Log
Retrieve the complete system audit trail.
- **Method**: `GET`
- **URL**: `/api/v1/audit/all`

---

## Enum References

### Application Status
- `APPLIED`: Application received.
- `PENDING`: Waiting for officer review.
- `APPROVED`: Loan granted.
- `REJECTED`: Loan application denied.

### Loan Status
- `ACTIVE`: Loan is currently in repayment.
- `CLOSED`: Loan has been fully repaid.
- `DEFAULTED`: Borrower has missed critical payments.

### EMI Status
- `PENDING`: Installment not yet due.
- `PAID`: Installment successfully paid.
- `OVERDUE`: Installment past due date.

### EMI Strategies
- `FLAT_RATE`: Base interest on full principal.
- `REDUCING_BALANCE`: Interest on remaining principal.
- `STEP_UP`: Increasing EMI over tenure.

### Payment Modes
- `UPI`
- `CARD`
- `NET_BANKING`

---

## Error Codes & Handling
The system returns a `BAD_REQUEST` (400) for validation failures or business rule violations.

| Case | Error Message Example |
| :--- | :--- |
| **DTI High** | "Application REJECTED: High debt-to-income ratio." |
| **Max Loans** | "Borrower cannot have more than 3 active loans." |
| **Unauthorized** | "Access Denied: You do not have permission to view this schedule." |
| **Already Processed**| "Only PENDING applications can be processed." |
| **Validation** | "PAN must be exactly 10 characters." |

---
*Documentation Generated on: 2026-03-31*
