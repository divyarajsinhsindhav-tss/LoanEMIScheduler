# Loan EMI Scheduler - API Documentation

Welcome to the **Loan EMI Scheduler API Documentation**. This API provides endpoints for user registration, loan application, approval flows, EMI scheduling, and payments.

## Base URL
`http://localhost:8080`

---

## Authentication
Most endpoints require a **Bearer Token** obtained via the Login endpoint.
Include the token in the request header:
`Authorization: Bearer <your_jwt_token>`

---

## Roles and Access Control (RBAC)
The system supports three roles:
1.  **BORROWER**: Can apply for loans, view their own profile, schedule, and pay EMIs.
2.  **LOAN_OFFICER**: Can review applications, approve/reject loans, and view all system payments.
3.  **ADMIN**: Full system access, including creating LOAN_OFFICER accounts.

---

## 1. Authentication Endpoints

### 1.1 Login
Authenticate a user and return a JWT token.

- **Method:** `POST`
- **URL:** `/api/v1/auth/login`
- **Request Body:**
```json
{
  "email": "borrower@example.com",
  "password": "password123"
}
```
- **Success Response (200 OK):**
```json
{
  "status": "OK",
  "message": "Login successful",
  "timestamp": "2026-03-30T19:47:19.123Z",
  "path": "/api/v1/auth/login",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "email": "borrower@example.com",
    "role": "BORROWER",
    "firstName": "John",
    "lastName": "Doe"
  }
}
```

### 1.2 Register Borrower
Create a new borrower account.

- **Method:** `POST`
- **URL:** `/api/v1/auth/register/borrower`
- **Request Body:**
```json
{
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe@example.com",
  "password": "securePassword123",
  "phone": "9876543210",
  "pan": "ABCDE1234F",
  "monthlyIncome": 50000.00
}
```
- **Success Response (201 Created):**
```json
{
  "status": "CREATED",
  "message": "Borrower registered successfully",
  "data": {
    "userId": "uuid-v4",
    "email": "john.doe@example.com",
    "role": "BORROWER"
  }
}
```

---

## 2. Borrower Endpoints

### 2.1 Get Personal Profile
Retrieve the logged-in borrower's profile details.

- **Method:** `GET`
- **URL:** `/api/v1/borrower/profile`
- **Success Response (200 OK):**
```json
{
  "status": "OK",
  "message": "Profile retrieved successfully",
  "data": {
    "email": "john.doe@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "phone": "9876543210",
    "pan": "ABCDE1234F",
    "monthlyIncome": 50000.00,
    "userCode": "BR-1001"
  }
}
```

---

## 3. Loan Application Endpoints

### 3.1 Apply for a Loan
Submit a new loan application.

- **Method:** `POST`
- **URL:** `/api/v1/loan/applications/apply`
- **Request Body:**
```json
{
  "requestedAmount": 500000.00,
  "tenureMonths": 24,
  "existingEmi": 5000.00
}
```
- **Success Response (201 Created):**
```json
{
  "status": "CREATED",
  "message": "Application APP-1234 submitted successfully.",
  "data": {
    "applicationCode": "APP-1234",
    "requestedAmount": 500000.00,
    "tenureMonths": 24,
    "status": "APPLIED"
  }
}
```

### 3.2 List Applications
Get a paginated list of applications (Filtered by status for officers).

- **Method:** `GET`
- **URL:** `/api/v1/loan/applications?pageNumber=0&pageSize=10&status=APPLIED`
- **Success Response (200 OK):**
```json
{
  "status": "OK",
  "message": "Applications retrieved successfully.",
  "data": {
    "content": [...],
    "totalPages": 1,
    "totalElements": 5
  }
}
```

### 3.3 Get Application Details
- **By Code:** `/api/v1/loan/applications/{applicationCode}`
- **Detailed View:** `/api/v1/loan/applications/details/{applicationCode}`

---

## 4. Loan Management (Officer/Admin)

### 4.1 Process Application Decision
Approve or reject a loan application.

- **Method:** `PUT`
- **URL:** `/api/v1/loans/applications/{applicationCode}/decision`
- **Request Body:**
```json
{
  "status": "APPROVED",
  "interestRate": 10.5,
  "officerStrategy": "REDUCING_BALANCE",
  "remarks": "Credit score is good."
}
```
- **Success Response (200 OK):**
```json
{
  "status": "OK",
  "message": "Application decision processed successfully.",
  "data": {
    "loanCode": "L-5567",
    "loanAmount": 500000.00,
    "interestRate": 10.5,
    "status": "ACTIVE"
  }
}
```

### 4.2 Get Loan Summary
Retrieve high-level summary of an active loan.

- **Method:** `GET`
- **URL:** `/api/v1/loans/{loanCode}/summary`

### 4.3 Get Repayment Schedule
Retrieve the list of EMIs for a specific loan.

- **Method:** `GET`
- **URL:** `/api/v1/loans/{loanCode}/schedule`

---

## 5. Payment Endpoints

### 5.1 Make EMI Payment
Pay an individual EMI installment.

- **Method:** `POST`
- **URL:** `/api/v1/payments/pay`
- **Request Body:**
```json
{
  "emiId": "uuid-of-emi-record",
  "paymentMode": "UPI"
}
```
- **Success Response (201 Created):**
```json
{
  "status": "CREATED",
  "message": "Payment processed successfully. Status: SUCCESS",
  "data": {
    "transactionId": "TXN-7890",
    "amount": 23450.00,
    "status": "SUCCESS"
  }
}
```

### 5.2 Payment History
- **By Loan:** `/api/v1/payments/loan/{loanCode}`
- **Self History:** `/api/v1/payments/history`
- **Master List (Admin/Officer):** `/api/v1/payments/all`

---

## 6. Employee/Admin Endpoints

### 6.1 Register Loan Officer (Admin only)
- **Method:** `POST`
- **URL:** `/api/v1/employee/admin/register/officer`

---

## Error Handling
The API returns a standard error object in case of failures:

```json
{
  "status": "BAD_REQUEST",
  "message": "Validation failed",
  "errors": [
    "Requested amount cannot be negative",
    "Invalid PAN format"
  ],
  "path": "/api/v1/loan/applications/apply"
}
```



{
"emiId": "a1b2c3d4-e5f6-7890-1234-56789abcdef0",
"amount": 5000.00,
"paymentMode": "CARD",
"methodDetails": {
"type": "CARD",
"cardholderName": "John Doe",
"cardNumber": "1234567890123456",
"expiryDate": "12/28",
"cvv": "123"
}
}


{
"emiId": "a1b2c3d4-e5f6-7890-1234-56789abcdef0",
"amount": 2500.00,
"paymentMode": "UPI",
"methodDetails": {
"type": "UPI",
"upiId": "borrower@okhdfc"
}
}


{
"emiId": "a1b2c3d4-e5f6-7890-1234-56789abcdef0",
"amount": 10000.00,
"paymentMode": "NET_BANKING",
"methodDetails": {
"type": "NET_BANKING",
"bankCode": "SBI_RETAIL_001"
}
}
