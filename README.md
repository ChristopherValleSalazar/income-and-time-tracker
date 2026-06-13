# Income and Time Tracker

A full-stack application for tracking **programming hours** and **Amazon delivery transactions/payments**. The project consists of a Java Spring Boot backend with a PostgreSQL database and a lightweight HTML/CSS/JavaScript frontend.

---

## Features

### Programming Hours Tracker

* Log programming sessions
* View total accumulated programming hours
* Save and retrieve programming history
* Automatically calculate total hours worked

### Amazon Transaction Tracker

* Record Amazon package deliveries
* Track worker assignments
* Store payment information
* Generate weekly summaries
* View earnings grouped by employee
* Delete and manage transaction records
* Pagination support for large datasets

---

# Project Structure

```text
income-and-time-tracker/
│
├── frontendTracker/
│   ├── index.html          # Programming Hours UI
│   ├── amz.html            # Amazon Tracker UI
│   ├── script.js
│   ├── amz.js
│   ├── api.js
│   ├── theme.js
│   └── style.css
│
└── hoursBackend/
    ├── Controllers
    ├── Services
    ├── Repositories
    ├── Models
    ├── DTOs
    └── HoursBackendApplication.java
```

---

# Technologies Used

## Frontend

* HTML5
* CSS3
* Vanilla JavaScript (ES Modules)

## Backend

* Java 21
* Spring Boot
* Spring Data JPA
* PostgreSQL
* Maven

---

# Architecture

The backend follows a layered architecture.

```text
Frontend
      │
      ▼
REST Controllers
      │
      ▼
Services
      │
      ▼
Repositories
      │
      ▼
PostgreSQL
```

### Controllers

Responsible for handling HTTP requests and returning responses.

* ProgrammingHoursController
* AmazonTransactionController

### Services

Contain the application's business logic.

Examples include:

* Saving programming sessions
* Managing Amazon transactions
* Computing weekly summaries
* Aggregating totals by employee

### Repositories

Spring Data JPA repositories used for persistence.

* ProgrammingHoursRepo
* AmazonTransactionRepo

### Models

Core entities used throughout the application.

* ProgrammingHours
* AmazonTransaction
* AmazonNames (Enum)

### DTOs

Used for transferring weekly report data.

* WeeklyReportGeneral
* WeeklyReportPerPerson

---

# REST API

## Programming Hours

| Method | Endpoint           | Description                      |
| ------ | ------------------ | -------------------------------- |
| GET    | `/api/table`       | Retrieve programming sessions    |
| POST   | `/api/table`       | Save programming session         |
| GET    | `/api/table/total` | Retrieve total programming hours |

---

## Amazon Transactions

| Method | Endpoint                     | Description               |
| ------ | ---------------------------- | ------------------------- |
| GET    | `/api/amzTransaction`        | Retrieve transactions     |
| POST   | `/api/amzTransaction`        | Save transaction          |
| DELETE | `/api/amzTransaction/{id}`   | Delete transaction        |
| GET    | `/api/amzTransaction/names`  | Retrieve worker names     |
| GET    | `/api/amzTransaction/weekly` | Weekly totals             |
| GET    | `/api/amzTransaction/person` | Weekly totals by employee |

---

# Frontend

There are two primary pages.

## Programming Hours

Provides a simple interface to:

* Add programming sessions
* Save session history
* Calculate total hours
* Display previous entries

---

## Amazon Tracker

Allows users to:

* Add package transactions
* Record payment information
* Assign workers
* View weekly summaries
* Delete existing records
* Navigate large datasets using pagination

---

# Configuration

The backend loads database configuration using environment variables.

Example:

```properties
DB_URL=
DB_USERNAME=
DB_PASSWORD=
```

JPA is configured with automatic schema updates.

```properties
spring.jpa.hibernate.ddl-auto=update
```

---

# Running the Project

## Backend

```bash
cd hoursBackend

./mvnw spring-boot:run
```

or

```bash
mvn spring-boot:run
```

The backend runs on:

```
http://localhost:8080
```

---

## Frontend

Open the frontend using a local web server.

Example:

```bash
python -m http.server 5500
```

or use the VS Code Live Server extension.

---

# CORS

The backend currently allows requests from local development hosts running on port **5500**.

If deploying the frontend elsewhere, update the CORS configuration accordingly.

---

# API Configuration

The frontend currently uses a hardcoded API URL:

```javascript
http://raspberrypi:8080
```

For deployment, update the API endpoint to match your server.

---

# Styling

The frontend uses a single centralized stylesheet featuring:

* Dark/Light theme support
* Responsive tables
* Pagination styling
* Shared UI components
* CSS variables for theming

---

# Testing

Current automated testing is minimal and includes only a basic Spring Boot context load test.

Future improvements could include:

* Unit tests
* Integration tests
* Frontend testing
* API endpoint testing

---

# Future Improvements

* User authentication
* Responsive mobile layout
* Dashboard analytics
* Data export (CSV/PDF)
* Search and filtering
* Charts and visualizations
* Docker support
* Automated testing
* CI/CD pipeline
* Deployment configuration

---

# Author

**Christopher Valle**

GitHub: https://github.com/ChristopherValleSalazar
