# Manual membership payments

Premium and Elite display the client-supplied QR. The original image is retained; CSS shows the QR area and the original remains downloadable. Its decoded UPI recipient is `pnarayanamurthy3@ybl`. Prices come from the configured plan, not the QR.

After registration, a new pending membership snapshots its plan price and receives a `PAY-` tracking ID. The member dashboard accepts the UPI transaction reference. Pending members can return using their registered phone number and password.

Submission sets MANUAL_REVIEW. Admin opens the application, checks the reference and exact amount against the receiving bank/UPI account, and confirms receipt or marks it not received. Only VERIFIED payments permit approval for newly tracked memberships. Failed references can be corrected. References are normalized and unique across memberships; a stale admin reference cannot be verified. A static QR provides no automatic payment confirmation.

Legacy memberships have no payment tracking ID and retain their previous approval behavior. Admin-created active founding memberships remain supported. Existing pending plans cannot be changed through repeated enrollment; contact admin rather than creating another payment request.

Database changes are additive. Render uses Hibernate schema update; environments using schema validation must apply `membership-payments.sql` first. No table reset is needed. Member deletion also deletes the membership and its payment fields under the existing deletion policy.
