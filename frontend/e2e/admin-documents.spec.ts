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
      await route.fulfill({path:path.join(process.cwd(),'e2e/fixtures/test-document.pdf'),contentType:'application/pdf'});return;
    }
    if(route.request().url().endsWith('passport-photo')&&++photoRequests===1){await route.fulfill({status:404});return}
    await route.fulfill({path:path.join(process.cwd(),'e2e/fixtures/test-photo.png'),contentType:'image/png'});
  });
  await page.goto('/admin/applications/1');
  await expect(page.getByRole('heading',{name:'Document Test Member'})).toBeVisible();
  await expect(page.getByAltText('Aadhaar Front',{exact:true})).toBeVisible();
  const back=page.getByRole('article',{name:'Aadhaar Back',exact:true});
  await expect(back.getByRole('img',{name:'Aadhaar Back PDF page 1'})).toBeVisible();
  await back.getByRole('button',{name:'Next',exact:true}).click();
  await expect(back.getByRole('img',{name:'Aadhaar Back PDF page 2'})).toBeVisible();
  await expect(page.getByRole('link',{name:'Open Aadhaar Back'})).toHaveAttribute('href',/^blob:/);
  await expect(page.getByRole('alert')).toContainText('Passport Photo could not be loaded');
  await page.getByRole('button',{name:'Retry Passport Photo'}).click();
  await expect(page.getByAltText('Passport Photo',{exact:true})).toBeVisible();
});

test('password-protected PDF prompts for a password and renders after unlocking',async({page})=>{
  await page.addInitScript(()=>{localStorage.setItem('token','test-admin');localStorage.setItem('role','ADMIN')});
  await page.route('**/api/admin/applications/3',route=>route.fulfill({json:{id:3,fullNameEnglish:'Encrypted Document',applicationStatus:'PENDING_REVIEW',aadhaarFrontPath:'private/front.pdf'}}));
  await page.route('**/api/admin/members/3/documents/aadhaar-front',route=>route.fulfill({path:path.join(process.cwd(),'e2e/fixtures/test-document-protected.pdf'),contentType:'application/pdf'}));
  await page.goto('/admin/applications/3');
  await page.getByLabel('Aadhaar Front PDF password').fill('wrong');
  await page.getByRole('button',{name:'Unlock Aadhaar Front'}).click();
  await expect(page.getByRole('alert')).toContainText('Incorrect PDF password');
  await page.getByLabel('Aadhaar Front PDF password').fill('DemoPdf123!');
  await page.getByRole('button',{name:'Unlock Aadhaar Front'}).click();
  await expect(page.getByRole('img',{name:'Aadhaar Front PDF page 1'})).toBeVisible();
  await expect(page.getByLabel('Aadhaar Front PDF password')).toHaveCount(0);
});

test('malformed PDF shows a readable error and keeps the original downloadable',async({page})=>{
  await page.addInitScript(()=>{localStorage.setItem('token','test-admin');localStorage.setItem('role','ADMIN')});
  await page.route('**/api/admin/applications/4',route=>route.fulfill({json:{id:4,fullNameEnglish:'Invalid Document',applicationStatus:'PENDING_REVIEW',aadhaarBackPath:'private/back.pdf'}}));
  await page.route('**/api/admin/members/4/documents/aadhaar-back',route=>route.fulfill({contentType:'application/pdf',body:'%PDF-1.4\n%%EOF'}));
  await page.goto('/admin/applications/4');
  await expect(page.getByRole('alert')).toContainText('This PDF could not be read');
  await expect(page.getByRole('link',{name:'Download Aadhaar Back'})).toHaveAttribute('download','aadhaar-back.pdf');
});

test('missing uploaded documents are clearly identified',async({page})=>{
  await page.addInitScript(()=>{localStorage.setItem('token','test-admin');localStorage.setItem('role','ADMIN')});
  await page.route('**/api/admin/applications/2',route=>route.fulfill({json:{id:2,fullNameEnglish:'No Documents',applicationStatus:'PENDING_REVIEW'}}));
  await page.goto('/admin/applications/2');
  await expect(page.getByText('Not uploaded',{exact:true})).toHaveCount(3);
});
