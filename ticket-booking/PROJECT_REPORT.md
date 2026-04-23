# PROJECT REPORT

## Project Title
**Design and Development of a Spring Boot Java-based Web Application for Ticket Booking of an Internal Department Event**

---

## 1. INTRODUCTION

### 1.1 Project Overview
This project is a full-featured **Ticket Booking System** built using **Spring Boot (Java)** for managing registrations and ticket bookings for internal department events such as Technical Fests, Seminars, Workshops, and Cultural Events within a college or organization.

### 1.2 Objectives
- Provide a centralized platform for event discovery and ticket booking
- Eliminate manual/paper-based registration processes
- Ensure secure, role-based access for Users, Organizers, and Admins
- Generate QR-code-based digital tickets for contactless check-in
- Send automated email notifications for bookings, cancellations, and reminders
- Provide real-time analytics and dashboard for administrators

---

## 2. TECHNOLOGY STACK

| Layer | Technology |
|---|---|
| Backend Framework | Spring Boot 3.2.4 |
| Language | Java 17 |
| Security | Spring Security + JWT (JJWT 0.12.5) |
| Database | MySQL 8.x |
| ORM | Spring Data JPA / Hibernate |
| Email | Spring Mail + Thymeleaf Templates |
| QR Code | ZXing (Google) |
| PDF | iText 8 |
| API Docs | SpringDoc OpenAPI (Swagger UI) |
| Build Tool | Maven |
| Caching | Spring Cache (Simple/Redis-ready) |
| Mapping | MapStruct |
| Boilerplate | Lombok |

---

## 3. SYSTEM ARCHITECTURE

```
Client (Browser / Mobile App)
        |
        | HTTPS REST API
        v
+---------------------------+
|   Spring Boot Application  |
|---------------------------|
|  Controllers (REST Layer)  |
|  Services (Business Logic) |
|  Repositories (Data Layer) |
|  Security (JWT + Filters)  |
+---------------------------+
        |
        v
   MySQL Database
```

### 3.1 Layered Architecture
- **Controller Layer** – Handles HTTP requests, input validation, response formatting
- **Service Layer** – Business logic, transaction management, orchestration
- **Repository Layer** – JPA repositories for database operations
- **Security Layer** – JWT authentication, role-based authorization, account locking
- **Utility Layer** – QR code generation, email sending, PDF generation

---

## 4. DATABASE DESIGN

### 4.1 Entity Relationship Diagram (ERD)

```
USERS ──────────── BOOKINGS ──────────── EVENTS
  |                    |                    |
  |                    |── TICKETS          |── TICKET_TYPES
  |                    |
  └── AUDIT_LOGS
```

### 4.2 Tables

#### users
| Column | Type | Constraints |
|---|---|---|
| id | VARCHAR(36) | PK, UUID |
| employee_id | VARCHAR(50) | UNIQUE, NOT NULL |
| full_name | VARCHAR(100) | NOT NULL |
| email | VARCHAR(150) | UNIQUE, NOT NULL |
| password | VARCHAR(255) | NOT NULL (BCrypt) |
| phone | VARCHAR(15) | |
| department | VARCHAR(100) | |
| designation | VARCHAR(50) | |
| role | ENUM | USER/ADMIN/ORGANIZER |
| enabled | BOOLEAN | Default: false |
| account_non_locked | BOOLEAN | Default: true |
| failed_login_attempts | INT | Default: 0 |
| lock_time | DATETIME | |
| email_verification_token | VARCHAR(255) | |
| password_reset_token | VARCHAR(255) | |
| last_login_at | DATETIME | |
| created_at | DATETIME | Auto |

#### events
| Column | Type | Constraints |
|---|---|---|
| id | VARCHAR(36) | PK, UUID |
| title | VARCHAR(200) | NOT NULL |
| description | TEXT | |
| venue | VARCHAR(200) | NOT NULL |
| event_date | DATETIME | NOT NULL |
| registration_deadline | DATETIME | |
| total_capacity | INT | NOT NULL |
| available_seats | INT | |
| ticket_price | DECIMAL(10,2) | Default: 0 |
| organizer | VARCHAR(100) | |
| department | VARCHAR(100) | |
| category | VARCHAR(100) | SEMINAR/TECHFEST/WORKSHOP/etc |
| status | ENUM | UPCOMING/ONGOING/COMPLETED/CANCELLED |
| is_free | BOOLEAN | |
| max_tickets_per_user | INT | |
| created_by | VARCHAR(36) | FK → users |

