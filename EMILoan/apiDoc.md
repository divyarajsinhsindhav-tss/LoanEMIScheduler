# EMILoan API Documentation

## Overview
This documentation provides the details of all API endpoints for the **EMILoan** application. You can use this as a reference to set up your **Postman** collection.

**Base URL**: `http://localhost:8080` (Default)

---

## 1. Authentication Endpoints (Public)

### Login
Authenticates a user and returns a JWT token for further requests.

- **URL**: `/api/v1/auth/login`
- **Method**: `POST`
- **Request Body**:
  ```json
  {
      "email": "user@example.com",
      "password": "SecurePassword123"
  }
  ```
- **Success Response**: `200 OK`
  ```json
  {
      "status": "OK",
      "message": "Login successful",
      "data": {
          "accessToken": "eyJhbG...",
          "tokenType": "Bearer",
          "user": {
              "userId": "...",
              "userCode": "USR000001",
              "firstName": "John",
              "lastName": "Doe",
              "email": "john.doe@example.com",
              "phone": "9876543210",
              "role": { "roleName": "BORROWER" },
              "pan": "ABC*****4F"
          }
      }
  }
  ```

### Borrower Registration
Registers a new borrower in the system.

- **URL**: `/api/v1/auth/register/borrower`
- **Method**: `POST`
- **Request Body**:
  ```json
  {
      "firstName": "John",
      "lastName": "Doe",
      "email": "john.doe@example.com",
      "password": "SecurePassword123",
      "phone": "9876543210",
      "pan": "ABCDE1234F",
      "monthlyIncome": 50000.0
  }
  ```
- **Success Response**: `201 CREATED`

---

## 2. Borrower Endpoints (Requires **BORROWER** Role)

### Get Borrower Profile
Retrieves the logged-in borrower's profile with masked sensitive data.

- **URL**: `/api/v1/borrower/profile`
- **Method**: `GET`
- **Headers**: `Authorization: Bearer <JWT_TOKEN>`
- **Success Response**: `200 OK`
  ```json
  {
      "status": "OK",
      "message": "Profile retrieved successfully",
      "data": {
          "borrowerId": "...",
          "borrowerCode": "BRW000001",
          "monthlyIncome": 50000.0,
          "user": {
              "firstName": "John",
              "lastName": "Doe",
              "email": "john.doe@example.com",
              "pan": "ABC*****4F"
          }
      }
  }
  ```

---

## 3. Employee & Loan Officer Endpoints (Requires **LOAN_OFFICER** Role)

### Get Officer Profile
Retrieves the logged-in loan officer's profile.

- **URL**: `/api/v1/employee/profile`
- **Method**: `GET`
- **Headers**: `Authorization: Bearer <JWT_TOKEN>`
- **Success Response**: `200 OK`
  ```json
  {
      "status": "OK",
      "message": "Profile retrieved successfully",
      "data": {
          "employeeId": "...",
          "employeeCode": "EMP000001",
          "joiningDate": "2024-03-28",
          "salary": 65000.0,
          "isActive": true,
          "user": {
              "firstName": "Officer",
              "lastName": "One",
              "email": "officer1@loan.com"
          }
      }
  }
  ```

---

## 4. Admin Management (Requires **ADMIN** Role)

### Register Loan Officer
Allows an admin to create a new loan officer profile.

- **URL**: `/api/v1/employee/admin/register/officer`
- **Method**: `POST`
- **Headers**: `Authorization: Bearer <JWT_TOKEN>`
- **Request Body**:
  ```json
  {
      "firstName": "Officer",
      "lastName": "Two",
      "email": "officer2@loan.com",
      "password": "Password123",
      "phone": "9000800070",
      "pan": "OFFIC5678G",
      "salary": 68000.0,
      "joiningDate": "2024-03-29"
  }
  ```
- **Success Response**: `201 CREATED`

---

## Postman Tips
1. Use **Inherit auth from parent** for subsequent requests after logging in.
2. Store the `accessToken` in a Postman variable:
   ```javascript
   let response = pm.response.json();
   pm.environment.set("token", response.data.accessToken);
   ```

## Default Credentials
- **Admin**: `admin@example.com` / `AdminPassword123`
