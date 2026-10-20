import {test,expect} from '@playwright/test';

test('member verifies OTP, sets matching password and returns to login',async({page})=>{
  await page.route('**/api/auth/password-reset/request',async route=>{
    expect(route.request().postDataJSON()).toEqual({memberId:'AP-EGD-000001',phoneNumber:'9000000001'});
    await route.fulfill({json:{challengeId:'challenge-test',message:'If the details match, an OTP will be sent.'}});
  });
  let verifies=0;
  await page.route('**/api/auth/password-reset/verify',async route=>{
    verifies++;
    if(verifies===1){await route.fulfill({status:400,json:{message:'Invalid or expired verification. Request a new OTP.'}});return}
    expect(route.request().postDataJSON()).toEqual({challengeId:'challenge-test',otp:'123456'});
    await route.fulfill({json:{resetToken:'reset-test'}});
  });
  let resets=0;
  await page.route('**/api/auth/password-reset/confirm',async route=>{
    resets++;
    expect(route.request().postDataJSON()).toEqual({challengeId:'challenge-test',resetToken:'reset-test',newPassword:'NewPassword123!'});
    await route.fulfill({json:{message:'Password updated. Log in with your new password.'}});
  });
  await page.goto('/login');
  await page.getByRole('link',{name:'Forgot password?'}).click();
  await page.getByLabel('Member ID',{exact:true}).fill('AP-EGD-000001');
  await page.getByLabel('Registered phone number').fill('9000000001');
  await page.getByRole('button',{name:'Send OTP',exact:true}).click();
  await expect(page.getByRole('button',{name:/Resend OTP in/})).toBeDisabled();
  await page.getByLabel('6-digit OTP').fill('000000');
  await page.getByRole('button',{name:'Verify OTP',exact:true}).click();
  await expect(page.getByRole('alert')).toContainText('Invalid or expired');
  await page.getByLabel('6-digit OTP').fill('123456');
  await page.getByRole('button',{name:'Verify OTP',exact:true}).click();
  await page.getByLabel('New password',{exact:true}).fill('NewPassword123!');
  await page.getByLabel('Confirm new password').fill('Mismatch123!');
  await page.getByRole('button',{name:'Save new password'}).click();
  await expect(page.getByRole('alert')).toHaveText('Passwords do not match.');
  expect(resets).toBe(0);
  await page.getByLabel('Confirm new password').fill('NewPassword123!');
  await page.getByRole('button',{name:'Save new password'}).click();
  await expect(page.getByRole('status')).toContainText('Password updated');
  await page.getByRole('link',{name:'Back to login'}).click();
  await expect(page.getByTestId('login-submit')).toBeVisible();
});

test('unconfigured SMS displays an error without advancing',async({page})=>{
  await page.route('**/api/auth/password-reset/request',route=>route.fulfill({status:400,json:{message:'SMS password recovery is not available yet. Please contact the administrator.'}}));
  await page.goto('/forgot-password');
  await page.getByLabel('Member ID',{exact:true}).fill('AP-EGD-000001');
  await page.getByLabel('Registered phone number').fill('9000000001');
  await page.getByRole('button',{name:'Send OTP',exact:true}).click();
  await expect(page.getByRole('alert')).toContainText('not available yet');
  await expect(page.getByRole('button',{name:'Send OTP',exact:true})).toBeEnabled();
  await expect(page.getByLabel('6-digit OTP')).toHaveCount(0);
});
