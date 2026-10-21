import {test,expect} from '@playwright/test';
import {fillStep1,fillStep2} from './helpers';

test.beforeEach(async({page})=>{
  await page.route('**/api/public/plans',route=>route.fulfill({json:[]}));
});

test('DOB keeps a four-digit year when filled or pasted and rejects future dates',async({page})=>{
  let requests=0;
  await page.route('**/api/registration/drafts/start',route=>{requests++;return route.fulfill({json:{draftToken:'valid-draft'}})});
  await page.goto('/register');
  await fillStep1(page,'123456789012');
  const date=page.getByTestId('date-of-birth');
  await date.focus();
  for(let i=0;i<3;i++)await date.press('ArrowLeft');
  await date.press('ArrowRight');await date.press('ArrowRight');
  await page.keyboard.type('1989');
  await expect(date).toHaveValue('1989-01-15');
  await date.fill('1990-01-15');
  await date.fill('19901-01-15');
  await expect(date).toHaveValue('1990-01-15');
  await expect(page.getByText('The year must contain exactly four digits.',{exact:true})).toBeVisible();
  await date.fill('123456-01-15');
  await expect(date).toHaveValue('1990-01-15');
  await date.fill('2999-01-01');
  await page.getByTestId('next-step').click();
  await expect(date).toHaveAttribute('aria-invalid','true');
  expect(requests).toBe(0);
  await date.fill('2000-02-29');
  await page.getByTestId('next-step').click();
  await expect(page.getByTestId('whatsapp-number')).toBeVisible();
  expect(requests).toBe(1);
});

test('blank names and invalid contact details are blocked before saving',async({page})=>{
  await page.route('**/api/registration/drafts/start',route=>route.fulfill({json:{draftToken:'valid-draft'}}));
  let savedAddress=false;
  await page.route('**/api/registration/drafts/valid-draft/step/2',route=>{savedAddress=true;return route.fulfill({json:{draftToken:'valid-draft'}})});
  await page.goto('/register');
  await fillStep1(page,'123456789012');
  await page.getByTestId('full-name').fill('   ');
  await page.getByTestId('next-step').click();
  await expect(page.getByTestId('full-name')).toHaveAttribute('aria-invalid','true');
  await page.getByTestId('full-name').fill('Valid Test Member');
  await page.getByTestId('next-step').click();
  await fillStep2(page,'12345');
  await page.getByTestId('whatsapp-number').fill('invalid');
  await page.getByTestId('pin-code').fill('000000');
  await page.getByTestId('next-step').click();
  await expect(page.getByTestId('phone-number')).toHaveAttribute('aria-invalid','true');
  await expect(page.getByTestId('whatsapp-number')).toHaveAttribute('aria-invalid','true');
  await expect(page.getByTestId('pin-code')).toHaveAttribute('aria-invalid','true');
  expect(savedAddress).toBe(false);
  await page.getByTestId('phone-number').fill('+91 90000-00001');
  await page.getByTestId('whatsapp-number').fill('9000000001');
  await page.getByTestId('pin-code').fill('500001');
  await page.getByTestId('next-step').click();
  await expect(page.getByTestId('upload-01')).toBeVisible();
  expect(savedAddress).toBe(true);
});

test('upload step blocks oversized documents and PDFs used as passport photos',async({page})=>{
  await page.addInitScript(()=>localStorage.setItem('purohit_registration_draft_token','upload-test'));
  await page.route('**/api/registration/drafts/upload-test',route=>route.fulfill({json:{draftToken:'upload-test',lastCompletedStep:2}}));
  let uploaded=false;
  await page.route('**/api/registration/drafts/upload-test/step/3',route=>{uploaded=true;return route.fulfill({json:{}})});
  await page.goto('/register');
  await page.getByTestId('upload-01').setInputFiles({name:'large.pdf',mimeType:'application/pdf',buffer:Buffer.alloc(5*1024*1024+1)});
  await page.getByTestId('upload-03').setInputFiles({name:'photo.pdf',mimeType:'application/pdf',buffer:Buffer.from('%PDF-1.4')});
  await expect(page.locator('#upload-01-error')).toContainText('5 MB');
  await expect(page.locator('#upload-03-error')).toContainText('JPG or PNG');
  await page.getByTestId('next-step').click();
  expect(uploaded).toBe(false);
});
