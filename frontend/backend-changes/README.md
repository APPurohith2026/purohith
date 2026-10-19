The backend stores passport photos in a private filesystem folder. The frontend now requests those photos through authenticated endpoints rather than exposing filesystem paths.

Copy `MemberPhotoController.java` into:
`../backend/src/main/java/com/purohitcommunity/portal/web/MemberPhotoController.java`

Restart the backend. Its existing security configuration restricts `/api/admin/**` to administrators and `/api/member/**` to members. The controller serves only the selected member's passport photo; member requests derive the member from the authenticated account. It rejects paths outside the configured private storage directory, including symlinks outside that directory.

Endpoints:
- `GET /api/admin/members/{id}/passport-photo`
- `GET /api/member/passport-photo`

No database migration is needed. Existing uploaded photos are reused. Missing photos return 404; card printing stays disabled until a valid photo loads.

The backend source is staged here because the current workspace only permits writes to the frontend folder. The backend controller has not been compiled or integration-tested in this session.
