# AgriFarm - Farm Management System

## Project Overview

AgriFarm is a comprehensive farm management application built with JavaFX that provides tools for farmers, agricultural businesses, and consumers. The system offers features for managing farms, fields, crops, soil data, product inventory, orders, user accounts, articles, and more. It includes administrative dashboards, user interfaces, payment processing, and various agricultural management tools.

## Features

### Farm Management
- Farm registration and management
- Field tracking and monitoring
- Crop planning and scheduling
- Soil data analysis and tracking
- Task management for agricultural activities

### Product Management
- Product catalog with categories
- Inventory management
- Product approval workflow
- Image support for products

### E-commerce
- Shopping cart functionality
- Order processing
- Multiple payment methods including Stripe integration
- Order status tracking

### User Management
- User registration and authentication
- Role-based access control (Admin/User)
- Profile management with profile pictures
- Email verification
- Google OAuth integration

### Content Management
- Article publishing system
- Comment and rating system
- Featured content management

### Additional Features
- QR code generation using Google ZXing
- PDF document generation with Apache PDFBox
- SMS notifications via Twilio
- Email notifications
- Weather data integration
- OpenCV integration for image processing

## Technology Stack

- **Language**: Java 17
- **Frontend**: JavaFX 17.0.8, CSS, FXML
- **Database**: MySQL
- **Authentication**: JWT, BCrypt password hashing, Google OAuth
- **Payment Processing**: Stripe
- **Notifications**: Twilio (SMS), JavaMail (Email)
- **Build Tool**: Maven
- **Additional Libraries**:
  - Apache PDFBox for PDF generation
  - ZXing for QR code generation
  - Jackson and GSON for JSON processing
  - OpenCV for image processing
  - ControlsFX for enhanced UI components
  - SLF4J and Logback for logging

## Project Structure

```
agrifarm/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   ├── controller/      # UI Controllers for JavaFX views
│   │   │   ├── database/        # Database connection and DAO classes
│   │   │   ├── entite/          # Entity classes (domain model)
│   │   │   ├── models/          # Data models and business logic
│   │   │   ├── service/         # Service layer for business operations
│   │   │   ├── test/            # Main application entry point
│   │   │   ├── utils/           # Utility classes and helpers
│   │   │   └── views/           # View-related classes
│   │   ├── resources/
│   │   │   ├── css/             # CSS stylesheets
│   │   │   ├── fxml/            # FXML layout files
│   │   │   ├── images/          # Application images
│   │   │   └── profile_pics/    # User profile pictures
│   ├── test/                    # Test classes
│   └── user_data/               # User-specific data storage
├── config/                      # Configuration files
├── tokens/                      # Authentication tokens
└── target/                      # Build output directory
```

## Entity Relationships

### Main Entities
- **User**: System user with roles (Admin/User)
- **Farm**: Agricultural property with location, features, and budget information
- **Field**: Subdivision of a farm with specific crops and tasks
- **Crop**: Cultivation data including type, planting dates, and harvesting information
- **SoilData**: Soil quality metrics for agricultural analysis
- **Produit**: Products available for sale
- **Commande**: Order information with products and payment details
- **Article**: Content pieces with title, content, and images
- **Commentaire**: User comments and ratings on articles

## Setup Instructions

### Prerequisites
- Java Development Kit (JDK) 17 or higher
- Maven 3.8+ for dependency management
- MySQL Server 8.0+
- IDE (IntelliJ IDEA recommended)

### Database Setup
1. Create a new MySQL database named `agrifarm`
2. Configure the database connection in the application (likely in the database package)

### Build and Run
1. Clone the repository:
   ```bash
   git clone [repository-url]
   cd agrifarm
   ```

2. Build the project with Maven:
   ```bash
   mvn clean install
   ```

3. Run the application:
   ```bash
   mvn javafx:run
   ```
   
   Alternatively, run the main class directly:
   ```bash
   java -jar target/agrifarm-1.0-SNAPSHOT.jar
   ```

### Configuration
- API keys for services like Stripe, Twilio, and Google OAuth should be configured in the appropriate configuration files
- Database connection details can be modified in the database connection manager

## Dependencies

The project uses various dependencies managed by Maven, including:

- JavaFX components for UI
- MySQL Connector for database access
- Jackson and GSON for JSON parsing
- BCrypt for password hashing
- Stripe API for payment processing
- Twilio for SMS notifications
- PDFBox for PDF generation
- ZXing for QR code generation
- OpenCV for image processing

For a complete list, refer to the `pom.xml` file.

## License

[Specify License Information]

## Contributors

[List Project Contributors]

"# agrifarmjava wjavafx" 
