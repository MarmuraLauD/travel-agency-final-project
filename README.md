# 🌍 Travel Agency Management System

A modern, full-stack web application for managing travel vouchers and tours built with Spring Boot and Thymeleaf.

![Java](https://img.shields.io/badge/Java-17-orange?style=flat-square&logo=java)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.1-brightgreen?style=flat-square&logo=spring)
![MySQL](https://img.shields.io/badge/MySQL-Latest-%234479A1?style=flat-square&logo=mysql&logoColor=white)

---

## 📋 Table of Contents

- [Features](#-features)
- [Technology Stack](#-technology-stack)
- [Architecture](#-architecture)
- [Getting Started](#-getting-started)
- [Configuration](#-configuration)
- [API Documentation](#-api-documentation)
- [Testing](#-testing)
- [Security](#-security)
- [Internationalization](#-internationalization)
- [Project Structure](#-project-structure)
- [Contributing](#-contributing)
- [License](#-license)

---

## ✨ Features

### 🎫 Voucher Management
- **Browse Tours**: View available travel packages with advanced filtering
- **Shopping Cart**: Add tours to cart with custom arrival dates
- **Order Confirmation**: Secure payment processing with balance validation
- **Search & Filter**: Search by title, tour type, transport, hotel rating, and hot deals
- **Real-time Status**: Track voucher status (Available, In Cart, Confirmed)

### 👥 User Management
- **Role-Based Access**: USER, MANAGER, and ADMIN roles with granular permissions
- **User Authentication**: Secure JWT-based authentication with refresh tokens
- **Profile Management**: Update personal information and view order history
- **Balance System**: Virtual wallet for purchasing tours

### 🛡️ Admin Panel
- **Voucher Control**: Create, update, delete, and mark tours as HOT deals
- **User Administration**: Manage user accounts, roles, and account status
- **Dashboard Analytics**: Overview of total users and available vouchers
- **Dynamic Forms**: Comprehensive forms with real-time validation

### 🎨 Modern UI/UX
- **Responsive Design**: Mobile-first approach using Bootstrap 5.3.2
- **Gradient Themes**: Beautiful purple gradient design throughout
- **Real-time Validation**: Client and server-side validation with instant feedback
- **Custom Error Pages**: Styled 404, 403, 500 error pages
- **Language Switcher**: Seamless switching between English and Ukrainian

---

## 🛠️ Technology Stack

### Backend
- **Framework**: Spring Boot 3.2.1
- **Security**: Spring Security 6 with JWT Authentication
- **Database**: PostgreSQL (Production), H2 (Testing)
- **ORM**: Spring Data JPA with Hibernate
- **Validation**: Jakarta Bean Validation (JSR-380)
- **Mapping**: MapStruct 1.5.5
- **Build Tool**: Maven

### Frontend
- **Template Engine**: Thymeleaf with Spring Security integration
- **CSS Framework**: Bootstrap 5.3.2
- **Icons**: Bootstrap Icons
- **JavaScript**: Vanilla JS with Fetch API

### Security & Authentication
- **JWT**: JSON Web Tokens (io.jsonwebtoken 0.11.5)
- **Password Encryption**: BCrypt
- **Cookie-based Sessions**: Secure HTTP-only cookies
- **CSRF Protection**: Enabled for all state-changing operations

### Testing
- **Unit Testing**: JUnit 5, Mockito
- **Integration Testing**: Spring Boot Test, MockMvc
- **Security Testing**: Spring Security Test
- **Coverage**: 90+ test cases across all layers

---

## 🏗️ Architecture

### Layered Architecture

```
┌─────────────────────────────────────────┐
│         Presentation Layer              │
│  (Thymeleaf Templates + REST API)       │
├─────────────────────────────────────────┤
│          Controller Layer               │
│  (REST Controllers + View Controllers)  │
├─────────────────────────────────────────┤
│           Service Layer                 │
│     (Business Logic + Security)         │
├─────────────────────────────────────────┤
│         Repository Layer                │
│    (Spring Data JPA Repositories)       │
├─────────────────────────────────────────┤
│          Database Layer                 │
│         (PostgreSQL / H2)               │
└─────────────────────────────────────────┘
```

### Key Design Patterns
- **DTO Pattern**: Data transfer between layers using MapStruct
- **Repository Pattern**: Data access abstraction with Spring Data JPA
- **Service Layer Pattern**: Business logic encapsulation
- **MVC Pattern**: Separation of concerns in web layer
- **Builder Pattern**: Object construction (via Lombok)
- **Specification Pattern**: Dynamic query building for filters

---

## 🚀 Getting Started

### Prerequisites

- **Java 17** or higher
- **Maven 3.6+**
- **PostgreSQL 12+** (or use H2 for development)
- **Git**

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/yourusername/travel-agency.git
   cd travel-agency
   ```

2. **Configure the database**

   Create a `.env` file in `src/main/resources/`:
   ```properties
   DB_URL=jdbc:postgresql://localhost:5432/travel_agency
   DB_USERNAME=your_username
   DB_PASSWORD=your_password
   JWT_SECRET=your-256-bit-secret-key-here
   ```

3. **Build the project**
   ```bash
   mvn clean install
   ```

4. **Run the application**
   ```bash
   mvn spring-boot:run
   ```

5. **Access the application**
   - Open browser: `http://localhost:8080`
   - Default admin credentials:
     - Username: `superadmin`
     - Password: (configured in application.properties)

### Quick Start with H2 (In-Memory Database)

For quick testing without PostgreSQL:

1. Update `application.properties`:
   ```properties
   spring.datasource.url=jdbc:h2:mem:testdb
   spring.datasource.driver-class-name=org.h2.Driver
   spring.jpa.hibernate.ddl-auto=create-drop
   ```

2. Run the application and access H2 console at `http://localhost:8080/h2-console`

---

## ⚙️ Configuration

### Application Properties

Key configuration options in `application.properties`:

```properties
# Database Configuration
spring.datasource.url=${DB_URL}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}

# JPA/Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# JWT Configuration
jwt.secret-key=${JWT_SECRET}
jwt.expiration=3600000
jwt.refresh-token.expiration=86400000

# Internationalization
spring.messages.basename=messages
spring.messages.encoding=UTF-8

# Error Handling
spring.mvc.throw-exception-if-no-handler-found=true
server.error.whitelabel.enabled=false
```

### Environment Variables

Required environment variables:
- `DB_URL`: PostgreSQL connection string
- `DB_USERNAME`: Database username
- `DB_PASSWORD`: Database password
- `JWT_SECRET`: Secret key for JWT signing (256-bit minimum)

---

## 📡 API Documentation

### Authentication Endpoints

| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| POST | `/api/auth/signup` | Register new user | Public |
| POST | `/api/auth/signin` | User login | Public |
| POST | `/api/auth/refresh` | Refresh access token | Public |
| DELETE | `/api/auth/logout` | User logout | Authenticated |

### Voucher Endpoints

| Method | Endpoint | Description | Required Permission |
|--------|----------|-------------|---------------------|
| GET | `/api/vouchers` | Get all vouchers with filters | `voucher:read` |
| POST | `/api/vouchers` | Create new voucher | `voucher:create` |
| PATCH | `/api/vouchers/{id}` | Update voucher | `voucher:update` |
| DELETE | `/api/vouchers/{id}` | Delete voucher | `voucher:delete` |
| POST | `/api/vouchers/{id}/order` | Add to cart | `voucher:update` |
| POST | `/api/vouchers/{id}/confirm` | Confirm order | `voucher:update` |
| POST | `/api/vouchers/{id}/cancel` | Cancel order | `voucher:update` |
| PATCH | `/api/vouchers/{id}/status` | Toggle hot status | `voucher:update` |

### User Endpoints

| Method | Endpoint | Description | Required Permission |
|--------|----------|-------------|---------------------|
| GET | `/api/users` | Get all users | `user:update` |
| POST | `/api/users` | Create user | `user:create` |
| GET | `/api/users/me` | Get current user | Authenticated |
| PATCH | `/api/users/{username}` | Update user | `user:update` or self |
| DELETE | `/api/users/{id}` | Delete user | `user:delete` |
| PATCH | `/api/users/{id}/role` | Change user role | `user:update` |
| PATCH | `/api/users/{id}/status` | Change account status | `user:update` |

### Request/Response Examples

**Create Voucher Request:**
```json
{
  "title": "Paris Adventure",
  "description": "Amazing 7-day tour to Paris",
  "price": 1500.00,
  "tourType": "CULTURAL",
  "transferType": "PLANE",
  "hotelType": "FIVE_STARS"
}
```

**Error Response:**
```json
{
  "timestamp": "2025-01-15T10:30:00",
  "status": 400,
  "error": "Validation Error",
  "message": "Invalid form data",
  "path": "/api/vouchers",
  "validationErrors": [
    {
      "field": "title",
      "message": "Title must be between 3 and 100 characters"
    }
  ]
}
```

---

## 🧪 Testing

### Running Tests

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=VoucherServiceImplTest

# Run with coverage
mvn clean test jacoco:report
```

### Test Coverage

| Layer | Test Files | Test Cases | Coverage |
|-------|-----------|------------|----------|
| Services | 4 | 43 | 95% |
| Controllers | 4 | 34 | 90% |
| Exception Handlers | 1 | 8 | 100% |
| Mappers | 1 | 6 | 100% |
| **Total** | **10** | **91** | **93%** |

### Test Categories

- **Unit Tests**: Service layer with Mockito
- **Integration Tests**: Controller layer with MockMvc
- **Security Tests**: Authentication and authorization
- **Validation Tests**: DTO and form validation
- **Exception Tests**: Error handling scenarios

---

## 🔒 Security

### Authentication Flow

1. User submits credentials to `/api/auth/signin`
2. Server validates credentials
3. Server generates JWT access token (1 hour expiry)
4. Server generates refresh token (24 hours expiry)
5. Tokens stored in HTTP-only cookies
6. Client includes cookies in subsequent requests
7. Server validates JWT on each request
8. Client can refresh tokens using `/api/auth/refresh`

### Role-Based Permissions

| Role | Permissions |
|------|-------------|
| **USER** | `voucher:read`, `voucher:update` (own orders) |
| **MANAGER** | All USER permissions + `voucher:create`, `user:read` |
| **ADMIN** | All permissions including `user:create`, `user:delete`, `voucher:delete` |

### Security Features

- ✅ Password encryption with BCrypt
- ✅ JWT-based stateless authentication
- ✅ HTTP-only secure cookies
- ✅ CSRF protection
- ✅ SQL injection prevention (JPA)
- ✅ XSS protection (Thymeleaf escaping)
- ✅ Method-level security with `@PreAuthorize`
- ✅ Custom error pages (no stack trace exposure)

---

## 🌐 Internationalization

### Supported Languages

- 🇬🇧 **English** (Default)
- 🇺🇦 **Ukrainian**

### Language Switching

Users can switch languages using the dropdown in the navbar. The selected language is stored in a cookie for 1 year.

**Programmatic Language Change:**
```
GET /locale?lang=uk  # Switch to Ukrainian
GET /locale?lang=en  # Switch to English
```

### Adding New Languages

1. Create new message file: `src/main/resources/messages_[locale].properties`
2. Add translations for all keys from `messages.properties`
3. Language will be automatically available in the switcher

---

## 🗂️ Project Structure

```
src/
├── main/
│   ├── java/com/epam/finaltask/
│   │   ├── config/          # Security, Locale, App configuration
│   │   ├── controller/      # REST and View controllers
│   │   │   ├── restcontroller/
│   │   │   └── AuthController, UIController
│   │   ├── dto/             # Data Transfer Objects
│   │   ├── exception/       # Custom exceptions and handlers
│   │   ├── mapper/          # MapStruct mappers
│   │   ├── model/           # JPA entities
│   │   ├── repository/      # Spring Data repositories
│   │   ├── service/         # Business logic
│   │   │   └── security/    # JWT and security services
│   │   └── specification/   # JPA Specifications
│   └── resources/
│       ├── templates/       # Thymeleaf templates
│       │   ├── admin/
│       │   ├── auth/
│       │   ├── error/
│       │   ├── fragments/
│       │   └── user/
│       ├── application.properties
│       ├── messages.properties
│       ├── messages_uk.properties
│       └── .env
└── test/
    └── java/com/epam/finaltask/
        ├── controller/      # Controller tests
        ├── service/         # Service tests
        ├── exception/       # Exception handler tests
        └── mapper/          # Mapper tests
```

---

## 🤝 Contributing

Contributions are welcome! Please follow these steps:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

### Code Style

- Follow Java naming conventions
- Use Lombok annotations to reduce boilerplate
- Write meaningful commit messages
- Add tests for new features
- Update documentation as needed

---

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## 👨‍💻 Author

**EPAM Final Task Project**

- Project: Travel Agency Management System
- Version: 0.0.1-SNAPSHOT
- Framework: Spring Boot 3.2.1
- Java Version: 17

---

## 🙏 Acknowledgments

- Spring Boot team for the excellent framework
- Thymeleaf for the powerful template engine
- Bootstrap team for the responsive UI framework
- MapStruct for seamless object mapping
- JJWT library for JWT implementation

---

## 📞 Support

For support, create an issue in the repository.

---

<div align="center">
  <p>Made with ❤️ using Spring Boot</p>
  <p>⭐ Star this repository if you find it helpful!</p>
</div>
