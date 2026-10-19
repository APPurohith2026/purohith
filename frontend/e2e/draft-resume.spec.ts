import { test, expect } from '@playwright/test';
import { fillStep1, fillStep2, next, uniqueDigits } from './helpers';

test('registration draft persists and resumes at the next incomplete step', async ({ page }) => {
  await page.goto('/register');
  await page.evaluate(() => localStorage.removeItem('purohit_registration_draft_token'));
  await page.reload();

  await fillStep1(page, uniqueDigits(12));
  await next(page);
  await expect(page.getByTestId('phone-number')).toBeVisible();

  const phone = uniqueDigits(10);
  await fillStep2(page, phone);

  await Promise.all([
    page.waitForURL('/', { timeout: 10_000 }),
    page.getByTestId('save-exit').click(),
  ]);

  const token = await page.evaluate(() => localStorage.getItem('purohit_registration_draft_token'));
  expect(token).toBeTruthy();

  await page.goto('/register');
  await expect(page.getByTestId('upload-01')).toBeVisible({ timeout: 10_000 });

  await page.getByRole('button', { name: /చిరునామా/ }).click();
  await expect(page.getByTestId('phone-number')).toHaveValue(phone);
  await expect(page.getByTestId('state')).toHaveValue('Telangana');
  await expect(page.getByTestId('district')).toHaveValue('Hyderabad');
});
