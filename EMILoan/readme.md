

# 🏦 EMI Loan Management System (ELMS)

A robust, enterprise-grade RESTful API built with **Spring Boot 4.0** and **Java 21** for managing the complete lifecycle of a loan—from borrower registration and risk assessment to automated repayment scheduling and foreclosure.

## 🚀 Core Features

* **Borrower Registration & Security**: Secure onboarding with PII (PAN) hashing and masked data responses.
* **Intelligent Loan Intake**:
    * **Fail-Fast Thresholds**: Immediate rejection of loans exceeding amount (₹3M) or tenure (72 months).
    * **DTI Engine**: Real-time Debt-to-Income ratio calculation with automated capping.
    * **Strategy Engine**: Rule-based automated approval/rejection suggestions (Strategy Pattern).
* **Repayment Engine**:
    * Dynamic EMI schedule generation.
    * **Foreclosure Logic**: Intelligent settlement that waives future interest and balances the ledger.
* **Audit & Notifications**: Comprehensive system auditing and async email notifications (SMTP).
* **Role-Based Access Control (RBAC)**: Distinct permissions for `BORROWER`, `LOAN_OFFICER`, and `ADMIN` via JWT.

---

## 🛠️ Tech Stack

| Technology | Usage |
| :--- | :--- |
| **Java 21** | Core Language (Amazon Corretto) |
| **Spring Boot 4.0** | Framework |
| **Spring Security + JWT** | Authentication & Authorization |
| **Spring Data JPA** | Persistence Layer |
| **PostgreSQL** | Database |
| **Flyway** | Database Migrations |
| **MapStruct** | Data Mapping (DTO ↔ Entity) |
| **Lombok** | Boilerplate reduction |
| **JUnit 5 / Mockito** | Unit & Integration Testing |

---

## 🏗️ Getting Started

### Prerequisites
* JDK 21
* Maven 3.9+
* PostgreSQL 16+

### Installation
1. **Clone the repository**:
   ```bash
   git clone https://github.com/yourusername/EMILoan.git
   cd EMILoan
   ```

2. **Database Setup**:
   Create a database named `emiloan` in PostgreSQL and update `src/main/resources/application.yml`:
   ```yaml
   spring:
     datasource:
       url: jdbc:postgresql://localhost:5443/emiloan
       username: your_user
       password: your_password
   ```

