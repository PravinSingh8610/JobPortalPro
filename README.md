# 🚀 JobPortalPro – Online Job Portal Management System

**JobPortalPro** is a Java-based full-stack web application developed using Spring Boot, Thymeleaf, Bootstrap, and MySQL. It provides a platform where candidates can search and apply for jobs, while recruiters can manage job postings and the complete recruitment process.

The application is designed to simplify recruitment activities by bringing candidates and recruiters together on a single platform.

---

## 📌 Project Overview

JobPortalPro provides separate functionalities for Candidates and Recruiters.

- Candidates can register, log in, search for jobs, apply for positions, and track their applications.
- Recruiters can manage job postings, review applications, schedule interviews, generate offer letters, and manage onboarding documents.

## ✨ Key Features

### 👨‍💻 Candidate Module

- Candidate Registration and Login
- Candidate Dashboard
- Browse and Search Available Jobs
- Apply for Jobs
- Track Job Application Status
- Interview Details
- View Offer Letters
- Accept or Reject Job Offers
- Upload Onboarding Documents

### 🏢 Recruiter Module

- Recruiter Dashboard
- Post New Jobs
- Manage Job Postings
- View Candidate Applications
- Manage Recruitment Process
- Schedule and Manage Interviews
- Generate Offer Letters
- Manage Candidate Onboarding
- Verify Uploaded Documents

### 🔐 Security & Authentication

- User Registration and Login
- Password Encryption
- Role-Based Access
- Candidate and Recruiter Functionality

### 📂 Document Management

- Candidate Document Upload
- Onboarding Document Management
- Recruiter Document Verification

---

## 🛠️ Technology Stack

| Technology | Purpose |
|---|---|
| Java 21 | Backend Development |
| Spring Boot 4.1.1 | Application Framework |
| Spring MVC | Request Handling |
| Spring Security | Authentication and Authorization |
| Spring Data JPA | Database Operations |
| Hibernate | ORM |
| Thymeleaf | Frontend Template Engine |
| HTML5 | Page Structure |
| CSS3 | Styling |
| Bootstrap 5 | Responsive UI |
| JavaScript | Client-Side Functionality |
| MySQL 8 | Database |
| Maven | Dependency Management |
| Spring Tool Suite | Development IDE |

---

## 🏗️ Project Structure

```text
JobPortalPro
│
├── src
│   ├── main
│   │   ├── java
│   │   │   └── com.jobportal
│   │   │       │
│   │   │       ├── controller
│   │   │       ├── service
│   │   │       ├── repository
│   │   │       ├── entity
│   │   │       ├── dto
│   │   │       ├── config
│   │   │       └── JobPortalProApplication.java
│   │   │
│   │   └── resources
│   │       ├── static
│   │       │   ├── css
│   │       │   ├── js
│   │       │   └── images
│   │       │
│   │       ├── templates
│   │       └── application.properties
│   │
│   └── test
│
├── .mvn
├── mvnw
├── mvnw.cmd
├── pom.xml
└── README.md
```

*Note: The structure above represents the main application layers. Update folder names if your actual package names differ.*

---

## ⚙️ Installation and Setup

Follow these steps to run the project locally.

### 1. Clone the Repository

```bash
git clone https://github.com/PravinSingh8610/JobPortalPro.git
```

### 2. Open the Project

- Open Spring Tool Suite (STS).
- Select File → Import.
- Choose Existing Maven Projects.
- Browse the cloned JobPortalPro folder.
- Click Finish.

### 3. Create MySQL Database

Open MySQL Workbench and execute:

```sql
CREATE DATABASE jobportal_db;
```

### 4. Configure Database

Update your local `application.properties` file:

```properties
spring.application.name=JobPortalPro

server.port=8081

spring.datasource.url=jdbc:mysql://localhost:3306/jobportal_db
spring.datasource.username=root
spring.datasource.password=YOUR_MYSQL_PASSWORD

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

spring.thymeleaf.cache=false
```

Replace `YOUR_MYSQL_PASSWORD` with your local MySQL password.

**Security Note:** Never upload real database passwords or other credentials to GitHub.

### 5. Run the Application

- Right-click on the project.
- Select Run As → Spring Boot App.
- Wait for the application to start.

Open your browser:

```text
http://localhost:8081
```

---

## 🔄 Application Workflow

```text
Candidate Registration
        |
        v
Candidate Login
        |
        v
Browse Available Jobs
        |
        v
Apply for Job
        |
        v
Recruiter Reviews Application
        |
        v
Interview Scheduling
        |
        v
Offer Letter Generation
        |
        v
Offer Acceptance / Rejection
        |
        v
Onboarding Document Upload
        |
        v
Recruiter Document Verification
```

---

## 📸 Application Screenshots

Screenshots of the application will be added here.

### 1. Home Page
![Home Page](screenshots/home.png)

### 2. Registration Page
![Registration Page](screenshots/register.png)

### 3. Login Page
![Login Page](screenshots/login.png)

### 4. Candidate Dashboard
![Candidate Dashboard](screenshots/candidate-dashboard.png)

### 5. Recruiter Dashboard
![Recruiter Dashboard](screenshots/recruiter-dashboard.png)

### 6. Job Posting Page
![Job Posting Page](screenshots/job-posting.png)

### 7. Job Listings
![Job Listings](screenshots/jobs.png)

### 8. Application Management
![Application Management](screenshots/applications.png)

### 9. Interview Management
![Interview Management](screenshots/interviews.png)

### 10. Offer Letter
![Offer Letter](screenshots/offer-letter.png)

*Add your actual screenshots using the same filenames shown above.*

---

## 🎯 Project Objectives

- Develop a full-stack web application using Java and Spring Boot.
- Implement role-based functionality for Candidates and Recruiters.
- Simplify the job application and recruitment process.
- Manage job postings, applications, interviews, and offers.
- Implement database operations using Spring Data JPA and MySQL.

---

## 🔮 Future Enhancements

- Email Notifications
- Advanced Job Search and Filtering
- Resume Upload and Management
- Admin Dashboard
- Job Recommendations
- Analytics and Reporting

---

## 👨‍💻 Developer

**Pravin Singh**

GitHub: [PravinSingh8610](https://github.com/PravinSingh8610)

---

## 📄 License

This project is developed for learning and portfolio purposes.
