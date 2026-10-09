# Digital Forensic Screenshot Analyzer - Summary of Work Completed

## Backend (Spring Boot 3.x, Java 17)

### Phase 1: Backend Skeleton
- Created project structure with Maven
- Configured MySQL connection in `application.properties`
- Created JPA entities: User, Role, Case, CaseShare, Screenshot, AnalysisResult, AuditLog, LoginAttempt, CaseNote, ExtractedEvidence
- Created repositories for each entity
- Created `schema.sql` and `data.sql` for database initialization (note: password hashes are placeholders)

### Phase 2: Authentication & Security
- Implemented session-based authentication with Spring Security
- Added BCrypt password hashing
- Implemented account lockout after 5 failed attempts (15 minute lockout)
- Created authentication controllers (register, login, logout, me)
- Added CSRF protection with cookie token repository
- Created custom success/failure handlers to log attempts and manage lockout
- Added role-based access control with `@PreAuthorize`

### Phase 3: Case Management & Screenshot Upload
- Implemented Case service and controller (CRUD with pagination/search)
- Implemented Screenshot service and controller (upload, file serving, metadata)
- Added FileUtil for file validation (MIME type and magic bytes) and hash calculation (MD5, SHA-256)
- Screenshots stored outside web root with UUID filenames
- Added audit logging for actions (login, upload, etc.)
- Added case sharing and notes placeholders (to be completed)

### Phase 4: Forensic Analysis Engine
- Implemented asynchronous analysis with `@Async`
- Created analysis services:
  - MetadataAnalyzer: extracts EXIF/XMP data, detects editing software signatures
  - ErrorLevelAnalysis: generates ELA image by re-compressing JPEG and computing difference
  - TamperHeuristics: placeholder for noise inconsistency, compression grid, duplicated region, edge sharpness
  - OCRAndEvidenceExtractor: uses Tess4J OCR + regex to extract URLs, emails, phone numbers, IPs, amounts, dates
  - PerceptualHash: computes dHash for near-duplicate detection
  - AuthenticityScorer: combines results into 0-100 score and verdict (AUTHENTIC/SUSPICIOUS/LIKELY_TAMPERED)
- Updated Screenshot entity to include perceptual_hash column
- Updated AnalysisResult entity to include ela_image_file_name and JSON columns for detailed results
- Added AnalysisService to orchestrate analysis and update screenshot status (PENDING/PROCESSING/COMPLETED/FAILED)
- Enhanced ScreenshotController with analysis triggering and result retrieval endpoints
- Added ELA image serving endpointVerification
I restarted the backend (running on port 8091) and the frontend (running on port 5188). When you upload a screenshot and click "Analyze" now:

The frontend triggers the API and polls the backend.
The backend successfully transitions the status to PROCESSING.
The OCR step catches the Tesseract error and gracefully degrades.
The Authenticity Scorer factors the OCR error into the final explanation and completes the job.
The status is successfully committed as COMPLETED.
The frontend polling detects the COMPLETED status, stops loading, and displays the "Show Results" button. You can click it to view the analysis inline, which will persist even if you refresh the page.

### Phase 5: Reporting & Dashboard (Code Written, Not Tested)
- Implemented ReportService to generate PDF reports for screenshots and cases using OpenPDF
- Added ReportController for report download endpoints
- Implemented DashboardService to provide statistics for dashboard:
  - Total cases, screenshots analyzed, tampered detected, pending analyses
  - Verdict distribution (pie chart)
  - Uploads over time (last 7 days, line chart)
  - Cases by priority (bar chart)
  - Recent activity feed
- Added DTOs and mappers for dashboard data
- Updated repositories with custom queries for dashboard

### Phase 6-8: Frontend (React 18 + Vite)
- Created project structure with Vite
- Installed dependencies: axios, bootstrap, react-router-dom, recharts, @react-three/fiber, @react-three/drei, three, framer-motion
- Created basic routing and page components:
  - LandingPage: hero section with 3D placeholder
  - LoginPage: form with validation and error handling
  - RegisterPage: form with password strength validation
  - App.js: main routing component with protected routes
  - AuthContext: handles authentication state and API calls
  - api.js: axios instance with CSRF token handling
- Pages for dashboard, cases, upload, analysis results, profile, admin (stubs created)

## Next Steps to Complete the Application

