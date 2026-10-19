import { test, expect } from '@playwright/test';
import path from 'node:path';

test('generates a photo ID card with the existing member ID and prints it', async ({ page }) => {
  const errors: string[] = [];
  page.on('pageerror', e => errors.push(e.message));
  await page.addInitScript(() => {
    localStorage.setItem('token', 'test-admin'); localStorage.setItem('role', 'ADMIN');
    window.print = () => { document.body.dataset.printed = 'true'; };
  });
  await page.route('**/api/admin/members', route => route.fulfill({ json: [
    { id: 10, memberNumber: 'TS-HYD-000010', fullNameEnglish: 'Test Member', district: 'Hyderabad', state: 'Telangana', phoneNumber: '9000000010', bloodGroup: 'O+', gotram: 'Bharadwaja', planCode: 'PREMIUM', membershipStatus: 'ACTIVE', applicationStatus: 'APPROVED', expiryDate: '2027-10-11' },
    { id: 11, memberNumber: 'TS-HYD-000011', fullNameEnglish: 'Pending Member', applicationStatus: 'PENDING_REVIEW' },
    { id: 12, memberNumber: 'TS-HYD-000012', fullNameEnglish: 'No Photo Member', applicationStatus: 'APPROVED' },
  ] }));
  await page.route('**/api/admin/members/10/passport-photo', async route => {
    expect(route.request().headers().authorization).toBe('Bearer test-admin');
    await route.fulfill({ path: path.join(process.cwd(), 'e2e/fixtures/test-photo.png'), contentType: 'image/png' });
  });
  await page.route('**/api/admin/members/12/passport-photo', route => route.fulfill({ status: 404 }));
  await page.goto('/admin/id-cards');
  await expect(page.getByRole('button', { name: /Pending Member/ })).toHaveCount(0);
  await page.getByRole('button', { name: /Test Member/ }).click();
  const card = page.getByRole('article', { name: 'Member ID card' });
  await expect(card).toContainText('TS-HYD-000010');
  await expect(card).toContainText('Bharadwaja');
  await expect(card.getByAltText('Member passport photo')).toBeVisible();
  await expect(page.getByRole('button', { name: 'Print / Save as PDF' })).toBeEnabled();
  await page.getByRole('button', { name: 'Print / Save as PDF' }).click();
  await expect(page.locator('body')).toHaveAttribute('data-printed', 'true');
  await page.getByRole('button', { name: /No Photo Member/ }).click();
  await expect(page.getByRole('alert')).toContainText('Passport photo unavailable');
  await expect(page.getByRole('button', { name: 'Print / Save as PDF' })).toBeDisabled();
  await page.getByRole('textbox', { name: 'Search members' }).fill('missing-member');
  await expect(page.getByText('No approved members found.')).toBeVisible();
  expect(errors).toEqual([]);
});
