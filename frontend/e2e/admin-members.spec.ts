import { test, expect } from '@playwright/test';

test('admin members loads, filters, and unmounts without a React error', async ({ page }) => {
  const errors: string[] = [];
  page.on('pageerror', error => errors.push(error.message));
  await page.addInitScript(() => {
    localStorage.setItem('token', 'test-admin-token');
    localStorage.setItem('role', 'ADMIN');
    localStorage.setItem('loginId', 'test-admin');
  });
  await page.route('**/api/admin/members', route => route.fulfill({
    json: [
      { id: 1, memberNumber: 'TS-HYD-000001', fullNameEnglish: 'Approved Member', phoneNumber: '9000000001', district: 'Hyderabad', applicationStatus: 'APPROVED', membershipStatus: 'ACTIVE', planCode: 'PREMIUM' },
      { id: 2, memberNumber: 'TS-HYD-000002', fullNameEnglish: 'Pending Member', applicationStatus: 'PENDING_REVIEW' },
    ],
  }));
  await page.goto('/admin/members');
  await expect(page.getByRole('cell', { name: 'TS-HYD-000001', exact: true })).toBeVisible();
  await expect(page.getByText('Pending Member', { exact: true })).toHaveCount(0);
  await page.locator('.admin-search input').fill('no-match');
  await expect(page.locator('tbody tr')).toHaveCount(0);
  await page.locator('.admin-search input').fill('Hyderabad');
  await expect(page.locator('tbody tr')).toHaveCount(1);
  await page.locator('a[href="/admin/settings"]').click();
  await expect(page.getByRole('heading', { name: 'Settings', exact: true })).toBeVisible();
  await page.locator('a[href="/admin/members"]').click();
  await expect(page.getByRole('cell', { name: 'TS-HYD-000001', exact: true })).toBeVisible();
  expect(errors).toEqual([]);
});