#### bookings
| Column | Type | Constraints |
|---|---|---|
| id | VARCHAR(36) | PK, UUID |
| booking_reference | VARCHAR(20) | UNIQUE, NOT NULL |
| user_id | VARCHAR(36) | FK → users |
| event_id | VARCHAR(36) | FK → events |
| ticket_type_id | VARCHAR(36) | FK → ticket_types |
| quantity | INT | NOT NULL |
| total_amount | DECIMAL(10,2) | |
| status | ENUM | PENDING/CONFIRMED/CANCELLED/WAITLISTED |
| payment_status | ENUM | PENDING/COMPLETED/REFUNDED/NOT_REQUIRED |
| qr_code_data | TEXT | |
| checked_in | BOOLEAN | Default: false |
| check_in_time | DATETIME | |
| booked_at | DATETIME | Auto |

#### tickets
| Column | Type | Constraints |
|---|---|---|
| id | VARCHAR(36) | PK, UUID |
| ticket_number | VARCHAR(30) | UNIQUE |
| booking_id | VARCHAR(36) | FK → bookings |
| seat_number | VARCHAR(100) | |
| status | ENUM | ACTIVE/USED/CANCELLED/EXPIRED |
| checked_in | BOOLEAN | |
| issued_at | DATETIME | Auto |

#### ticket_types
| Column | Type | Constraints |
|---|---|---|
| id | VARCHAR(36) | PK, UUID |
| name | VARCHAR(100) | GENERAL/VIP/STUDENT/FACULTY |
| price | DECIMAL(10,2) | |
| total_seats | INT | |
| available_seats | INT | |
| max_per_user | INT | |
| event_id | VARCHAR(36) | FK → events |

#### audit_logs
| Column | Type | Description |
|---|---|---|
| id | VARCHAR(36) | PK |
| user_id | VARCHAR(36) | Who performed action |
| action | VARCHAR(100) | Action name |
| entity_type | VARCHAR(100) | Entity affected |
| entity_id | VARCHAR(36) | Entity ID |
| details | TEXT | Description |
| ip_address | VARCHAR(50) | Client IP |
| created_at | DATETIME | Timestamp |

---

## 5. API ENDPOINTS

### 5.1 Authentication APIs (`/api/auth`)
| Method | Endpoint | Description | Auth |
|---|---|---|---|
| POST | /auth/register | Register new user | Public |
| POST | /auth/login | Login, get JWT | Public |
| GET | /auth/verify-email?token= | Verify email | Public |
| POST | /auth/forgot-password | Request reset link | Public |
| POST | /auth/reset-password | Reset password | Public |
| POST | /auth/refresh-token | Refresh JWT | Public |

### 5.2 Event APIs
| Method | Endpoint | Description | Auth |
|---|---|---|---|
| GET | /events/public | List upcoming events | Public |
| GET | /events/public/{id} | Get event details | Public |
| GET | /events/public/search?keyword= | Search events | Public |
| GET | /events | All events with filter | USER+ |
| GET | /events/{id} | Event details | USER+ |
| POST | /organizer/events | Create event | ORGANIZER/ADMIN |
| PUT | /organizer/events/{id} | Update event | ORGANIZER/ADMIN |
| PATCH | /organizer/events/{id}/cancel | Cancel event | ORGANIZER/ADMIN |
| DELETE | /admin/events/{id} | Delete event | ADMIN |

### 5.3 Booking APIs (`/api/bookings`)
| Method | Endpoint | Description | Auth |
|---|---|---|---|
| POST | /bookings | Create booking | USER+ |
| GET | /bookings/my | My bookings | USER+ |
| GET | /bookings/{id} | Booking details | USER+ |
| GET | /bookings/reference/{ref} | By reference | USER+ |
| PATCH | /bookings/{id}/cancel | Cancel booking | USER+ |
| GET | /bookings/event/{eventId} | Event bookings | ORGANIZER/ADMIN |
| POST | /bookings/check-in/{ref} | QR check-in | ORGANIZER/ADMIN |

