# EduStream Learning Platform

EduStream is a modern, interactive e-learning platform designed to provide a seamless educational experience. With its futuristic UI design, collaborative features, and personalized learning paths, EduStream enables users to create, share, and track their learning journey.

## Features

### Core Features
- **Interactive Dashboard**: A visually stunning dashboard with progress tracking and quick access to personalized learning content
- **Learning Plans**: Create and customize learning plans with specific goals, deadlines, and resources
- **Course Exploration**: Discover trending courses and learning materials from the community
- **Progress Tracking**: Monitor your learning journey with detailed progress statistics and achievements
- **Social Learning**: Connect with other learners, follow their progress, and engage in discussions
- **Bookmarks**: Save and organize your favorite learning resources for quick access

### User Experience
- **Modern UI**: A sleek, cosmic-themed user interface with glassmorphism design elements
- **Responsive Design**: Fully responsive layout that works on all devices - desktop, tablet, and mobile
- **Dark Mode**: Eye-friendly dark theme for comfortable learning in any environment
- **Streamlined Navigation**: Intuitive navigation with quick access to all platform features

## Technical Stack

- **Backend**: Spring Boot, Java
- **Frontend**: Thymeleaf, Bootstrap 5, HTML/CSS, JavaScript
- **Security**: Spring Security with OAuth2 support
- **Database**: Compatible with SQL databases

## Getting Started

### Prerequisites
- Java 11 or higher
- Maven 3.6+ or Gradle 7+
- MySQL (or any compatible database)

### Setup

1. Clone the repository:
   ```
   git clone https://github.com/yourusername/newedustream.git
   cd newedustream
   ```

2. Configure database connection in `src/main/resources/application.properties`:
   ```
   spring.datasource.url=jdbc:mysql://localhost:3306/edustream
   spring.datasource.username=yourdbusername
   spring.datasource.password=yourdbpassword
   ```

3. Build the project:
   ```
   ./mvnw clean install
   ```

4. Run the application:
   ```
   ./mvnw spring-boot:run
   ```

5. Access the application at:
   ```
   http://localhost:8080
   ```

## Usage

1. **Registration**: Create a new account by clicking on the "Register" button
2. **Dashboard**: Upon login, you'll be greeted with your personalized dashboard
3. **Learning Plans**: Create learning plans from the "Create Learning Plan" option
4. **Explore Content**: Discover learning materials through the "Explore" or "Trending" sections
5. **Track Progress**: View your progress on the dashboard and learning plan details

## Contributing

Contributions are welcome! Please feel free to submit a pull request or open an issue for any bugs, features, or improvements.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a pull request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Screenshots

(Add screenshots of key pages - Dashboard, Learning Plans, Trending, etc.)

## Acknowledgements

- Built with [Spring Boot](https://spring.io/projects/spring-boot)
- UI components powered by [Bootstrap](https://getbootstrap.com/)
- Icons by [Bootstrap Icons](https://icons.getbootstrap.com/)
- Fonts by [Google Fonts](https://fonts.google.com/)