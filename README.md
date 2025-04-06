#  Invoice Processing API

A Java Spring Boot REST API for parsing, validating, and storing invoices and their associated transactions using CSV files.

---

## 🛠️ Prerequisites

To run this project locally, you'll need:

- Java 17
- Gradle
- **Docker**

> **Note:**  
> This project uses Docker to spin up a PostgreSQL database automatically.  
> Please ensure you have Docker installed and running before starting the application.

##  Tech Stack

- **Java 17**
- **Spring Boot 3.4**
- **Gradle**
- **PostgreSQL** (running in **Docker**)
- **JUnit + Mockito** for testing
- **SLF4J** for logging
- **@ControllerAdvice** for global exception handling

---

##  Getting Started



## Steps to get the code running

* Checkout the code from Github
* Go to the terminal under `/invoice`
* Execute `./gradlew composeUp` to start Postgres DB
* Execute `./gradlew bootRun` to start the application
### Application Base URL

Base URL is exposed at 8080: **[Base URL](http://localhost:8080)**

### 📌 Endpoints Exposed

| Method | Endpoint                                 | Description                                                                |
|--------|------------------------------------------|----------------------------------------------------------------------------|
| POST   | `/invoice`                               | Uploads invoice and transaction CSV files, processes and stores them      |
| GET    | `/invoice`                               | Retrieves all stored invoices                                              |
| GET    | `/invoice/{invoiceId}`                   | Retrieves a specific invoice by its ID                                     |
| GET    | `/invoice/{invoiceId}/status`            | Returns status and reason for a given invoice ID                 |

---
# Invoice API Usage

## Endpoints

Below are the available endpoints for interacting with the Invoice API:
> 💡 **Note:**  
> Please begin with the `POST /invoice` endpoint.  
> This will seed the system with the required data so that other endpoints like `GET /invoice` return meaningful results.


### 1. Upload Invoices and Transactions

**Endpoint:**
`/invoice`
**Description:**
Uploads invoice and transaction CSV files to the server for processing.
**Sample Request:**

```
curl -X POST http://localhost:8080/invoice -F "invoices=@src/main/resources/data/invoices.csv" -F "transactions=@src/main/resources/data/transactions.csv"
```

### 2. Get All invoices

**Endpoint:**
`/invoice`
**Description:**
Fetches a list of all processed invoices.
**Sample Request:**

```
curl -X GET http://localhost:8080/invoice
```

### 3. Get Invoice by ID

**Endpoint:**
`/invoice/{invoiceId}`
**Description:**
Fetches details of a specific invoice by its ID.
**Sample Request:**
```
curl -X GET http://localhost:8080/invoice/31620
```

### 4. Get status of an invoice by ID

**Endpoint:**
`/invoice/{invoiceId}/status`
**Description:**
Fetches the  status of a specific invoice by its ID.
**Sample Request:**
```
curl -X GET http://localhost:8080/invoice/31620/status
```

###  Assumptions 

- Invoices and transactions are uploaded together via a **multipart CSV upload**.
- CSV headers must match expected column names; otherwise, parsing errors are thrown.
- PostgreSQL is assumed to run as a **Docker container** using `docker-compose`.
- If invoice id is missing in transaction record of transactions file, that record is skipped but it continues to 
  process other records. 
- If invoice id or transaction id is missing, that record is skipped but it continues to process other records.

---

###  Enhancements & Future Improvements

-  Add pagination to list invoices using optional query parameters and Spring Data's Pageable.
-  Add **authentication/authorization** to secure upload and read endpoints using OAuth2
-  Extend test coverage with more integration and negative test cases
-  Add monitoring using Spring Actuator endpoints
-  Add **OpenAPI** documentation for API readability and can consider having OpenApi generator .