### 5.4 User APIs (`/api/users`)
| Method | Endpoint | Description | Auth |
|---|---|---|---|
| GET | /users/me | Get profile | USER+ |
| PUT | /users/me | Update profile | USER+ |
| PATCH | /users/me/change-password | Change password | USER+ |

### 5.5 Admin APIs (`/api/admin`)
| Method | Endpoint | Description | Auth |
|---|---|---|---|
| GET | /admin/dashboard | Dashboard stats | ADMIN |
| GET | /admin/users | All users | ADMIN |
| PATCH | /admin/users/{id}/role | Update role | ADMIN |
| PATCH | /admin/users/{id}/toggle-status | Enable/disable | ADMIN |

---

## 6. SECURITY FEATURES

### 6.1 Authentication & Authorization
- **JWT (JSON Web Token)** – Stateless authentication with access + refresh tokens
- **BCrypt Password Hashing** – Strength 12 (industry standard)
- **Role-Based Access Control (RBAC)** – USER, ORGANIZER, ADMIN roles
- **Method-Level Security** – `@PreAuthorize` annotations on sensitive endpoints
- **Email Verification** – Account activation via email token (24-hour expiry)

### 6.2 Account Protection
- **Brute Force Protection** – Account locked after 5 failed login attempts
- **Auto-Unlock** – Account unlocks after 30 minutes
- **Password Reset** – Secure token-based reset with 1-hour expiry
- **Refresh Token Rotation** – New refresh token on each use

### 6.3 Input Validation & Sanitization
- **Bean Validation** – `@Valid` on all request DTOs
- **Custom Regex Patterns** – Employee ID, phone, password strength
- **Password Policy** – Uppercase + lowercase + digit + special character required
- **SQL Injection Prevention** – JPA parameterized queries only

### 6.4 HTTP Security Headers
- `X-Frame-Options: DENY` – Clickjacking prevention
- `Content-Security-Policy` – XSS prevention
- `Referrer-Policy: STRICT_ORIGIN_WHEN_CROSS_ORIGIN`
- `Permissions-Policy` – Camera/microphone/geolocation disabled
- **CORS** – Configured for specific allowed origins only

### 6.5 Business Logic Security
- **Atomic Seat Decrement** – Prevents double-booking via DB-level locking
- **Max Tickets Per User** – Enforced per event
- **Cancellation Window** – Enforced 24-hour cutoff
- **Audit Logging** – All critical actions logged with IP and user agent
- **Error Message Sanitization** – No stack traces exposed to clients
- **Email Enumeration Prevention** – Forgot password always returns success

---

## 7. KEY FEATURES

### 7.1 User Features
- Self-registration with employee ID and email verification
- Browse and search events by keyword, category, department
- Book tickets with quantity selection
- View booking history with QR codes
- Cancel bookings (within allowed window)
- Change password and update profile

### 7.2 Organizer Features
- Create and manage events with multiple ticket types
- View all bookings for their events
- QR code-based check-in at venue
- Real-time seat availability tracking

### 7.3 Admin Features
- Full dashboard with analytics:
  - Total events, bookings, users, revenue
  - Bookings by category and department
  - Top events by attendance
  - Check-in statistics
- User management (role assignment, enable/disable)
- Event management (create, update, cancel, delete)
- Audit log access

### 7.4 System Features
- **QR Code Generation** – Unique QR per booking with embedded booking data
- **Email Notifications** – HTML emails for registration, booking, cancellation, password reset
- **Pagination & Sorting** – All list APIs support pagination
- **Caching** – Event data cached for performance
- **Async Processing** – Email sending is non-blocking
- **Swagger UI** – Interactive API documentation at `/api/swagger-ui.html`
- **Health Monitoring** – Spring Actuator endpoints

---

## 8. PROJECT STRUCTURE

