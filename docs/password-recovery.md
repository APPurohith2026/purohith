# Member password recovery

The login page links to `/forgot-password`. Members provide their Member ID and
the exact phone number used for registration (formatting spaces and dashes are
ignored). The server checks that both identify the same enabled MEMBER account.
Admins cannot reset their passwords through this flow. Approval requirements for
Member ID login are unchanged; pending members can log in using their phone.

1. `POST /api/auth/password-reset/request` with `memberId`, `phoneNumber`.
2. `POST /api/auth/password-reset/verify` with the returned `challengeId`, `otp`.
3. `POST /api/auth/password-reset/confirm` with `challengeId`, returned `resetToken`,
   and `newPassword`.
4. Log in using Member ID or registered phone with the new password.

OTPs expire after five minutes and allow five guesses. Verification consumes the
OTP and grants a single-use reset token valid for five minutes. Resending replaces
the old OTP; sending is limited to once per minute and five times per hour per
account. Hashes, expiry and attempt limits are stored in the database, with row
locks for concurrent requests. Neither OTPs nor reset tokens are logged or sent
back in the OTP request response. Reset tokens remain only in browser memory.
Unknown and mismatched accounts receive the same generic request response.

## SMS delivery setup required

There is currently no SMS provider account or approved OTP template. **Real SMS
delivery is not configured.** The feature returns an unavailable message until a
gateway is configured; there is no fixed test OTP or production bypass.

`GatewayOtpSmsSender` defines an integration contract, not a direct integration
with a named commercial SMS provider. Once the provider is selected, implement
its API behind `OtpSmsSender`, or provide a trusted HTTPS adapter supporting:

- Environment variable `APP_SMS_GATEWAY_URL`: HTTPS POST endpoint.
- Environment variable `APP_SMS_GATEWAY_TOKEN`: bearer credential, stored as a
  deployment secret.
- JSON body: `phoneNumber`, `otp`, `purpose` (`PASSWORD_RESET`),
  `expiresInSeconds` (`300`).
- HTTP 2xx only when the SMS has been accepted; errors otherwise.
- The adapter must normalize the stored number to the provider's required
  country-code format and use the provider's approved sender/template settings.
- Do not enable request-body logging for this endpoint or the SMS adapter.

The provider must be configured and tested with a controlled phone before a
client demonstration. Configure provider spending limits as well as edge request
rate limits for the public auth endpoints.

## Schema

Render's existing `ddl-auto: update` creates the additional user columns and
challenge index. Deployments using `ddl-auto: validate` must apply
`password-recovery-postgres.sql` first. No user data is removed by that script.

Password changes do not revoke previously issued JWT sessions in the existing
authentication system; those sessions retain their normal expiry.
