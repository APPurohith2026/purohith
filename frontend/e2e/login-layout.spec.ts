import {test,expect} from '@playwright/test';
test('login keeps its compact form and does not render registration steps',async({page})=>{
 await page.goto('/login');
 await expect(page.getByTestId('login-identifier')).toBeVisible();await expect(page.getByTestId('login-password')).toBeVisible();
 await expect(page.locator('.wizard-page,.wizard-rail')).toHaveCount(0);
 await expect(page.getByText('Started registration earlier?',{exact:false})).toHaveCount(0);
 await expect(page.getByText('After saving step 1',{exact:false})).toHaveCount(0);
 await expect(page.locator('footer')).toBeVisible();
 expect(await page.evaluate(()=>document.documentElement.scrollWidth<=innerWidth)).toBeTruthy();
});
