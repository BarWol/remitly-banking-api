# Remitly Banking API  

A **Spring Boot** API developed as part of the Remitly internship application, designed to manage banking data with full **CRUD operations**.  
The application parses a **CSV file** to populate the database and provides **RESTful endpoints** to interact with banking data.  

## Features  

- **CRUD Operations** – Full support for creating, reading, updating, and deleting banking records.  
- **CSV Parsing** – Automatically loads banking data from a CSV file into the database.  
- **PostgreSQL Database** – Persistent storage for banking data.  
- **Docker Integration** – Simplified setup and deployment using Docker.  
- **RESTful API** – Provides endpoints for managing banking data, including headquarters and branches.  

## Technologies Used  

- **Spring Boot** – Backend framework for building the API.  
- **PostgreSQL** – Relational database for storing banking data.  
- **Docker** – Containerization for easy deployment and development.  
- **CSV Parsing** – Utilizes OpenCSV (or similar libraries) for processing and loading data.  
- **RESTful Web Services** – Exposes endpoints for seamless data interaction.  

## API Endpoints  

| Method   | Endpoint                        | Description                                 |
|----------|---------------------------------|---------------------------------------------|
| **GET**  | `/v1/test`              | Retrieve all banking records.              |
| **GET**  | `/v1/swift-codes/{swiftCode}`  | Retrieve a specific banking record in json format     |
| **GET**  | `/v1/swift-codes/country/{countryISO2code}`  | Retrieve banks in a country given IS02code   |
| **POST** | `/v1/swift-codes`              | Create a new banking record. The headquarter must be added before branch              |
| **DELETE** | `/v1/swift-codes/{swiftCode}` | Delete a banking record.                   |

## Testing  

Due to time constraints, the project lacks full unit test coverage. However, thorough manual testing during development was conducted using:  

- **Postman** – For API request validation and response testing.  
- **SQL Queries** – To verify database changes and data integrity.  
- **Integration** – Some integration tests were implemented to validate API responses.  

## Learning Experience  

This project provided a great opportunity to enhance my skills in:  

- **Spring Boot**  
- **PostgreSQL** 
- **Docker** 
- **CSV Parsing** 
- **RESTful API Design**
- **Postman**
- **Maven**

### Challenges Faced  

While coding was relatively straightforward and enjoyable, organizing the project properly took some extra effort.  

---

To use this project, ensure that **Docker** is installed
1. clone the repository
2. docker-compose up --build
3. mvn test to run the integration api tests
