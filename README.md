# School Learning Management System (SLMS)

School Learning Management System (SLMS) is a comprehensive web application designed to manage the administrative and academic aspects of educational institutions. It provides functionalities such as user management (students, teachers, admins, non-teaching staff), session management, class management, fee processing, transfer certificate requests, and role-based access controls.

---

## 🚀 Features

- **User Management**: Handle Students, Teachers, Admins, and Non-Teaching Staff with role-based access control.
- **Session Management**: Add, update, activate, or deactivate academic sessions.
- **Class Management**: Manage class entities under active sessions, including fee structures and student enrollments.
- **Fee Management**: Track payments, dues, and disallow fee payment if previous dues exist.
- **Transfer Certificate Requests**: Students can request TCs, and admins can approve/reject them.
- **Attendance Management**: Role-based attendance APIs for teachers and students.
- **Time Table Scheduling**: Assign and view class-wise time tables.
- **Secure Authentication**: JWT-based login and access control for all endpoints.
- **Comprehensive Logging**: Track all user actions and system events.
- **RESTful APIs**: Well-structured and documented endpoints for frontend integration.
- **Swagger/OpenAPI**: Integrated API documentation via Swagger UI for quick testing and reference.

---

## 🛠️ Technologies Used

- **Backend**: Java 17+, Spring Boot 3, Spring Data JPA, Hibernate  
- **Database**: MySQL  
- **Security**: Spring Security with JWT  
- **Object Mapping**: ModelMapper  
- **Build Tool**: Maven or Gradle  
- **Logging**: SLF4J with Logback  
- **IDE**: IntelliJ IDEA (recommended)

---

## ⚙️ Setup & Installation

### ✅ Prerequisites

Make sure the following are installed and set up on your system:

- Java 17 or higher  
- Maven or Gradle  
- MySQL or a compatible database (installed and running)  
- IDE – IntelliJ IDEA is recommended  

---

### 🔐 Configuration

A file named **slms.txt** has been pushed to the repository.  
It contains essential configuration values such as:

- Database credentials  
- JWT secret key  
- Admin login details

> ✏️ Replace the default values in `slms.txt` with your own before running the app.

---

### 🛠 Set the Environment Variable

To allow the application to read from `slms.txt`, set a system environment variable named `SLMS_CONFIG` pointing to the file's absolute path:

#### For **Windows (CMD)**:
```cmd
set SLMS_CONFIG=C:\absolute\path\to\slms.txt
```

#### For **Linux / macOS**:
```bash
export SLMS_CONFIG=/absolute/path/to/slms.txt
```

---

### 📦 Clone the Repository

```bash
git clone https://github.com/shivk1709/slms.git
cd slms
```

---

### ▶️ Run the Application

Once the repository is cloned and the `SLMS_CONFIG` environment variable is set, follow these steps to run the application:

1. **Open the project in IntelliJ IDEA**:
   - Launch IntelliJ  
   - Click **File > Open**  
   - Select the `slms` folder  

2. **Wait for IntelliJ to import dependencies** (via Maven or Gradle).

3. **Locate the main class**, typically:
   ```
   src/main/java/com/java/slms/SlmsApplication.java
   ```
   > (Update with your actual package name if different.)

4. **Right-click** the file and select:
   ```
   Run 'SlmsApplication.main()'
   ```

The Spring Boot application should now start, and you'll see logs in the console indicating it's running on:

```
http://localhost:8080
```

---

### 🌐 Access API

The default REST API root is:

```
http://localhost:8080/api/
```

All endpoints defined in the Postman collection and backend services are prefixed with `/api/`.  
Make sure the application is running locally and the `SLMS_CONFIG` environment variable is properly set before making requests.

---

### 📖 Swagger UI (API Documentation)

Swagger UI is integrated to test and explore APIs in your browser. Once the application is running, access it at:

```
http://localhost:8080/swagger-ui/index.html#
```

This provides a user-friendly interface to view, test, and validate all available REST APIs.

---

### 🧪 Test the APIs using Postman

A Postman collection file named **`SLMS_APIS.postman_collection.json`** is included in the repository.

#### 📌 To Use the Collection:

1. Open **Postman**
2. Click **Import**
3. Select the file `SLMS_APIS.postman_collection.json`
4. The collection titled **"SLMS APIS"** will appear in your workspace

> ✅ Make sure your backend server is running before hitting the endpoints.

---

### 📝 Optional: Sample Endpoints

Here are some example endpoints included in the Postman collection:

- `POST /api/auth/login` – Authenticate a Admin and receive a JWT token  
- `POST /api/auth/register/student` – Register Students (admin access required)  
- `POST /api/auth/register/staff` – Register Staff (admin access required)  
- `PUT /api/fees/pay` – Pay the fees  (admin access required)

> 🛠 Update the base URL in Postman if your application is running on a different port or environment.

---