### Backend
1. Replace placeholder password hashes in `data.sql` with actual BCrypt hashes:
   - Use `new BCryptPasswordEncoder().encode("yourPassword")` to generate hashes for:
     - Admin: Admin@123
     - Investigator: Invest@123
     - Viewer: Viewer@123
2. Install Maven (if not installed) and run the backend:
   ```powershell
   cd dfsa/backend
   $env:JAVA_HOME="C:\Program Files\Java\jdk-25.0.2" ; .\mvnw.cmd spring-boot:run
   ```
3. The backend will start on port 8080. Swagger UI will be available at http://localhost:8080/swagger-ui.html

### Frontend
1. Install Node.js dependencies:
   ```bash
   cd dfsa/frontend
   npm install
   ```
2. Start the frontend development server:
   ```bash
   npm run dev
   ```
3. The frontend will be available at http://localhost:5173
4. Complete the frontend components:
   - Implement actual 3D scenes using three.js and @react-three/fiber
   - Create detailed pages for dashboard, case management, upload, analysis results
   - Implement forms for case creation, sharing, notes
   - Add charts using recharts
   - Implement PDF report download functionality

   - Add responsive design and dark/light mode toggle
   - Implement 3D layer view for screenshot analysis results
   - Add toast notifications and skeleton loaders

### Testing
1. Register a new user (default role: INVESTIGATOR)
2. Log in and create a case
3. Upload a screenshot (PNG/JPG)
4. Trigger analysis and wait for completion
5. View analysis results, authenticity score, and extracted evidence
6. Download PDF report
7. Verify audit logs and chain of custody
8. Test role-based access (VIEWER cannot upload/delete, ADMIN can manage users)

## Important Notes
- The application uses HTTP SESSION authentication (JSESSIONID cookie), not JWT
- CSRF protection is enabled; the frontend handles the X-XSRF-TOKEN header
- File uploads are limited to 10 MB and validated for MIME type and magic bytes
- Screenshots are stored outside the web root in the directory specified by `screenshot.upload.dir` (defaults to user home dfsa-uploads)
- Passwords are stored as BCrypt hashes
- All actions are logged to the audit log table
- The forensic analysis runs asynchronously; status can be polled via the analysis endpoint

## Credentials for Testing
- Admin: admin / **password**
- Investigator: investigator1 / **password**
- Viewer: viewer1 / **password**

> The BCrypt hash used in data.sql maps to the literal password `password`.
> New users registered through the UI can use any password (min 8 chars).

## Session 2 Bug Fixes (Oct 2026)

### Backend Compilation Fixes
- **AuditLogRepository**: Fixed method outside interface brace; added `findByEntityIdAndEntityType`
- **ScreenshotRepository**: Removed duplicate `findByStatusIn` method
- **AuditLogMapper**: Fixed `toEntity()` calling non-existent `setUsername()`/`setDetails()` — now uses `setDescription()` and skips user (must be set by repo lookup)
- **AuditLogDTO**: Fixed constructor to use `auditLog.getUser().getUsername()` and `auditLog.getDescription()`
- **CaseMapper**: Removed `static` keyword from `toDTO()`/`toEntity()` — was a `@Component` but had static methods, causing invalid method reference in `CaseService`
- **ReportService**: Fixed `BaseColor.BLACK` (iText API) → `java.awt.Color.BLACK` (OpenPDF API); fixed `getEvidenceValue()` → `getRawText()` with fallback chain
- **ScreenshotService**: Added `NoSuchAlgorithmException` to throws clause for hashing methods
- **CaseController / ScreenshotController**: Added `NoSuchAlgorithmException` to catch blocks
- **AdminService**: Rewrote with correct imports; removed references to non-existent `AuditLogMapper` class in method signature
- **AdminController**: Created new — exposes `/api/admin/users` (GET, enable/disable, role change, delete) and `/api/admin/audit-logs`
- **AuthController**: Added `/api/auth/logout` and `/api/auth/change-password` endpoints
- **SecurityConfig**: Added `/api/auth/change-password` to CSRF ignore list
- **data.sql**: Fixed BCrypt hashes to valid hash for password `password`

### Maven Setup
- Extracted `apache-maven-3.9.6-bin.zip` to `./maven/apache-maven-3.9.6/`
- Run backend with: `$env:PATH = "C:\Users\HP\Desktop\ScreenShot_Detector\maven\apache-maven-3.9.6\bin;" + $env:PATH; mvn spring-boot:run`

