import {test,expect} from '@playwright/test';

test('registration omits Pravara and submits with the six remaining declarations',async({page})=>{
  const draft={draftToken:'test-draft',lastCompletedStep:4,fullName:'Test Member',state:'Andhra Pradesh',district:'East Godavari',planCode:'PREMIUM'};
  await page.addInitScript(()=>localStorage.setItem('purohit_registration_draft_token','test-draft'));
  await page.route('**/api/public/plans',route=>route.fulfill({json:[]}));
  await page.route('**/api/registration/drafts/test-draft',route=>route.fulfill({json:draft}));
  let submitted=false;
  await page.route('**/api/registration/drafts/test-draft/confirm',async route=>{
    expect(route.request().postDataJSON()).toEqual({password:'TestPassword123!',declarationAccepted:true});
    submitted=true;
    await route.fulfill({status:400,json:{message:'Test submission received'}});
  });
  await page.goto('/register');
  await expect(page.locator('.declaration-item')).toHaveCount(6);
  await expect(page.locator('.declaration-panel')).not.toContainText('అఖిల భారత');
  await expect(page.locator('.review-grid')).not.toContainText('ప్రవర');
  await page.getByTestId('password').fill('TestPassword123!');
  await page.getByTestId('confirm-password').fill('TestPassword123!');
  for(let i=1;i<=6;i++)await page.getByTestId(`declaration-${i}`).check();
  await page.getByTestId('confirm-submit').click();
  await expect(page.getByText('Test submission received',{exact:true})).toBeVisible();
  expect(submitted).toBe(true);
  await page.goto('/register');
  await page.locator('.review-card').first().getByRole('button').click();
  await expect(page.getByTestId('full-name')).toBeVisible();
  await expect(page.getByTestId('pravara')).toHaveCount(0);
});
