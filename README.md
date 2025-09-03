# Bank Management System

A full-stack application for managing bank accounts and transfers, built with Java Spring Boot backend and React TypeScript frontend.

## Features

### Account Management
- View list of all bank accounts
- Create new accounts with unique names and IBANs
- Edit existing account details
- Freeze/unfreeze accounts
- View account balances

### Transfer Management
- View all transfers for specific accounts
- Create transfers between accounts
- Real-time balance updates
- Transfer history tracking

## Technology Stack

### Backend
- **Java 17**
- **Spring Boot 3.1.5**
- **Spring Data JPA**
- **PostgreSQL Database** (H2 for quick testing)
- **Maven** for dependency management

### Frontend
- **React 18** with TypeScript
- **Axios** for API communication
- **CSS3** for styling

## Project Structure

```
joni/
├── backend/                    # Java Spring Boot application
│   ├── src/main/java/com/bankmanagement/
│   │   ├── entity/            # JPA entities (Account, Transfer)
│   │   ├── repository/        # Data access layer
│   │   ├── service/           # Business logic layer
│   │   ├── controller/        # REST API endpoints
│   │   └── BankManagementApplication.java
│   ├── src/main/resources/
│   │   └── application.properties
│   └── pom.xml
├── frontend/                   # React TypeScript application
│   ├── src/
│   │   ├── components/        # React components
│   │   ├── services/          # API service layer
│   │   ├── types/             # TypeScript type definitions
│   │   └── App.tsx
│   ├── package.json
│   └── tsconfig.json
└── README.md
```

## API Endpoints

### Account Management
- `GET /api/accounts` - Get all accounts
- `GET /api/accounts/{id}` - Get account by ID
- `POST /api/accounts` - Create new account
- `PUT /api/accounts/{id}` - Update account
- `PUT /api/accounts/{id}/freeze` - Freeze account
- `PUT /api/accounts/{id}/unfreeze` - Unfreeze account
- `DELETE /api/accounts/{id}` - Delete account

### Transfer Management
- `GET /api/transfers` - Get all transfers
- `GET /api/transfers/{id}` - Get transfer by ID
- `GET /api/transfers/account/{accountId}` - Get transfers for specific account
- `POST /api/transfers` - Create new transfer

## Database Schema

### Accounts Table
- `id` (Primary Key)
- `name` (Unique)
- `iban` (Unique)
- `status` (ACTIVE/FROZEN)
- `available_amount`
- `created_on`
- `modified_on`

### Transfers Table
- `id` (Primary Key)
- `account_id`
- `beneficiary_account_id`
- `type` (CREDIT/DEBIT)
- `amount`
- `created_on`
- `modified_on`

## Getting Started

### Prerequisites
- Java 17 or higher
- Maven 3.6+
- Node.js 16+ and npm
- PostgreSQL 12+ (or Docker)

### Database Setup
1. **Option 1 - Docker (Recommended):**
   ```bash
   docker run --name bankdb-postgres \
     -e POSTGRES_DB=bankdb \
     -e POSTGRES_USER=postgres \
     -e POSTGRES_PASSWORD=password \
     -p 5432:5432 \
     -d postgres:15
   ```

2. **Option 2 - Local PostgreSQL:**
   - Install PostgreSQL from https://www.postgresql.org/
   - Create database: `CREATE DATABASE bankdb;`
   - Use credentials: postgres/password

See [POSTGRESQL_SETUP.md](POSTGRESQL_SETUP.md) for detailed instructions.

### Backend Setup
1. Navigate to the backend directory:
   ```bash
   cd backend
   ```

2. Install dependencies and run the application:
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```

3. The backend will start on `http://localhost:8080`
4. Database tables will be automatically created on first run

### Frontend Setup
1. Navigate to the frontend directory:
   ```bash
   cd frontend
   ```

2. Install dependencies:
   ```bash
   npm install
   ```

3. Start the development server:
   ```bash
   npm start
   ```

4. The frontend will start on `http://localhost:3000`

## Usage

1. **Create Accounts**: Start by creating bank accounts with unique names and IBANs
2. **Manage Accounts**: View all accounts, freeze/unfreeze them as needed
3. **Create Transfers**: Transfer money between active accounts
4. **View Transfer History**: Monitor all transfers for specific accounts

## Data Validation

- Account names and IBANs must be unique
- IBAN format validation (basic pattern matching)
- Transfer amounts must be positive
- Transfers only allowed between active accounts
- Sufficient balance validation for transfers

## Development Features

- **Hot Reload**: Both frontend and backend support hot reload during development
- **Error Handling**: Comprehensive error handling with user-friendly messages
- **Responsive Design**: Mobile-friendly UI
- **Type Safety**: Full TypeScript support in frontend

## Future Enhancements

- User authentication and authorization
- Account types (Savings, Checking, etc.)
- Transfer fees and limits
- Transaction categories and descriptions
- Export functionality for statements
- Email notifications for transfers
- Real-time notifications
- Audit logging

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## License
