import {test,expect} from '@playwright/test';

for(const section of ['members','applications']){
  test(`${section}: cancel, failure and successful admin deletion`,async({page})=>{
    await page.addInitScript(()=>{
      localStorage.setItem('token','test-admin-token');
      localStorage.setItem('role','ADMIN');
      localStorage.setItem('loginId','test-admin');
    });
    await page.route(`**/api/admin/${section}`,route=>route.fulfill({json:[{
      id:1,memberNumber:'TS-HYD-000001',fullNameEnglish:'Delete Test Member',
      applicationStatus:'APPROVED',membershipStatus:'ACTIVE',planCode:'PREMIUM'
    }]}));
    let calls=0;
    await page.route('**/api/admin/members/1',route=>{
      calls++;
      return calls===1?route.fulfill({status:500,json:{message:'Deletion failed'}}):route.fulfill({status:204});
    });
    await page.goto(`/admin/${section}`);
    const remove=page.getByRole('button',{name:'Delete',exact:true});
    page.once('dialog',dialog=>dialog.dismiss());
    await remove.click();
    await expect(page.getByText('Delete Test Member',{exact:true})).toBeVisible();
    expect(calls).toBe(0);
    page.once('dialog',dialog=>dialog.accept());
    await remove.click();
    await expect(page.getByRole('alert')).toHaveText('Deletion failed');
    await expect(remove).toBeEnabled();
    page.once('dialog',dialog=>dialog.accept());
    await remove.click();
    await expect(page.locator('tbody tr')).toHaveCount(0);
    expect(calls).toBe(2);
  });
}
