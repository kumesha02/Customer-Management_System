# 🎓 Customer Management System

A full-stack customer management application built with **Spring Boot** and **React**, designed as a comprehensive learning resource for enterprise Java development.

## ✨ Features

### Core Functionality
- ✅ **CRUD Operations**: Create, Read, Update, Delete customers
- ✅ **Complex Relationships**: Multiple mobile numbers, addresses, and family members per customer
- ✅ **Master Data Management**: Countries and cities with cascading dropdowns
- ✅ **Bulk Operations**: Upload Excel files with up to 1,000,000 customer records
- ✅ **Advanced Search**: Find customers by name, NIC, or other criteria
- ✅ **Pagination**: Efficient handling of large datasets

### Technical Highlights
- 🚀 **Performance Optimized**: Chunk-based batch processing for bulk uploads
- 💾 **Memory Efficient**: Streaming Excel processing prevents OutOfMemoryError
- 🔒 **Data Integrity**: Database-level constraints and validation
- 🧪 **Well Tested**: Comprehensive JUnit tests with Mockito
- 📚 **Educational**: Extensively documented code with teaching comments

---

## 🛠️ Technology Stack

### Backend
| Technology | Version | Purpose |
|------------|---------|---------|
| **Java** | 8 | Programming language |
| **Spring Boot** | 2.7.18 | Application framework |
| **Spring Data JPA** | 2.7.18 | Database ORM |
| **MariaDB** | 10.x+ | Relational database |
| **Apache POI** | 5.2.3 | Excel file processing |
| **Lombok** | Latest | Reduce boilerplate code |
| **ModelMapper** | 3.1.1 | DTO ↔ Entity mapping |
| **JUnit 5** | 5.9.x | Unit testing |
| **Mockito** | 4.x | Mocking framework |
| **Maven** | 3.6+ | Build tool & dependency management |

### Frontend
| Technology | Version | Purpose |
|------------|---------|---------|
| **React** | 18.x | UI framework |
| **Axios** | Latest | HTTP client |
| **React Hook Form** | Latest | Form handling |
| **React Datepicker** | Latest | Date selector |
| **Bootstrap** | 5.x | UI styling |

---

## 📋 Prerequisites

Before you begin, ensure you have the following installed:

