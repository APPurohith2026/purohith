import { test, expect } from '@playwright/test';
import path from 'node:path';
import { fillStep1, fillStep2, next, uniqueDigits } from './helpers';

const PASSWORD = 'Test@12345';
const API = 'http://localhost:8080/api';
const fixture = path.join(
  process.cwd(),
  'e2e',
  'fixtures',
  'test-photo.png'
);

test(
  'mission: register, approve, and login with generated Member ID',
  async ({ page, request }) => {
    const phone = uniqueDigits(10);
    const aadhaar = uniqueDigits(12);

    // ------------------------------------------------------------
    // 1. START REGISTRATION
    // ------------------------------------------------------------

    await page.goto('/register');
    await page.evaluate(() => localStorage.clear());
    await page.reload();

    // ------------------------------------------------------------
    // 2. STEP 1 - PERSONAL DETAILS
    // ------------------------------------------------------------

    await fillStep1(page, aadhaar);
    await next(page);

    // ------------------------------------------------------------
    // 3. STEP 2 - ADDRESS / CONTACT
    // ------------------------------------------------------------

    await fillStep2(page, phone);
    await next(page);

    // ------------------------------------------------------------
    // 4. STEP 3 - DOCUMENTS
    // ------------------------------------------------------------

    await page.getByTestId('upload-01').setInputFiles(fixture);
    await page.getByTestId('upload-02').setInputFiles(fixture);
    await page.getByTestId('upload-03').setInputFiles(fixture);

    await next(page);

    // ------------------------------------------------------------
    // 5. STEP 4 - MEMBERSHIP
    // ------------------------------------------------------------

    await page.getByTestId('plan-elite').check();

    await next(page);

    // ------------------------------------------------------------
    // 6. STEP 5 - PASSWORD + DECLARATION
    // ------------------------------------------------------------

    for (let i = 1; i <= 6; i++) {
      await page
        .getByTestId(`declaration-${i}`)
        .check();
    }

    // ------------------------------------------------------------
    // 7. CONFIRM REGISTRATION
    // ------------------------------------------------------------

    const confirmResponsePromise = page.waitForResponse(
      response =>
        response
          .url()
          .includes('/api/registration/drafts/') &&
        response.url().endsWith('/confirm') &&
        response.request().method() === 'POST'
    );

    await page
      .getByTestId('confirm-submit')
      .click();

    const confirmResponse =
      await confirmResponsePromise;

    expect(
      confirmResponse.ok(),
      `Confirm failed: ${confirmResponse.status()} ${await confirmResponse.text()}`
    ).toBeTruthy();

    const confirmData =
      await confirmResponse.json();

    const memberId =
      confirmData.memberNumber as string;

    expect(memberId).toMatch(
      /^(AP|TS)-[A-Z0-9]+-\d{6}$/
    );

    // ------------------------------------------------------------
    // 8. MEMBER DASHBOARD AFTER REGISTRATION
    // ------------------------------------------------------------

    await page.waitForURL('**/dashboard');
    const paymentReference = `TEST${Date.now()}`;
    const submittedPayment = await request.post(`${API}/member/payment`, {
      headers: {Authorization: `Bearer ${confirmData.token}`},
      data: {reference: paymentReference}
    });
    expect(submittedPayment.ok(), await submittedPayment.text()).toBeTruthy();


    await expect(
      page.locator('.member-number-line')
    ).toContainText(memberId);

    await expect(
      page
        .getByText(
          'PENDING_REVIEW',
          { exact: true }
        )
        .first()
    ).toBeVisible();

    // ------------------------------------------------------------
    // 9. MEMBER-ID LOGIN MUST FAIL BEFORE APPROVAL
    // ------------------------------------------------------------

    const blockedLogin =
      await request.post(
        `${API}/auth/login`,
        {
          data: {
            identifier: memberId,
            password: PASSWORD
          },
          failOnStatusCode: false
        }
      );

    expect(
      blockedLogin.status()
    ).toBeGreaterThanOrEqual(400);

    // ------------------------------------------------------------
    // 10. ADMIN LOGIN
    // ------------------------------------------------------------

    const adminLogin =
      await request.post(
        `${API}/auth/login`,
        {
          data: {
            identifier:
              'admin@purohit.local',
            password:
              'Admin@12345'
          }
        }
      );

    expect(
      adminLogin.ok(),
      `Admin login failed: ${adminLogin.status()}`
    ).toBeTruthy();

    const adminAuth =
      await adminLogin.json();

    const adminToken =
      adminAuth.token as string;

    const adminHeaders = {
      Authorization:
        `Bearer ${adminToken}`
    };

    // ------------------------------------------------------------
    // 11. VERIFY APPLICATION EXISTS
    // ------------------------------------------------------------

    const apps =
      await request.get(
        `${API}/admin/applications`,
        {
          headers: adminHeaders
        }
      );

    expect(
      apps.ok(),
      `Applications API failed: ${apps.status()}`
    ).toBeTruthy();

    const rows =
      await apps.json();

    const member =
      rows.find(
        (x: any) =>
          x.memberNumber === memberId
      );

    expect(
      member,
      `Application ${memberId} not found`
    ).toBeTruthy();

    // ------------------------------------------------------------
    // 12. APPROVE MEMBER THROUGH ADMIN API
    // ------------------------------------------------------------

    const blockedApproval = await request.patch(`${API}/admin/members/${member.id}/approve`, {headers: adminHeaders});
    expect(blockedApproval.status()).toBe(400);
    const verifiedPayment = await request.patch(`${API}/admin/members/${member.id}/payment`, {
      headers: adminHeaders, data: {received: true, reference: paymentReference}
    });
    expect(verifiedPayment.ok(), await verifiedPayment.text()).toBeTruthy();

    const approve =
      await request.patch(
        `${API}/admin/members/${member.id}/approve`,
        {
          headers: adminHeaders,
          data: {
            comment:
              'Approved by Playwright automated test'
          }
        }
      );

    expect(
      approve.ok(),
      `Approval failed: ${approve.status()} ${await approve.text()}`
    ).toBeTruthy();

    const approved =
      await approve.json();

    expect(
      approved.applicationStatus
    ).toBe('APPROVED');

    expect(
      approved.membershipStatus
    ).toBe('ACTIVE');

    // ------------------------------------------------------------
    // 13. VERIFY ADMIN STATISTICS
    // ------------------------------------------------------------

    const statsResp =
      await request.get(
        `${API}/admin/stats`,
        {
          headers: adminHeaders
        }
      );

    expect(
      statsResp.ok(),
      `Stats API failed: ${statsResp.status()}`
    ).toBeTruthy();

    const stats =
      await statsResp.json();

    expect(
      stats.approvedMembers
    ).toBeGreaterThanOrEqual(1);

    expect(
      stats.eliteMembers
    ).toBeGreaterThanOrEqual(1);

    // ------------------------------------------------------------
    // 14. MEMBER-ID LOGIN MUST WORK AFTER APPROVAL
    // ------------------------------------------------------------

    const memberLogin =
      await request.post(
        `${API}/auth/login`,
        {
          data: {
            identifier: memberId,
            password: PASSWORD
          }
        }
      );

    expect(
      memberLogin.ok(),
      `Approved member login failed: ${memberLogin.status()}`
    ).toBeTruthy();

    const memberAuth =
      await memberLogin.json();

    // ------------------------------------------------------------
    // 15. LOAD MEMBER DASHBOARD USING APPROVED LOGIN
    // ------------------------------------------------------------

    await page.evaluate(
      ({ token, loginId }) => {
        localStorage.clear();

        localStorage.setItem(
          'token',
          token
        );

        localStorage.setItem(
          'role',
          'MEMBER'
        );

        localStorage.setItem(
          'loginId',
          loginId
        );
      },
      {
        token:
          memberAuth.token,
        loginId:
          memberAuth.loginId
      }
    );

    await page.goto('/dashboard');

    // ------------------------------------------------------------
    // 16. FINAL VERIFICATION
    // ------------------------------------------------------------

    await expect(
      page.locator(
        '.member-number-line'
      )
    ).toContainText(memberId);

    await expect(
      page
        .getByText(
          'APPROVED',
          { exact: true }
        )
        .first()
    ).toBeVisible();

    await expect(
      page.locator('.digital-card')
    ).toBeVisible();
  }
);