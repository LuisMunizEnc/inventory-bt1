# Inventory Management Application

This Breakable Toy I was designed for product inventory management, allowing users to view, add, update, and delete products.

## Technologies Used

### Front-end
* **Technology:** ReactJS with TypeScript
* **State Management:** Redux 
* **Execution Port:** `8080`

### Back-end
* **Technology:** Java with Spring Boot
* **Dependency Management:** Maven
* **Execution Port:** `9090`

## Project Structure

The repository is divided into two main folders:

* `frontend/`: Contains all source code and configuration for the React project.
* `backend/`: Contains all source code and configuration for the Spring Boot project.

## Prerequisites

Before running the application, make sure you have the following installed:

* **Node.js and npm:** For the front-end. You can download them from [nodejs.org](https://nodejs.org/).
* **Java Development Kit (JDK) 17 or higher:** For the back-end (v24 used).
* **Maven:** For the back-end. You can download it from [maven.apache.org](https://maven.apache.org/).

## How to Run the Application

Follow these steps to get the complete application up and running:

### 1. Run the Back-end

First, start the Spring Boot server.

1.  Navegate to the back-end folder:
    ```bash
    cd backend
    ```
2.  Execute the Spring Boot application:
    ```bash
    mvn spring-boot:run
    ```
    The back-end will start on `http://localhost:9090`.

### 2. Run the Front-end

Once the back-end is running, you can start the front-end.

1.  Open a **new terminal** and navigate to the front-end folder:
    ```bash
    cd frontend
    ```
2.  Install project dependencies (only the first time or if there are changes in `package.json`):
    ```bash
    npm install
    ```
3.  Build the application:
    ```bash
    npm install
    ```
3.  Run the React application:
    ```bash
    npm run start
    ```
    The front-end will automatically open in your browser on `http://localhost:8080`.

## Running Tests

### Front-end Tests

To run all front-end unit and integration tests:

1.  Navigate to the front-end folder:
    ```bash
    cd frontend
    ```
2.  Run the tests:
    ```bash
    npm run tests
    ```

### Back-end Tests

To run all back-end unit and integration tests (full code coverage):

1.  Navigate to the back-end folder:
    ```bash
    cd backend
    ```
2.  Run the tests:
    ```bash
    mvn test
    ```