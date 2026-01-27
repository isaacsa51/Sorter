# Contributing to Sorter

First off, thank you for considering contributing to Sorter! 🎉

## Code of Conduct

This project adheres to a code of conduct. By participating, you are expected to uphold this code and treat all contributors with respect.

## How Can I Contribute?

### Reporting Bugs

Before creating bug reports, please check the existing issues to avoid duplicates. When creating a bug report, include:

- **Clear title** describing the issue
- **Detailed description** of the problem
- **Steps to reproduce** the behavior
- **Expected behavior** vs actual behavior
- **Screenshots** if applicable
- **Device/Environment info:**
  - Android version
  - Device model
  - App version

### Suggesting Features

Feature requests are welcome! Please provide:

- **Clear use case** for the feature
- **Detailed description** of the proposed functionality
- **Mockups or examples** if applicable
- **Priority level** (nice-to-have vs critical)

### Pull Requests

1. **Fork the repository**
2. **Create a feature branch** from `develop`:
   ```bash
   git checkout develop
   git pull origin develop
   git checkout -b feature/amazing-feature
   ```

3. **Make your changes** following the coding standards
4. **Test locally** before pushing:
   ```bash
   ./gradlew lint
   ./gradlew detekt
   ./gradlew testDebugUnitTest
   ./gradlew assembleDebug
   ```

5. **Commit your changes** with clear messages:
   ```bash
   git commit -m "Add feature: brief description"
   ```

6. **Push to your fork**:
   ```bash
   git push origin feature/amazing-feature
   ```

7. **Create a Pull Request** to the `develop` branch

## Development Setup

### Prerequisites

- **Android Studio** Hedgehog (2023.1.1) or later
- **JDK 17** or later
- **Android SDK** with API 31+
- **Git**

### Initial Setup

1. **Clone the repository**:
   ```bash
   git clone https://github.com/isaacsa51/Sorter.git
   cd Sorter
   ```

2. **Open in Android Studio**
   - File → Open → Select project directory
   - Wait for Gradle sync to complete

3. **Run the app**:
   - Connect a device or start an emulator
   - Click Run

### Project Structure

```
Sorter/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/serranoie/app/media/sorter/
│   │   │   │   ├── data/          # Data layer (repositories, data sources)
│   │   │   │   ├── domain/        # Domain layer (use cases, models)
│   │   │   │   ├── presentation/  # UI layer (screens, viewmodels)
│   │   │   │   └── di/            # Dependency injection modules
│   │   │   └── res/               # Resources (layouts, strings, etc.)
│   │   └── test/                  # Unit tests
│   └── build.gradle.kts
├── .github/
│   └── workflows/                 # CI/CD pipelines
└── build.gradle.kts
```

## Coding Standards

### Kotlin Style Guide

- Follow [Kotlin coding conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Use meaningful variable and function names
- Keep functions small and focused (single responsibility)
- Add comments for complex logic
- Use type inference when obvious

### Architecture Guidelines

**MVVM Pattern:**
- **Model**: Data classes and business logic
- **View**: Composable functions (UI)
- **ViewModel**: State management and business logic coordination

**Compose Best Practices:**
- Keep composables small and reusable
- Hoist state when needed
- Use `remember` and `rememberSaveable` appropriately
- Prefer stateless composables

**Dependency Injection:**
- Use Hilt for DI
- Inject dependencies through constructors
- Avoid service locator pattern

### Git Commit Messages

Follow [Conventional Commits](https://www.conventionalcommits.org/):

```
<type>(<scope>): <subject>

<body>

<footer>
```

**Types:**
- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation changes
- `style`: Code style changes (formatting, etc.)
- `refactor`: Code refactoring
- `perf`: Performance improvements
- `test`: Adding or updating tests
- `chore`: Build process or auxiliary tool changes
- `ci`: CI/CD changes

**Examples:**
```
feat(sorting): add video sorting capability

fix(ui): resolve crash on empty gallery

docs(readme): update installation instructions

refactor(viewmodel): simplify state management
```

## Testing

### Unit Tests

- Write tests for ViewModels and business logic
- Use JUnit 4 and Mockito
- Aim for meaningful test coverage
- Test edge cases and error scenarios

```kotlin
@Test
fun `test sorting logic with empty list`() {
    // Arrange
    val emptyList = emptyList<Media>()
    
    // Act
    val result = sorter.sort(emptyList)
    
    // Assert
    assertTrue(result.isEmpty())
}
```

### UI Tests (Optional, but recommended)

- Use [Compose Testing APIs](https://developer.android.com/jetpack/compose/testing)

- Use Compose Testing APIs
- Test user interactions
- Verify UI state changes

## CI/CD

All PRs must pass CI checks:

✅ **Build and Test**
- Lint checks
- Detekt static analysis
- Unit tests
- Debug APK build

✅ **Code Quality**
- Detekt static analysis
- Dependency checks

**Run all checks locally:**
```bash
./gradlew lint detekt testDebugUnitTest assembleDebug
```

## Documentation

- Update README for new features
- Add KDoc comments for public APIs
- Update relevant markdown files in `.github/`
- Include screenshots for UI changes

## Review Process

1. **Automated checks** must pass
2. **Code review** by maintainer(s)
3. **Changes requested** (if needed)
4. **Approval** and merge

### What Reviewers Look For

- ✅ Code quality and style
- ✅ Test coverage
- ✅ Performance implications
- ✅ Security considerations
- ✅ Backward compatibility
- ✅ Documentation completeness

## Release Process

1. **Feature development** on feature branches
2. **Merge to develop** after review
3. **Testing** on develop branch
4. **Merge to main** for releases
5. **Tag releases** with version numbers

## Need Help?

- Open a discussion for questions
- Create an issue for bugs
- Contact maintainers

## Recognition

Contributors will be acknowledged in:
- Release notes
- README (for significant contributions)
- GitHub contributors page

Thank you for making Sorter better!
