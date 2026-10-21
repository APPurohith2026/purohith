# Registration draft login

Step 1 saves personal details with a random browser draft token. Saving step 1 now requires a password for a new draft. The password is hashed with the existing password encoder; raw passwords are never returned or stored in browser storage. An unfinished draft reserves its normalized login phone so another draft cannot claim it.

Members return through Login with their phone number and password. If no completed account can authenticate, the frontend attempts draft authentication and opens the saved next incomplete step. Five incorrect draft password attempts lock draft login for 15 minutes. Row locking protects the failure counter. A browser draft token remains a secret recovery credential; temporary API failures preserve it and show Retry.

Older drafts without a password can continue from their original browser; they return to step 1 to set one. Existing drafts do not have a previously generated member ID or password to recover. If the original browser token was lost before this feature, admin assistance is required; do not expose a draft based on phone number alone.

Final submission creates the member account using the saved password hash and generates the permanent member ID. The submitted draft can no longer log in. Members use phone + password until approval, then may also use their member ID. Payment verification remains required for newly tracked memberships.

Render schema update adds the new draft fields automatically. For schema-validation environments apply draft-login.sql first. No table deletion is required.
