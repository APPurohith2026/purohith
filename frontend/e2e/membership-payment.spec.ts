import {test,expect} from '@playwright/test';
const initial={trackingId:'PAY-test-001',reference:null,status:'PENDING',amount:2500,planCode:'ELITE'};
test('member submits reference for manual verification without overflow',async({page})=>{
 await page.addInitScript(()=>{localStorage.setItem('token','test-member');localStorage.setItem('role','MEMBER')});
 await page.route('**/api/member/dashboard',r=>r.fulfill({json:{member:{id:1,fullNameEnglish:'Payment Test',applicationStatus:'PENDING_REVIEW',planCode:'ELITE'},availableActivities:0,registrations:0}}));
 await page.route('**/api/member/id-card',r=>r.fulfill({status:404,json:{message:'Not issued'}}));
 let payment:any={...initial};
 await page.route('**/api/member/payment',async r=>{if(r.request().method()==='POST'){expect(r.request().postDataJSON()).toEqual({reference:'123456789012'});payment={...payment,reference:'123456789012',status:'MANUAL_REVIEW'}}await r.fulfill({json:payment})});
 await page.goto('/dashboard');
 await expect(page.getByText('PAY-test-001',{exact:true})).toBeVisible();
 const qr=page.locator('.payment-qr-window img');await expect(qr).toBeVisible();expect(await qr.evaluate((i:HTMLImageElement)=>i.complete&&i.naturalWidth>0)).toBeTruthy();
 await page.getByLabel('UPI transaction reference / UTR').fill('123456789012');
 await page.getByRole('button',{name:'Submit payment reference'}).click();
 await expect(page.getByText('Awaiting admin verification',{exact:true})).toBeVisible();
 expect(await page.evaluate(()=>document.documentElement.scrollWidth<=innerWidth)).toBeTruthy();
});
test('admin explicitly confirms transaction before membership approval',async({page})=>{
 await page.addInitScript(()=>{localStorage.setItem('token','test-admin');localStorage.setItem('role','ADMIN')});
 await page.route('**/api/admin/applications/1',r=>r.fulfill({json:{id:1,fullNameEnglish:'Payment Test',applicationStatus:'PENDING_REVIEW'}}));
 let payment:any={...initial,reference:'123456789012',status:'MANUAL_REVIEW'};
 await page.route('**/api/admin/members/1/payment',async r=>{if(r.request().method()==='PATCH'){expect(r.request().postDataJSON()).toEqual({received:true,reference:'123456789012'});payment={...payment,status:'VERIFIED'}}await r.fulfill({json:payment})});
 await page.goto('/admin/applications/1');
 await page.getByRole('button',{name:'Confirm payment received'}).click();
 await expect(page.getByText('Payment verified',{exact:true})).toBeVisible();
 await expect(page.getByRole('button',{name:'Confirm payment received'})).toHaveCount(0);
});
