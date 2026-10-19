import { test, expect } from '@playwright/test';

test('public pages and backend plan API load without browser console errors', async ({ page, request }) => {
  const consoleErrors: string[] = [];
  const badResponses: string[] = [];

  page.on('console', m => {
    if (m.type() === 'error') consoleErrors.push(m.text());
  });
  page.on('response', r => {
    if (r.status() >= 400 && !r.url().endsWith('/favicon.ico')) {
      badResponses.push(`${r.status()} ${r.url()}`);
    }
  });

  const plans = await request.get('http://localhost:8080/api/public/plans');
  expect(plans.ok(), `Plans API failed: ${plans.status()}`).toBeTruthy();
  const data = await plans.json();
  expect(Array.isArray(data)).toBeTruthy();
  expect(data.length).toBeGreaterThanOrEqual(2);

  await page.goto('/');
  await expect(page).toHaveTitle(/Purohit|పురోహిత/i);

  await page.goto('/register');
  await expect(page.getByRole('heading', { name: 'సభ్యత్వ దరఖాస్తు' })).toBeVisible();

  await page.goto('/login');
  await expect(page.getByRole('heading', { name: 'లాగిన్' })).toBeVisible();

  const meaningfulErrors = consoleErrors.filter(x =>
    !x.includes('Download the React DevTools') &&
    !x.includes('favicon.ico') &&
    !x.includes('Failed to load resource: the server responded with a status of 404')
  );
  expect(badResponses, `HTTP failures: ${badResponses.join(', ')}`).toEqual([]);
  expect(meaningfulErrors, `Console errors: ${meaningfulErrors.join(' | ')}`).toEqual([]);
});
