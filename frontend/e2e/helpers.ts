import { expect, Page } from '@playwright/test';

export function uniqueDigits(length = 10) {
  const raw = `${Date.now()}${Math.floor(Math.random() * 100000)}`.replace(/\D/g, '');
  return raw.slice(-length).padStart(length, '7');
}

export async function fillStep1(page: Page, aadhaar: string) {
  await page.getByTestId('source-direct').check();
  await page.getByTestId('full-name').fill('Playwright Test Member');
  await page.getByTestId('gender').selectOption('MALE');
  await page.getByTestId('profession').fill('Purohit');
  await page.getByTestId('gotram').fill('Bharadwaja');
  await page.getByTestId('date-of-birth').fill('1990-01-15');
  await page.getByTestId('aadhaar-number').fill(aadhaar);
  await page.getByTestId('blood-group').selectOption('O+');
}

export async function fillStep2(page: Page, phone: string) {
  await page.getByTestId('phone-number').fill(phone);
  await page.getByTestId('whatsapp-number').fill(phone);
  await page.getByTestId('door-number').fill('1-23');
  await page.getByTestId('city-town').fill('Hyderabad');
  await page.getByTestId('constituency').fill('Secunderabad');
  await page.getByTestId('state').selectOption('Telangana');
  await expect(page.getByTestId('district')).toBeEnabled();
  await page.getByTestId('district').selectOption('Hyderabad');
  await page.getByTestId('pin-code').fill('500001');
  if(await page.getByTestId('password').isVisible()){await page.getByTestId('password').fill('Test@12345');await page.getByTestId('confirm-password').fill('Test@12345')}
}

const nextStepReady: Record<number, string> = {
  1: 'phone-number',
  2: 'upload-01',
  3: 'plan-elite',
  4: 'confirm-submit',
};

export async function next(page: Page) {
  const currentText = await page.getByText(/దశ\s+[1-4]\s*\/\s*5/).first().textContent();
  const match = currentText?.match(/దశ\s+(\d+)\s*\/\s*5/);
  if (!match) throw new Error(`Unable to determine current registration step from: ${currentText}`);

  const current = Number(match[1]);
  const waitForSave = page.waitForResponse(r => {
    const u = r.url();
    const method = r.request().method();
    return u.includes('/api/registration/drafts/') && ['POST', 'PUT'].includes(method);
  }, { timeout: 15_000 });

  await page.getByTestId('next-step').click();
  const response = await waitForSave;
  expect(response.ok(), `Draft save failed: ${response.status()} ${response.url()}`).toBeTruthy();

  const readyId = nextStepReady[current];
  if (!readyId) throw new Error(`No next-step selector configured after step ${current}`);
  await expect(page.getByTestId(readyId)).toBeVisible();
}

export async function login(page: Page, identifier: string, password: string) {
  await page.goto('/login');
  await page.getByTestId('login-identifier').fill(identifier);
  await page.getByTestId('login-password').fill(password);
  await page.getByTestId('login-submit').click();
}
