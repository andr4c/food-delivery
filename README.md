# Sub-Functionality of Food Delivery Application


Sub-functionality of the food delivery application, which calculates the delivery fee for 
food couriers based on regional base fee, vehicle type, and weather conditions.


## Features


- H2 Database for storing and manipulating data
- Configurable scheduled task for importing weather data (CronJob)
- Functionality to calculate delivery fee
- REST interface, which enables to request of the delivery fee according to input parameters
- Swagger API UI available at: http://localhost:8080/swagger-ui/index.html#/
- H2 console available at: http://localhost:8080/h2-console/
- Business rules for base fees and extra fees could be managed (CRUD) through the REST interface.


## Dependencies


- SpringDoc OpenAPI (2.3.0)
- Hibernate Validator (8.0.1.Final)
- Jakarta Expression Language Implementation (5.0.0)
- Jakarta Expression Language API (5.0.1)
- JSoup (1.19.1)
- Spring Boot Starter JPA
- Spring Boot Starter Web
- Lombok
- H2 Database


## Getting Started


### Prerequisites

- Java 17 or later
- Gradle (if not using the wrapper script)


### Setup and Installation


1. Clone the repository:

```
git clone https://github.com/andr4c/food-delivery
cd food-delivery
```

2. Build the project using Gradle:

```
./gradlew build
```

3. Run the project:

```
./gradlew bootRun
```


## H2 Database

### Configuration

```
spring.datasource.name=food-delivery
spring.h2.console.path=/h2-console
spring.datasource.url=jdbc:h2:file:./data/food-delivery-db
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
```


## API Endpoints

### ``BaseFeeController``

- Get All Base Fees: ``GET /api/base-fee``
- Create a New Base Fee: ``POST /api/base-fee``
- Create Base Fees in Batch: ``POST /api/base-fee/batch``
- Update a Base Fee: ``PUT /api/base-fee/{id}``
- Delete a Base Fee: ``DELETE /api/base-fee/{id}``


### ``ExtraFeeController``

- Get All Extra Fees: ``GET /api/extra-fee``
- Create a New Extra Fee: ``POST /api/extra-fee``
- Create Extra Fees in Batch: ``POST /api/extra-fee/batch``
- Update an Extra Fee: ``PUT /api/extra-fee/{id}``
- Delete an Extra Fee: ``DELETE /api/extra-fee/{id}``


### ``DeliveryFeeController``

- Calculate Delivery Fee: ``POST /api/delivery-fee``


## Authors


**Andra Rajaste**  
andra.rajaste@gmail.com  
[GitHub](https://github.com/andr4c)