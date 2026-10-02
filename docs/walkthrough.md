# Walkthrough - CI/CD Pipeline, Quality Assurance & Oracle DB REST API Integration

Successfully configured an automated DevOps & Quality Assurance (QA) pipeline, including GitHub Actions CI/CD, JaCoCo Code Coverage reporting, Detekt Kotlin static code analysis, and Android Lint, in addition to the Oracle DB REST API architecture.

## Changes Made

### Quality Assurance & DevOps (CI/CD)
- **[.github/workflows/android_ci.yml](file:///C:/Users/Mark/StudioProjects/fontos_szakdoga/.github/workflows/android_ci.yml)**: Automated GitHub Actions CI/CD pipeline executing on every `push` and `pull_request`. Runs Lint, Detekt, Unit Tests, JaCoCo Coverage, builds Debug APK, and uploads test/coverage artifacts.
- **[config/detekt/detekt.yml](file:///C:/Users/Mark/StudioProjects/fontos_szakdoga/config/detekt/detekt.yml)**: Detekt static analysis rule configuration file for Kotlin code quality enforcement.
- **[app/build.gradle.kts](file:///C:/Users/Mark/StudioProjects/fontos_szakdoga/app/build.gradle.kts)**: Configured `jacocoTestReport` task and `detekt` block to generate HTML/XML coverage reports.
- **[ApiUnitTest.kt](file:///C:/Users/Mark/StudioProjects/fontos_szakdoga/app/src/test/java/hu/unicon/szakdoga/ApiUnitTest.kt)**: Expanded unit test suite (6/6 passing unit tests) for data models and JSON deserialization.

### UI Styling & Layouts
- **[pdfopen_page.xml](file:///C:/Users/Mark/StudioProjects/fontos_szakdoga/app/src/main/res/layout/pdfopen_page.xml)**: Updated background to `@drawable/background` and title text color to `@color/white` to match the consistent dark blue gradient theme across all activities.

---

## Verification Results

### Automated Verification
- `./gradlew :app:detekt` -> **SUCCESS (0 errors)**
- `./gradlew :app:testDebugUnitTest :app:jacocoTestReport` -> **SUCCESS (6/6 tests passed, JaCoCo HTML report generated at `app/build/reports/jacoco/jacocoTestReport/html/index.html`)**
- `./gradlew :app:lintDebug` -> **SUCCESS**
- `./gradlew :app:assembleDebug` -> **SUCCESS**
