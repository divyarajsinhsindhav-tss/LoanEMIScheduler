# Loan EMI Scheduler

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16+-blue)](https://www.postgresql.org/)

An **EMI Scheduler & Personal Loan Management System** designed to manage the complete loan lifecycle—from application intake and automated risk assessment to dynamic EMI scheduling and payment tracking.

---

## Project Overview

![Database Architecture](ERDiagram/Emi%20Schedular.png)

This system automates the traditionally manual process of loan evaluation using a **Strategy Suggestion Engine**. It calculates risk based on Debt-to-Income (DTI) ratios and requested tenures, suggesting the most appropriate repayment strategy (Flat Rate, Reducing Balance, or Step-Up EMI).

### Key Workflows:
1. **Borrower** submits an application → **System** calculates DTI automatically.
2. **System** suggests a strategy → **Loan Officer** reviews and makes the final decision.
3. **Officer Approves** → System generates a precise **Amortization Schedule**.
4. **System Monitors** → Automatically flags overdue payments and sends notifications.

---

## Technology Stack

| Component | Technology |
| :--- | :--- |
| **Backend** | Java 21+ / Spring Boot 3.x |
| **Database** | PostgreSQL |
| **Security** | Spring Security + Stateless JWT |
| **Mapping** | MapStruct / Lombok |
| **Email** | Thymeleaf Templates + JavaMailSender |

---

## System Roles

### Borrower
*   **Onboarding**: Register and login securely.
*   **Application**: Submit loan requests with financial metadata.
*   **Tracking**: View active loans and personal amortization tables.
*   **Simulate Payments**: Mark EMIs as paid (simulation mode).
*   **Constraint**: Maximum of **3 active loans** allowed concurrently.

### Loan Officer
*   **Queue Management**: View all pending applications across the system.
*   **Credit Review**: Analyze system-suggested strategies and DTI metrics.
*   **Decision Authority**: Approve or Reject applications (with the ability to override suggested strategies).
*   **Reporting**: Access overdue summaries and loan distribution reports.

### The System (Automated Engine)
*   **Risk Evaluation**: Real-time DTI calculation and strategy suggestion.
*   **Scheduling**: Instant generation of complex EMI schedules upon approval.
*   **Monitoring**: Daily background jobs to flag overdue installments.
*   **Alerting**: Automated HTML email notifications for submission, approval, and reminders.

---

## Automated Strategy Engine

The system uses a rule-based engine to determine the best financial product for a borrower:

| DTI Ratio | Requested Tenure | Suggested Strategy |
| :--- | :--- | :--- |
| **< 20% (Low Risk)** | Any | `FLAT_RATE_LOAN` |
| **20% – 40% (Mid Risk)** | < 24 Months | `REDUCING_BALANCE_LOAN` |
| **20% – 40% (Mid Risk)** | ≥ 24 Months | `STEP_UP_EMI_LOAN` |
| **> 40% (High Risk)** | Any | **REJECT** |

*Note: The Loan Officer has the final authority to override these suggestions based on auxiliary factor checks.*

---

## Feature Set

### 1. Loan Lifecycle & Strategy Resolution
*   **DTI Intelligence**: Automated calculation at the moment of submission.
*   **Pattern Implementation**: Uses the **Strategy Design Pattern** for various interest calculations and the **Factory Pattern** for dynamic resolution.

### 2. EMI Scheduling & Amortization
*   **Precision Tables**: Generates Installment IDs, Due Dates, Principal bits, Interest bits, and Running Balances.
*   **Simulation**: Interactive payment simulation for testing repayment flows.
*   **Overdue Engine**: CRON-based background jobs to update status and notify the user.

### 3. Smart Notifications
*   **HTML Templates**: Professional email branding using Thymeleaf.
*   **Event-Driven**: Alerts sent for Application Submission, Approval/Rejection, 3-day Payment Reminders, and Overdue Warnings.

---

## Non-Functional Requirements

*   **NFR-1 Security**: 100% stateless JWT authentication for all protected endpoints.
*   **NFR-2 Performance**: EMI schedule generation (up to 120 months) optimized to complete under **100ms**.
*   **NFR-3 Auditability**: Comprehensive audit trail logging every state transition with Officer ID and timestamps.
*   **NFR-4 Scalability**: Layered Architecture designed for easy migration to hexagonal/microservices.

---

## API Preview

| Method | Endpoint | Description | Role |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/loan/applications/apply` | Submit a new loan request | Borrower |
| `GET` | `/api/v1/loans/{id}/schedule` | View detailed EMI schedule | Borrower/Officer |
| `GET` | `/api/v1/loan/applications` | View pending applications | Officer |
| `PUT` | `/api/v1/loans/applications/{code}/decision` | Process Approve/Reject | Officer |
