📄 Invoice Management System
A full-stack Invoice Management System built with Spring Boot, Thymeleaf, and MySQL, featuring:

✨ Admin & User dashboards
✨ Role-based authentication & authorization
✨ Itemized invoices with GST calculation
✨ PDF invoice generation & email delivery
✨ Invoice search, sort, and filtering
✨ Due date & overdue alerts
✨ Dark mode support
✨ Charts & statistics with Chart.js

🚀 Features
🔐 Role-based Access

Admin: Create, edit, delete, and manage all invoices and users.

User: View assigned invoices, download PDF, track status.

📄 Itemized Invoices

Multiple line items per invoice with quantity, unit price, and GST calculation.

🖨️ PDF Export & Email

Download invoices as PDFs.

Email PDF invoices directly to clients.

📈 Dashboard

Admin & user-specific dashboards with charts (pie chart of invoice statuses).

⏰ Due Dates & Alerts

Tracks due dates & shows overdue invoices.

🌙 Dark Mode

Beautiful light & dark themes using TailwindCSS.

🔍 Search & Filter

Search invoices by client, date, amount, or status.

Sort invoices ascending/descending.

📷 Screenshots
Login Page-
<img width="956" alt="image" src="https://github.com/user-attachments/assets/44c7734d-6587-4653-9a89-530d43f0f2b7" />

Register Page-
<img width="956" alt="image" src="https://github.com/user-attachments/assets/757d7426-838f-44d6-bea6-3ae36729f05d" />

Admin Dashboard-
<img width="944" alt="image" src="https://github.com/user-attachments/assets/2f81d7b5-0cd9-40ee-a8d5-ad4fd41fe6a9" />

Creating Invoice Form-
<img width="944" alt="image" src="https://github.com/user-attachments/assets/8dd657a7-7eb9-4b7e-b63e-b91ed7664dac" />

View All Invoices page-
<img width="959" alt="image" src="https://github.com/user-attachments/assets/4ec6aaee-0ae1-4d9b-a907-4f9f92908698" />

User Dashboard -
<img width="959" alt="image" src="https://github.com/user-attachments/assets/e0a815cf-fd17-47a3-afe8-5d9d8365018f" />



🛠️ Tech Stack
Backend: Spring Boot, Spring Security, Hibernate/JPA 

Frontend: Thymeleaf, TailwindCSS, Chart.js

Database: MySQL

PDF: iTextPDF

Mail: JavaMailSender

📂 Project Structure
arduino
Copy
Edit
src/
├── main/
│   ├── java/
│   │   └── com.example.invoicesystem/
│   │       ├── controller/
│   │       ├── model/
│   │       ├── repository/
│   │       ├── service/
│   │       └── config/
│   ├── resources/
│   │   ├── templates/
│   │   ├── static/
│   │   └── application.properties
🧪 How to Run Locally
Prerequisites
✅ Java 17+
✅ Maven 3.8+
✅ MySQL 8+

Steps
1️⃣ Clone the repository

bash
Copy
Edit
git clone https://github.com/your-username/invoice-system.git
cd invoice-system
2️⃣ Create a MySQL database

sql
Copy
Edit
CREATE DATABASE invoice_dbs;
3️⃣ Update src/main/resources/application.properties with your MySQL credentials.

4️⃣ Build and run the project

bash
Copy
Edit
mvn clean install
mvn spring-boot:run
5️⃣ Visit:

Admin Dashboard: http://localhost:8080/login → login as admin

User Dashboard: http://localhost:8080/login → login as user

🧑‍💻 Default Roles
Role	Username	Password
Admin	admin	admin123
User	user	user123

(or create via signup if enabled.)

📜 To Do / Future Enhancements
✅ Dockerize the application

✅ Deploy to cloud (Railway, Heroku, AWS)

🚀 Add REST API endpoints for external integrations

🚀 Add unit & integration tests

📄 License
MIT License — feel free to use & modify.

🙌 Acknowledgements
Spring Boot documentation

Thymeleaf community

TailwindCSS & Chart.js

iTextPDF
