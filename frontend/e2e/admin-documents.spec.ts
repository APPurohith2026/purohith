import {test,expect} from '@playwright/test';
import path from 'node:path';

test('admin details displays authenticated Aadhaar image, PDF and photo',async({page})=>{
  await page.addInitScript(()=>{localStorage.setItem('token','test-admin');localStorage.setItem('role','ADMIN')});
  await page.route('**/api/admin/applications/1',route=>route.fulfill({json:{
    id:1,fullNameEnglish:'Document Test Member',memberNumber:'AP-EGD-000001',applicationStatus:'PENDING_REVIEW',
    aadhaarFrontPath:'private/front.png',aadhaarBackPath:'private/back.pdf',passportPhotoPath:'private/photo.png'
  }}));
  let photoRequests=0;
  await page.route('**/api/admin/members/1/**',async route=>{
    expect(route.request().headers().authorization).toBe('Bearer test-admin');
    if(route.request().url().endsWith('aadhaar-back')){
      await route.fulfill({contentType:'application/pdf',body:'%PDF-1.4\n%%EOF'});return;
    }
    if(route.request().url().endsWith('passport-photo')&&++photoRequests===1){await route.fulfill({status:404});return}
    await route.fulfill({path:path.join(process.cwd(),'e2e/fixtures/test-photo.png'),contentType:'image/png'});
  });
  await page.goto('/admin/applications/1');
  await expect(page.getByRole('heading',{name:'Document Test Member'})).toBeVisible();
  await expect(page.getByAltText('Aadhaar Front',{exact:true})).toBeVisible();
  await expect(page.getByTitle('Aadhaar Back preview')).toBeVisible();
  await expect(page.getByRole('link',{name:'Open Aadhaar Back'})).toHaveAttribute('href',/^blob:/);
  await expect(page.getByRole('alert')).toContainText('Passport Photo could not be loaded');
  await page.getByRole('button',{name:'Retry Passport Photo'}).click();
  await expect(page.getByAltText('Passport Photo',{exact:true})).toBeVisible();
});

test('missing uploaded documents are clearly identified',async({page})=>{
  await page.addInitScript(()=>{localStorage.setItem('token','test-admin');localStorage.setItem('role','ADMIN')});
  await page.route('**/api/admin/applications/2',route=>route.fulfill({json:{id:2,fullNameEnglish:'No Documents',applicationStatus:'PENDING_REVIEW'}}));
  await page.goto('/admin/applications/2');
  await expect(page.getByText('Not uploaded',{exact:true})).toHaveCount(3);
});
