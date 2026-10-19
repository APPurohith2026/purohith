# Deploy to Render and Vercel

Repository: https://github.com/APPurohith2026/purohith

## 1. Create accounts

Create Render and Vercel accounts and connect GitHub as **APPurohith2026**.
Grant both services access to this repository. Keep passwords and database
credentials in hosting environment settings, never in GitHub or chat.

## 2. Create the frontend project on Vercel

1. Open https://vercel.com/new and import `APPurohith2026/purohith`.
2. Set **Root Directory** to `frontend` and **Framework Preset** to Vite.
3. Build command: `npm run build`; output directory: `dist`.
4. Deploy to reserve your production URL. At this stage API operations will not
   work until you complete step 4 below.
5. Copy the production origin, for example `https://your-project.vercel.app`.
   Use your actual assigned URL, with no trailing slash or path.

`frontend/vercel.json` makes direct links and refreshes on routes such as
`/admin` work. Never put database passwords, admin passwords, or JWT secrets
in `VITE_*` variables: frontend variables are public.

## 3. Create the backend and PostgreSQL on Render

**Cost:** `render.yaml` requests a paid `0.5c-512mb` web service, a paid
`0.1c-256mb` PostgreSQL 17 database with 1 GB storage, and a 1 GB uploads disk. Review the
current estimate in Render before approving creation. No resources are
created merely by pushing this file to GitHub.

1. Open https://dashboard.render.com/ and choose **New > Blueprint**.
2. Connect this GitHub repository, select branch `main`, and use `render.yaml`.
3. Enter the values Render prompts for:

   | Variable | Value |
   | --- | --- |
   | `APP_ADMIN_PASSWORD` | A new, strong application admin password |
   | `CORS_ALLOWED_ORIGINS` | Your Vercel production origin from step 2 |

4. Review the resources and charges, then create the Blueprint.
5. Wait for PostgreSQL and the API service to finish deploying.
6. Copy the API service's assigned URL, for example
   `https://your-api.onrender.com`.
7. Open `<API URL>/actuator/health` and check for `{"status":"UP"}`.

The Blueprint automatically wires private PostgreSQL host, port, database,
username, and password into Spring's JDBC configuration. It generates a JWT
secret, disables external database access, and uses the `render` profile.
Render sets `PORT`; Spring already reads it.

The first startup creates tables with Hibernate `update`, an admin with email
`apbrahmanapurohithsamakya@gmail.com`, and Premium/Elite plans. Subsequent
startups preserve existing accounts and plans. No sample activities are added.
Changing `APP_ADMIN_PASSWORD` later does not reset an existing admin's password.
The separate `prod` profile continues to validate an existing schema; do not
select it for this initial Blueprint deployment. Introduce versioned migrations
before complex changes to a database that contains member data.

Uploaded photos and identity documents live under `/var/data/private` on the
persistent disk. Do not remove that disk when redeploying. A free web service's
temporary filesystem is unsuitable for retaining these files. Configure backups
for both PostgreSQL and uploaded files before collecting real member documents.

## 4. Connect the frontend to the backend

1. In Vercel, open the project's **Settings > Environment Variables**.
2. Add `VITE_API_BASE_URL` for **Production**, with value
   `<API URL>/api`, for example `https://your-api.onrender.com/api`.
3. Redeploy the Vercel production deployment. Vite embeds this value at build time.
4. In Render, confirm `CORS_ALLOWED_ORIGINS` exactly matches the Vercel production
   origin. Save and redeploy if you changed it.

For additional domains, put their exact origins in Render's CORS variable,
separated by commas. Vercel preview deployments have different origins and are
not automatically allowed to access the production API.

## 5. Verify the live application

- Open the home page and confirm membership plans load.
- Open `/login`, log in with the admin email and the application password you
  entered in Render, and check the admin dashboard.
- Refresh `/admin` directly to confirm Vercel routing works.
- Complete a test registration including document uploads, then approve it.
- Redeploy the backend and confirm the test account and its photo remain available.

After GitHub integration, pushes to `main` can trigger redeployments on both
services. Database records and uploads stay on their persistent resources;
deploying source code does not transfer your local MySQL data.

## Local verification

Run `mvn test` from `backend` and `npm run build` from `frontend`.
With Docker running, `PostgresContainerSmokeTest` exercises the actual Render
profile against PostgreSQL 17, including schema creation, Telugu plan names,
admin password hashing, and repeated initialization. Container tests skip when
Docker is unavailable. The Docker image itself must also build successfully
in Render before the deployment can be considered verified.

References: [Render Blueprints](https://render.com/docs/blueprint-spec),
[Render disks](https://render.com/docs/disks),
[Vite on Vercel](https://vercel.com/docs/frameworks/frontend/vite).
