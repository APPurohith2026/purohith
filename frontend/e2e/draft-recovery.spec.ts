import {fillStep2} from './helpers';
import {test,expect} from '@playwright/test';
const key='purohit_registration_draft_token';
test.beforeEach(async({page})=>{
 await page.addInitScript(()=>{if(!sessionStorage.getItem('draft-test-seeded')){localStorage.setItem('purohit_registration_draft_token','saved-draft');sessionStorage.setItem('draft-test-seeded','yes')}});
 await page.route('**/api/public/plans',r=>r.fulfill({json:[]}));
});
test('temporary draft load failure retains the token and retries at saved step',async({page})=>{
 let unavailable=true;
 await page.route('**/api/registration/drafts/saved-draft',r=>{return unavailable?r.fulfill({status:503,json:{message:'Unavailable'}}):r.fulfill({json:{draftToken:'saved-draft',lastCompletedStep:2,fullName:'Saved member',phoneNumber:'9000000001',state:'Andhra Pradesh',district:'East Godavari'}})});
 await page.goto('/register');
 await expect(page.getByRole('button',{name:'Retry loading draft'})).toBeVisible();
 expect(await page.evaluate(k=>localStorage.getItem(k),key)).toBe('saved-draft');
 unavailable=false;
 await page.getByRole('button',{name:'Retry loading draft'}).click();
 await expect(page.getByTestId('upload-01')).toBeVisible();
 expect(await page.evaluate(k=>localStorage.getItem(k),key)).toBe('saved-draft');
});
test('confirmed unavailable draft clears stale token',async({page})=>{
 await page.route('**/api/registration/drafts/saved-draft',r=>r.fulfill({status:400,json:{message:'Registration draft not found or already submitted'}}));
 await page.goto('/register');
 await expect(page.getByTestId('full-name')).toBeVisible();
 expect(await page.evaluate(k=>localStorage.getItem(k),key)).toBeNull();
});

test('phone and password resume saved draft from a fresh browser',async({page})=>{
 await page.route('**/api/auth/login',r=>r.fulfill({status:400,json:{message:'Not yet registered'}}));
 await page.route('**/api/registration/drafts/resume',async r=>{
  expect(r.request().postDataJSON()).toEqual({identifier:'9000000001',password:'DraftPassword123!'});
  await r.fulfill({json:{draftToken:'recovered-draft'}});
 });
 await page.route('**/api/registration/drafts/recovered-draft',r=>r.fulfill({json:{draftToken:'recovered-draft',lastCompletedStep:2,draftLoginReady:true,fullName:'Saved Member',phoneNumber:'9000000001'}}));
 await page.goto('/login');await page.evaluate(()=>localStorage.clear());
 await page.getByTestId('login-identifier').fill('9000000001');await page.getByTestId('login-password').fill('DraftPassword123!');
 await page.getByTestId('login-submit').click();
 await expect(page).toHaveURL(/register/);await expect(page.getByTestId('upload-01')).toBeVisible();
 expect(await page.evaluate(k=>localStorage.getItem(k),key)).toBe('recovered-draft');
 await page.reload();await expect(page.getByTestId('upload-01')).toBeVisible();
});
test('legacy saved draft returns to step two to establish a password',async({page})=>{
 await page.route('**/api/registration/drafts/saved-draft',r=>r.fulfill({json:{draftToken:'saved-draft',lastCompletedStep:2,draftLoginReady:false,phoneNumber:'9000000001'}}));
 await page.goto('/register');await expect(page.getByTestId('password')).toBeVisible();await expect(page.getByTestId('phone-number')).toHaveValue('9000000001');
});

test('step two saves the password before leaving registration',async({page})=>{
 await page.route('**/api/registration/drafts/saved-draft',r=>r.fulfill({json:{draftToken:'saved-draft',lastCompletedStep:1,draftLoginReady:false}}));
 let saved=false;
 await page.route('**/api/registration/drafts/saved-draft/step/2',async r=>{
  expect(r.request().postDataJSON().password).toBe('Test@12345');saved=true;
  await r.fulfill({json:{draftToken:'saved-draft',lastCompletedStep:2,draftLoginReady:true}});
 });
 await page.goto('/register');await fillStep2(page,'9000000001');await page.getByTestId('save-exit').click();
 await expect(page).toHaveURL('/');expect(saved).toBe(true);
});
