# Purohit Community Platform — V7 Admin Console

Telugu-first React + Spring Boot membership platform for Andhra Pradesh / Telangana Purohit Community.

## V7 changes

### Registration / referral logic
- Registration reference is **optional**.
- User chooses one of:
  - `DIRECT` — no Member Reference ID required.
  - `MEMBER_REFERENCE` — reference Member ID is mandatory and backend validates that it belongs to an `APPROVED` member with `ACTIVE` membership.
- Admin can create legitimate founding/existing members with `ADMIN_CREATED` source so the system can be bootstrapped before any public Member IDs exist.
- Existing V6 draft-save wizard remains: each step is persisted in MySQL and can be resumed.
- Generated Member ID format remains `<STATE>-<DISTRICT>-<GENERATED_ID>` such as `TS-HYD-000125`.

### Admin console
Routes:
- `/admin` — dashboard + live statistics + recent applications
- `/admin/applications` — application list/search/filter
- `/admin/applications/:id` — full application review + approve/correction/reject
- `/admin/members` — approved-member list + founding/existing member creation
- `/admin/activities` — activity monitoring
- `/admin/id-cards`, `/admin/reports`, `/admin/settings` — reserved navigation for next modules

Dashboard statistics are calculated from the database, not hard-coded:
- Total applications
- Pending review
- Approved
- Correction required
- Rejected
- Active members
- Premium members
- Elite members
- Direct registrations
- Referred registrations
- Saved registration drafts
- Published activities
- Total activity enrollments

### Admin APIs
- `GET /api/admin/dashboard`
- `GET /api/admin/stats`
- `GET /api/admin/applications?status=PENDING_REVIEW`
- `GET /api/admin/applications/{id}`
- `PATCH /api/admin/members/{id}/approve`
- `PATCH /api/admin/members/{id}/correction`
- `PATCH /api/admin/members/{id}/reject`
- `GET /api/admin/members`
- `GET /api/admin/members/{id}`
- `POST /api/admin/members/founding`
- `GET /api/admin/activities`
- existing admin activity create/update/delete endpoints remain available

### Reference validation API
- `GET /api/registration/reference/{memberNumber}`

## Local run

### 1. Start MySQL
```powershell
docker compose up -d mysql
```

For a clean development reset after upgrading from V6 (deletes only the local Docker DB volume):
```powershell
docker compose down -v
docker compose up -d mysql
```

### 2. Start backend
```powershell
cd backend
$env:SPRING_PROFILES_ACTIVE="local"
mvn spring-boot:run
```

Backend: `http://localhost:8080`

### 3. Start frontend
```powershell
cd frontend
npm install
npm run dev
```

Frontend: `http://localhost:5173`

## Local admin
See `backend/src/main/resources/application-local.yml` / environment settings for the local admin credentials. Do not reuse development credentials in production.

## Production DB
Local profile uses MySQL. Production profile remains PostgreSQL-ready. Before production deployment, replace Hibernate schema updates with versioned Flyway migrations and use environment-managed secrets.

## Design references
The four supplied admin reference screens are stored under:
`docs/admin-reference/`

---

# V8 — Automated Regression / Playwright Test Pack

V8 adds repeatable automated verification on top of the V7 Admin Dashboard build.

## What is included

- Playwright browser automation (`frontend/e2e`)
- Desktop Chromium + mobile Chromium projects
- Full critical member journey test: registration → generated Member ID → Admin approval → Member-ID login → Digital ID verification
- Draft Save & Resume browser test
- Public-page / console-error smoke test
- Screenshots, video and Playwright trace retained on failure
- Playwright HTML report
- Stable `data-testid` selectors on important registration/login/admin controls
- JUnit 5 Member ID generation tests
- Testcontainers MySQL integration smoke test (auto-skips when Docker is unavailable)
- Root `verify-project.ps1` one-command Windows verification
- `run-e2e.ps1` convenience command for an already-prepared machine

## One-time Windows setup

From `frontend`:

```powershell
npm install
npx playwright install chromium
```

Java 21, Maven, Docker Desktop and Node/npm must be available in PATH.

## Run everything

From the project root:

```powershell
.\verify-project.ps1
```

The script:

1. starts the local MySQL Docker container,
2. runs Maven/JUnit/Testcontainers tests,
3. installs frontend dependencies when needed,
4. builds the React application,
5. starts Spring Boot and Vite,
6. runs Playwright on desktop and mobile Chromium,
7. stops temporary frontend/backend processes,
8. leaves the Playwright HTML report under `frontend/playwright-report/`.

Useful commands:

```powershell
cd frontend
npm run test:e2e
npm run test:e2e:critical
npm run test:e2e:headed
npm run test:e2e:ui
npm run test:e2e:report
```

## Regression scenarios currently automated

### Smoke
- Home loads.
- Registration page loads.
- Login page loads.
- Browser console is checked for errors.

### Draft persistence
- Personal details are saved.
- Address details are saved.
- Save & Exit is used.
- Registration is reopened.
- The wizard resumes at the next incomplete step.
- Previously stored address/phone/state/district values are reloaded.

### Critical member journey
- Direct registration is completed through all 5 steps.
- Aadhaar front/back and passport-photo fixtures are uploaded.
- Elite plan is selected.
- All seven declarations are accepted.
- Generated state/district Member ID is captured.
- Pending status is verified.
- Admin logs in and approves that exact application.
- Admin statistics are checked.
- Member logs in using the generated Member ID and password.
- Approved status and generated Digital ID card are verified.

### Backend
- State/district Member ID format is unit tested.
- Invalid district is rejected.
- Medchal dash variants map to the same district code.
- Spring/JPA repository startup is checked against a disposable real MySQL Testcontainer.

## Test artifacts

On a Playwright failure inspect:

```text
frontend/test-results/playwright/
frontend/playwright-report/index.html
```

Failed tests can contain screenshots, video and trace information. To open the report:

```powershell
cd frontend
npm run test:e2e:report
```