1. **Java Development Kit (JDK) 8**
   - Download: [Oracle JDK 8](https://www.oracle.com/java/technologies/javase-jdk8-downloads.html) or[OpenJDK 8](https://adoptopenjdk.net/)
   - Verify: `java -version`

2. **Maven 3.6+**
   - Download: [Apache Maven](https://maven.apache.org/download.cgi)
   - Verify: `mvn -version`

3. **MariaDB 10.x+**
   - Download: [MariaDB Server](https://mariadb.org/download/)
   - Verify: `mysql --version`

4. **Node.js 14+ and npm**
   - Download: [Node.js](https://nodejs.org/)
   - Verify: `node --version` and `npm --version`

---

## 🚀 Getting Started

### 1️⃣ Database Setup

#### Create Database
```sql
-- Connect to MariaDB
mysql -u root -p

-- Create database
CREATE DATABASE customer_management CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Create user (optional, for security)
CREATE USER 'cms_user'@'localhost' IDENTIFIED BY 'cms_password';
GRANT ALL PRIVILEGES ON customer_management.* TO 'cms_user'@'localhost';
FLUSH PRIVILEGES;

EXIT;
```

#### Configure Connection
Edit `backend/src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mariadb://localhost:3306/customer_management
spring.datasource.username=root
spring.datasource.password=root
```

**Note**: Schema and master data will be created automatically on first run!

### 2️⃣ Backend Setup

#### Build and Run
```bash
# Navigate to backend directory
cd backend

# Clean and install dependencies
mvn clean install

# Run the application
mvn spring-boot:run
```

The backend will start on **http://localhost:8080**

#### Verify Backend
```bash
# Check health (you can add actuator endpoint)
curl http://localhost:8080/api/customers

# Expected: Empty list or sample data
```

### 3️⃣ Frontend Setup

#### Install Dependencies and Run
```bash
# Navigate to frontend directory
cd frontend

# Install npm packages
npm install

# Start development server
npm start
```

The frontend will start on **http://localhost:3000**

---

## 📁 Project Structure

```
customer-management/
├── backend/                          # Spring Boot application
│   ├── src/main/java/com/cms/
│   │   ├── entity/                   # JPA entities (Customer, Address, etc.)
│   │   ├── repository/               # Spring Data JPA repositories
│   │   ├── dto/                      # Data Transfer Objects
│   │   ├── service/                  # Business logic layer
│   │   ├── controller/               # REST API endpoints
│   │   ├── config/                   # Configuration classes (CORS, etc.)
│   │   └── util/                     # Helper classes (ExcelHelper)
│   ├── src/main/resources/
│   │   ├── application.properties    # App configuration
│   │   ├── schema.sql                # Database DDL
│   │   └── data.sql                  # Master data DML
│   ├── src/test/java/com/cms/        # JUnit tests
│   └── pom.xml                       # Maven dependencies
│
├── frontend/                         # React application
│   ├── public/
│   ├── src/
│   │   ├── components/               # React components
│   │   ├── services/                 # API service layer
│   │   └── App.js                    # Main app component
│   └── package.json                  # npm dependencies
│
└── README.md                         # This file
```

---

## 🌐 API Documentation

### Base URL
```
http://localhost:8080/api
```

### Customer Endpoints

#### 1. Create Customer
```http
POST /api/customers
Content-Type: application/json

{
  "name": "John Doe",
  "dateOfBirth": "1990-01-15",
  "nicNumber": "123456789V",
  "mobileNumbers": [
    {"number": "+94771234567"}
  ],
  "addresses": [
    {
      "addressLine1": "123 Main St",
      "addressLine2": "Apt 4B",
      "cityId": 1
    }
  ],
  "familyMemberIds": [2, 3]
}
```

**Response**: `201 Created` with customer object

#### 2. Get All Customers (Paginated)
```http
GET /api/customers?page=0&size=20&sortBy=name&sortDir=asc
```

**Response**: `200 OK` with page object

#### 3. Get Customer by ID
```http
GET /api/customers/1
```

**Response**: `200 OK` with customer object

#### 4. Update Customer
```http
PUT /api/customers/1
Content-Type: application/json

{
  "name": "John Updated",
  ...
}
```

**Response**: `200 OK` with updated customer

#### 5. Delete Customer
```http
DELETE /api/customers/1
```

**Response**: `204 No Content`

### Bulk Upload Endpoints

#### 6. Upload Excel File
```http
POST /api/bulk/upload
Content-Type: multipart/form-data

file: [Excel file]
```

**Response**: `200 OK` with upload results
```json
{
  "totalRecords": 1000,
  "successCount": 995,
  "failureCount": 5,
  "processingTimeMs": 15000,
  "errors": [
    {
      "rowNumber": 5,
      "field": "nicNumber",
      "message": "Duplicate NIC number"
    }
  ]
}
```

#### 7. Download Template
```http
GET /api/bulk/template
```

**Response**: Excel file download

---

## 📊 Excel File Format for Bulk Upload

### Column Structure
| Column | Field | Required | Example |
|--------|-------|----------|---------|
| A | Name | ✅ Yes | John Doe |
| B | Date of Birth | ✅ Yes | 1990-01-15 |
| C | NIC Number | ✅ Yes | 123456789V |
| D | Mobile 1 | ❌ No | +94771234567 |
| E | Mobile 2 | ❌ No | +94712345678 |
| F | Mobile 3 | ❌ No | +94769876543 |
| G | Address 1 - Line 1 | ❌ No | 123 Main St |
| H | Address 1 - Line 2 | ❌ No | Apt 4B |
| I | Address 1 - City | ❌ No | Colombo |
| J | Address 2 - Line 1 | ❌ No | 456 Park Ave |
| K | Address 2 - Line 2 | ❌ No | |
| L | Address 2 - City | ❌ No | Kandy |

### Download Sample Template
Simply call `GET /api/bulk/template` to download a pre-formatted Excel template.

---

## 🧪 Testing

### Run All Tests
```bash
cd backend
mvn test
```

### Run Specific Test Class
```bash
mvn test -Dtest=CustomerServiceTest
```

### Generate Coverage Report
```bash
mvn test jacoco:report

# Open report
# File: target/site/jacoco/index.html
```

### Test Structure
- **Unit Tests**: Service and utility classes (with Mockito mocks)
- **Integration Tests**: Full application context tests
- **Coverage Goal**: 80%+ code coverage

---

## 🎓 Key Learning Concepts

This project demonstrates:

### Spring Boot Concepts
- **Dependency Injection** & IoC Container
- **Spring Data JPA** & Repository pattern
- **Transaction Management** with @Transactional
- **REST API** design and best practices
- **Exception Handling** & error responses
- **CORS Configuration** for frontend integration

### JPA/Hibernate Concepts
- **Entity Relationships**: @OneToMany, @ManyToOne, @ManyToMany
- **Cascade Operations** & Orphan Removal
- **Lazy vs Eager Loading**
- **JOIN FETCH** to prevent N+1 queries
- **Batch Operations** for performance

### Database Concepts
- **Foreign Key Constraints**
- **Composite Primary Keys**
- **ON DELETE CASCADE/RESTRICT**
- **Indexes** for query optimization
- **Transactions** & ACID properties

### Design Patterns
- **Layered Architecture** (Controller → Service → Repository)
- **DTO Pattern** for API layer separation
- **Repository Pattern** for data access
- **Builder Pattern** (Lombok @Builder)

### Performance Optimization
- **Chunk-based Processing** for large datasets
- **Streaming APIs** for memory efficiency
- **Connection Pooling** (HikariCP)
- **Batch INSERT** operations
- **Master Data Caching**

### Testing
- **Unit Testing** with JUnit 5
- **Mocking** with Mockito
- **AAA Pattern** (Arrange-Act-Assert)
- **Code Coverage** with JaCoCo

---

## 🔧 Troubleshooting

### Common Issues

#### 1. Database Connection Failed
```
Error: Access denied for user 'root'@'localhost'
```
**Solution**: Check username/password in `application.properties`

#### 2. Out of Memory Error (Bulk Upload)
```
java.lang.OutOfMemoryError: Java heap space
```
**Solution**: Increase JVM heap size
```bash
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Xmx2g"
```

#### 3. CORS Error
```
Access to XMLHttpRequest blocked by CORS policy
```
**Solution**: Verify CORS configuration in `CorsConfig.java` includes your frontend URL

#### 4. Schema Not Created
```
Table 'customer' doesn't exist
```
**Solution**: Check `application.properties`:
```properties
spring.sql.init.mode=always
spring.jpa.hibernate.ddl-auto=update
```

---

## 📚 Additional Resources

### Documentation
- [Spring Boot Reference](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Spring Data JPA](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/)
- [Hibernate ORM](https://hibernate.org/orm/documentation/)
- [Apache POI](https://poi.apache.org/components/spreadsheet/)
- [React Documentation](https://react.dev/)

### Tutorials Used
- Spring Boot + JPA relationships
- Bulk operations with large datasets
- Excel processing in Java
- REST API best practices

---

## 👨‍💻 Development

###Built With Love  For Learning

This project was built with extensive inline documentation to serve as a learning resource. Every significant code block includes:
- **Concept explanations** (marked with 🎓)
- **Best practices** and pitfalls to avoid
- **Alternative approaches** and trade-offs
- **Real-world examples**

Feel free to explore the code and learn from the comments!

---

## 📄 License

This project is created for educational purposes.

---

## 🤝 Contributing

This is an educational project. Feel free to:
- Fork and experiment
- Suggest improvements
- Report issues
- Add more features

---

## 📧 Questions?

If you have questions about the code or concepts, check the inline documentation first—it's extensive! Each component includes detailed educational comments explaining the "what" and "why."

---

**Happy Learning! 🎓**
