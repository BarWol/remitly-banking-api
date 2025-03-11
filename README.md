# Remitly Banking API  

A **Spring Boot** API developed as part of the Remitly internship application, designed to manage banking data with full **CRUD operations**.  
The application parses a **CSV file** to populate the database and provides **RESTful endpoints** to interact with banking data.  

## Features  

- **Spring Boot**  
- **PostgreSQL** 
- **Docker** 
- **CSV Parsing** 
- **RESTful API Design**
- **Postman**
- **Maven**
  
## Technologies Used  

- **Spring Boot** 
- **PostgreSQL**   
- **Docker**   
- **CSV Parsing**   
- **RESTful Web Services**   

## API Endpoints  

| Method   | Endpoint                        | Description                                 |
|----------|---------------------------------|---------------------------------------------|
| **GET**  | `/v1/test`              | Retrieve all banking records.              |
| **GET**  | `/v1/swift-codes/{swiftCode}`  | Retrieve a specific banking record in json format     |
| **GET**  | `/v1/swift-codes/country/{countryISO2code}`  | Retrieve banks in a country given IS02code   |
| **POST** | `/v1/swift-codes`              | Create a new banking record. The headquarter must be added before branch              |
| **DELETE** | `/v1/swift-codes/{swiftCode}` | Delete a banking record.                   |

## Testing  

Due to time constraints, the project lacks full unit test coverage with mocking. However, thorough manual testing during development was conducted using:  
- **Postman** – For API request validation and response testing.  
- **SQL Queries** – To verify database changes and data integrity.

  ## Integration tests are present


### Challenges Faced  

While coding was relatively straightforward and enjoyable, organizing the project properly took some extra effort.  

---

To use this project, ensure that **Docker** is installed
1. clone the repository
2. docker-compose up --build
3. mvn test to run the integration api tests