3. **Build & Run**:
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```
---


## 👨‍💼 Loan Officer API Documentation

### **Overview**
The Loan Officer role is responsible for the manual review of loan applications, adjusting interest rates, and final approval/rejection. All requests require a `Bearer` token in the `Authorization` header.

---

### **1. Authentication & Profile**

#### **Login**
Authenticate and receive an access token.
* **Endpoint:** `POST /api/v1/auth/login`
* **Body:**
    ```json
    {
        "email": "officer@vvpedulink.ac.in",
        "password": "Password123"
    }
    ```

#### **Get My Info**
Retrieve the current officer's profile details.
* **Endpoint:** `GET /api/v1/auth/me`

#### **Get Borrower Profile**
View a specific borrower's details (Financials, PII, etc.) using their user code.
* **Endpoint:** `GET /api/v1/borrower/profile/{userCode}`

#### **Dashboard Statistics**
Get high-level stats (Total Pending, Total Active Loans, etc.).
* **Endpoint:** `GET /api/v1/employee/dashboard`

---

### **2. Loan Application Management**

#### **List All Applications**
Retrieve a list of all loan applications in the system.
* **Endpoint:** `GET /api/v1/loan/applications`

#### **Get Application Details**
Retrieve full details of a specific application, including the DTI ratio and the strategy suggested by the engine.
* **Endpoint:** `GET /api/v1/loan/applications/{appCode}/details`

#### **Submit Process Decision**
Approve or Reject a pending application. This step generates the actual loan record if approved.
* **Endpoint:** `PUT /api/v1/loans/applications/{appCode}/decision`
* **Body:**
    ```json
    {
        "status": "APPROVED",
        "interestRate": 10.5,
        "remarks": "Credit score and income verified.",
        "officerStrategy": "FLAT_RATE"
    }
    ```

---

### **3. Loan Oversight**

#### **List All Active Loans**
View all loans that are currently in repayment status.
* **Endpoint:** `GET /api/v1/loans/all`

#### **View Loan Details & Schedule**
Fetch specific loan terms or the month-by-month repayment schedule.
* **Endpoint:** `GET /api/v1/loans/{loanCode}`
* **Endpoint:** `GET /api/v1/loans/{loanCode}/schedule`

#### **Update Loan Status**
Manually override a loan status (e.g., marking as `CLOSED` or `DEFAULTED`).
* **Endpoint:** `PATCH /api/v1/loans/{loanCode}/status`
* **Body:**
    ```json
    {
        "loanStatus": "CLOSED"
    }
    ```

---

### **4. Payments & Audit**

#### **Payment History**
Track all payments made toward a specific loan or view the global payment ledger.
* **Endpoint:** `GET /api/v1/payments/history/loan/{loanCode}`
* **Endpoint:** `GET /api/v1/payments/history/all`

#### **Audit History**
Retrieve the system audit logs for a specific entity (e.g., LOAN, APPLICATION).
* **Endpoint:** `GET /api/v1/audit/entity/LOAN/{id}`

#### **Strategy Overrides**
Review logs specifically where a Loan Officer's final decision differed from the AI Strategy Engine's suggestion.
* **Endpoint:** `GET /api/v1/audit/strategy-overrides`

---

### **Response Codes**
| Code | Meaning |
| :--- | :--- |
| `200 OK` | Request successful. |
| `201 Created` | Resource (Loan/Payment) generated. |
| `401 Unauthorized` | Invalid or expired token. |
| `403 Forbidden` | Access denied (Only Loan Officer/Admin allowed). |
| `422 Unprocessable Entity` | Business rule violation (e.g., Loan already closed). |

---

## 👤 Borrower API Documentation

### **Overview**
The Borrower role is designed for customers to manage their own financial journey. Borrowers can apply for loans, track their application status, view repayment schedules, and make payments (EMI or Foreclosure). All protected requests require a `Bearer` token.

---

### **1. Onboarding & Profile**

#### **Register as Borrower**
Create a new borrower account. Provide personal details and financial info.
* **Endpoint:** `POST /api/v1/auth/register/borrower`
* **Body:**
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

#### **Login**
Receive a JWT access token for session management.
* **Endpoint:** `POST /api/v1/auth/login`

#### **Get My Profile**
Retrieve your personal details, including masked PII and linked account info.
* **Endpoint:** `GET /api/v1/borrower/profile`

#### **Update Monthly Income**
Update your financial profile to improve future loan eligibility.
* **Endpoint:** `PATCH /api/v1/borrower/profile/income?newMonthlyIncome=60000`

---

### **2. Loan Intake Process**

#### **Apply for a Loan**
Submit a new loan request. The system will automatically check DTI and apply risk strategies.
* **Endpoint:** `POST /api/v1/loan/applications/apply`
* **Body:**
    ```json
    {
        "requestedAmount": 150000,
        "tenureMonths": 12,
        "existingEmi": 2000
    }
    ```

#### **My Applications**
View a list of all your previous and currently pending applications.
* **Endpoint:** `GET /api/v1/loan/applications`

#### **Withdraw Application**
Cancel a pending application before it is processed by an officer.
* **Endpoint:** `PATCH /api/v1/loan/applications/{appCode}/withdraw`

---

### **3. Active Loan Management**

#### **My Active Loans**
Get a list of all your loans currently in repayment.
* **Endpoint:** `GET /api/v1/loans/`

#### **Repayment Schedule**
View the full month-by-month breakdown of principal, interest, and due dates.
* **Endpoint:** `GET /api/v1/loans/{loanCode}/schedule`

#### **Upcoming Payment**
Get details for the next installment due (EMI amount and due date).
* **Endpoint:** `GET /api/v1/loans/{loanCode}/schedule/next`

#### **Foreclosure Quote**
Get the exact amount required to settle the entire loan today (Principal + Current Interest).
* **Endpoint:** `GET /api/v1/loans/{loanCode}/foreclosure-quote`

---

### **4. Payments**

#### **Pay Monthly EMI**
Make a regular monthly payment via Card, UPI, or NetBanking.
* **Endpoint:** `POST /api/v1/payments/pay`
* **Body (UPI Example):**
    ```json
    {
        "emiId": "uuid-here",
        "amount": 5000,
        "paymentMode": "UPI",
        "methodDetails": {
            "type": "UPI",
            "upiId": "john@okaxis"
        }
    }
    ```

#### **Foreclose Loan**
Pay off the entire loan balance in a single transaction.
* **Endpoint:** `POST /api/v1/payments/foreclose`
* **Body:**
    ```json
    {
        "loanCode": "LAN000001",
        "amount": 125000.50,
        "paymentMode": "CARD",
        "methodDetails": {
            "type": "CARD",
            "cardholderName": "John Doe",
            "cardNumber": "4111...",
            "expiryDate": "12/28",
            "cvv": "123"
        }
    }
    ```

#### **Payment History**
View all successful and failed payment transactions linked to your account.
* **Endpoint:** `GET /api/v1/payments/history/my`

---

### **Borrower Constraints**
* **Max Active Loans:** You can only have a combined total of 3 Active or Pending applications at any time.
* **Max Tenure:** Individual loans cannot exceed 72 months.
* **Max Amount:** Maximum request threshold is ₹3,000,000.
---

## 👑 System Administrator API Documentation

### **Overview**
The Admin role holds the highest level of authority. Administrators manage the internal workforce (Loan Officers), perform sensitive security actions like account recovery, and have unrestricted access to the global audit trail to ensure system integrity.

---

### **1. Workforce & Officer Management**

#### **Register Loan Officer**
Onboard a new employee into the system with specific salary and joining details.
* **Endpoint:** `POST /api/v1/employee/admin/register/officer`
* **Body:**
    ```json
    {
        "firstName": "Officer",
        "lastName": "One",
        "email": "officer.one@loan.com",
        "password": "Password123",
        "phone": "9000800070",
        "pan": "OFFIC5678B",
        "salary": 68000.0,
        "joiningDate": "2024-03-29"
    }
    ```

#### **List All Officers**
Retrieve a complete directory of all loan officers.
* **Endpoint:** `GET /api/v1/employee/admin/all`

#### **Update Officer Details**
Modify salary or toggle the active status of an officer's account.
* **Endpoint:** `PATCH /api/v1/employee/admin/{userCode}`
* **Body:**
    ```json
    {
        "salary": 75000,
        "isActive": true
    }
    ```

---

### **2. Security & Account Operations**

#### **Delete Account**
Perform a "Soft Delete" on a user account based on their email address.
* **Endpoint:** `DELETE /api/v1/auth/delete?email={email}`

#### **Recover Deleted Account**
Restore access for a previously deleted user.
* **Endpoint:** `POST /api/v1/auth/recover`
* **Body:**
    ```json
    {
        "email": "user@example.com",
        "password": "NewSecurePassword123"
    }
    ```

#### **Access Borrower Profile**
View full, unrestricted profile data for any borrower using their unique user code.
* **Endpoint:** `GET /api/v1/borrower/profile/{userCode}`

---

### **3. Global System Auditing**

#### **Global Audit Trail**
Fetch every single action performed across the entire system.
* **Endpoint:** `GET /api/v1/audit/all`

#### **Audit by Entity**
Filter logs for a specific entity (e.g., viewing all changes ever made to a specific LOAN).
* **Endpoint:** `GET /api/v1/audit/entity/{ENTITY_TYPE}/{id}`
    * *Supported Entity Types: LOAN, APPLICATION, USER, PAYMENT*

#### **Audit by Officer**
Monitor all actions taken by a specific Loan Officer to ensure compliance.
* **Endpoint:** `GET /api/v1/audit/officer/{officer_id}`

#### **Audit by Action Type**
Filter logs by the type of operation performed (e.g., see all `DELETE` or `REJECTED` actions).
* **Endpoint:** `GET /api/v1/audit/action/{ACTION_TYPE}`
    * *Supported Actions: CREATE, UPDATE, DELETE, APPROVED, REJECTED*

#### **Strategy Override Report**
View a specialized report of cases where a Loan Officer manually overrode the AI Strategy Engine's recommendation.
* **Endpoint:** `GET /api/v1/audit/strategy-overrides`

---

### **Admin Permissions Summary**
* **Workforce Control:** Only Admins can register/update Loan Officers.
* **Data Recovery:** Only Admins can restore deleted user accounts.
* **Full Transparency:** Admins have "Read-All" access to all borrowers, applications, and payments regardless of ownership.
---
## 🧪 Running Tests
The project maintains high coverage with Mockito unit tests for service layers and engine logic.
```bash
mvn test
```

---

## 🛡️ Security & Business Rules
* **PAN Security**: PAN card numbers are stored as one-way hashes (`SHA-256`) to prevent PII leakage while ensuring uniqueness per role.
* **Loan Limits**: Maximum of 3 concurrent "Active + Pending" loans per borrower.
* **DTI Cap**: Ratios are mathematically capped at `999.99` to prevent database overflows.

---

## 🤝 Contributing
1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

---

**Developed with ❤️ by Om Kariya**