```
ticket-booking/
├── pom.xml
└── src/
    └── main/
        ├── java/com/ticketbooking/
        │   ├── TicketBookingApplication.java
        │   ├── config/
        │   │   ├── SecurityConfig.java       ← JWT, CORS, Security Headers
        │   │   └── SwaggerConfig.java        ← OpenAPI documentation
        │   ├── controller/
        │   │   ├── AuthController.java       ← Register, Login, Token
        │   │   ├── EventController.java      ← Event CRUD
        │   │   ├── BookingController.java    ← Booking + Check-in
        │   │   ├── UserController.java       ← Profile management
        │   │   └── AdminController.java      ← Admin dashboard
        │   ├── service/
        │   │   ├── AuthService.java          ← Auth business logic
        │   │   ├── EventService.java         ← Event business logic
        │   │   ├── BookingService.java       ← Booking + ticket generation
        │   │   ├── EmailService.java         ← Async email sending
        │   │   ├── QrCodeService.java        ← ZXing QR generation
        │   │   ├── AuditService.java         ← Audit logging
        │   │   ├── AdminService.java         ← Admin operations
        │   │   └── UserDetailsServiceImpl.java
        │   ├── repository/
        │   │   ├── UserRepository.java
        │   │   ├── EventRepository.java
        │   │   ├── BookingRepository.java
        │   │   ├── TicketRepository.java
        │   │   ├── TicketTypeRepository.java
        │   │   └── AuditLogRepository.java
        │   ├── model/entity/
        │   │   ├── User.java                 ← Implements UserDetails
        │   │   ├── Event.java
        │   │   ├── Booking.java
        │   │   ├── Ticket.java
        │   │   ├── TicketType.java
        │   │   └── AuditLog.java
        │   ├── dto/
        │   │   ├── request/                  ← Validated input DTOs
        │   │   └── response/                 ← Output DTOs
        │   ├── security/
        │   │   ├── JwtService.java           ← Token generation/validation
        │   │   └── JwtAuthenticationFilter.java
        │   └── exception/
        │       ├── GlobalExceptionHandler.java
        │       ├── ResourceNotFoundException.java
        │       ├── BadRequestException.java
        │       └── UnauthorizedException.java
        └── resources/
            ├── application.yml
            ├── data.sql                      ← Seed data
            └── templates/email/
                ├── verification.html
                ├── booking-confirmation.html
                ├── booking-cancellation.html
                └── password-reset.html
```

---

## 9. SETUP & RUNNING

### 9.1 Prerequisites
- Java 17+
- Maven 3.8+
- MySQL 8.x

### 9.2 Database Setup
```sql
CREATE DATABASE ticket_booking_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 9.3 Configuration
Set environment variables or update `application.yml`:
```
DB_USERNAME=root
DB_PASSWORD=yourpassword
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password
JWT_SECRET=your-256-bit-hex-secret
```

### 9.4 Run
```bash
cd ticket-booking
mvn clean install
mvn spring-boot:run
```

### 9.5 Access
- API Base: `http://localhost:8080/api`
- Swagger UI: `http://localhost:8080/api/swagger-ui.html`
- Default Admin: `admin@college.edu` / `Admin@1234`

---

## 10. TESTING

### Sample API Calls

**Register:**
```json
POST /api/auth/register
{
  "employeeId": "EMP001",
  "fullName": "John Doe",
  "email": "john@college.edu",
  "password": "Pass@1234",
  "confirmPassword": "Pass@1234",
  "department": "Computer Science",
  "phone": "9876543210"
}
```

**Login:**
```json
POST /api/auth/login
{
  "email": "john@college.edu",
  "password": "Pass@1234"
}
```

**Book Ticket:**
```json
POST /api/bookings
Authorization: Bearer <token>
{
  "eventId": "event-uuid-here",
  "quantity": 2
}
```

---

## 11. FUTURE ENHANCEMENTS

1. **Payment Gateway Integration** – Razorpay/PayU for paid events
2. **Waitlist Management** – Auto-assign when cancellations occur
3. **Mobile App** – React Native / Flutter frontend
4. **Redis Caching** – Replace simple cache for distributed deployment
5. **WebSocket Notifications** – Real-time seat availability updates
6. **PDF Ticket Download** – Downloadable ticket with QR code
7. **OAuth2 / SSO** – Google/Microsoft login for college accounts
8. **Event Feedback System** – Post-event ratings and reviews
9. **Bulk Import** – CSV upload for batch user registration
10. **Multi-language Support** – i18n for regional languages

---

## 12. CONCLUSION

This Spring Boot-based Ticket Booking System provides a robust, secure, and scalable solution for managing internal department events. The system implements industry-standard security practices including JWT authentication, BCrypt password hashing, brute-force protection, and comprehensive audit logging. The RESTful API design ensures easy integration with any frontend framework, and the modular architecture allows for straightforward feature additions and maintenance.
