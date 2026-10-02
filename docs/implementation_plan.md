# CI/CD Pipeline, Code Coverage, and Static Code Analysis

Set up an automated Quality Assurance (QA) and DevOps pipeline for the Android project, including GitHub Actions CI/CD, JaCoCo Code Coverage reporting, Detekt Kotlin static analysis, and Android Lint.

## User Review Required

> [!IMPORTANT]
> **GitHub Repository Requirements**
> The GitHub Actions pipeline (`.github/workflows/android_ci.yml`) will execute automatically on GitHub servers whenever code is pushed to your repository or a pull request is opened.

> [!NOTE]
> **Java JDK 17 Target for CI**
> The CI runner will use JDK 17, which matches Android Gradle Plugin 8.7+ requirements for fast, reproducible builds.

## Proposed Changes

### Build Configuration & Tools

#### [MODIFY] [build.gradle.kts](file:///C:/Users/Mark/StudioProjects/fontos_szakdoga/build.gradle.kts)
- Apply `jacoco` and `io.gitlab.arturbosch.detekt` plugins at top-level.

#### [MODIFY] [app/build.gradle.kts](file:///C:/Users/Mark/StudioProjects/fontos_szakdoga/app/build.gradle.kts)
- Apply `jacoco` and `detekt` plugins.
- Configure `jacocoTestReport` task to calculate code coverage from unit tests (`testDebugUnitTest`).
- Configure `detekt` static analysis block.

#### [NEW] [config/detekt/detekt.yml](file:///C:/Users/Mark/StudioProjects/fontos_szakdoga/config/detekt/detekt.yml)
- Detekt rule configuration file for Kotlin code quality checks.

---

### CI/CD Workflow

#### [NEW] [.github/workflows/android_ci.yml](file:///C:/Users/Mark/StudioProjects/fontos_szakdoga/.github/workflows/android_ci.yml)
- GitHub Actions workflow that automatically:
  1. Checks out repository code.
  2. Sets up Java 17 JDK & Gradle cache.
  3. Runs Android Lint (`./gradlew lintDebug`).
  4. Runs Detekt static code analysis (`./gradlew detekt`).
  5. Runs Unit Tests & generates JaCoCo Coverage Report (`./gradlew testDebugUnitTest jacocoTestReport`).
  6. Uploads Test & Coverage HTML/XML reports as downloadable build artifacts.

---

### Unit Test Suite Expansion

#### [MODIFY] [ApiUnitTest.kt](file:///C:/Users/Mark/StudioProjects/fontos_szakdoga/app/src/test/java/hu/unicon/szakdoga/ApiUnitTest.kt)
- Expand test coverage for data models (`Category`, `Material`, `Folder`, `Document`) and `ApiClient` URL formatting logic.

---

## Verification Plan

### Automated Tests
- Run Detekt static analysis: `./gradlew detekt`
- Run Android Lint: `./gradlew lintDebug`
- Run Unit Tests & JaCoCo Coverage Report: `./gradlew testDebugUnitTest jacocoTestReport`
- Verify coverage HTML report is generated at `app/build/reports/jacoco/jacocoTestReport/html/index.html`.

### Manual Verification
- Commit and push to GitHub repository to verify GitHub Actions workflow runs and passes green.
