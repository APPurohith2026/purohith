import {test,expect,Download} from '@playwright/test';
import ExcelJS from 'exceljs';

const approved={id:1,memberNumber:'AP-EGD-000001',fullNameTelugu:'పరీక్ష సభ్యుడు',fullNameEnglish:'=1+1',phoneNumber:'0900000001',district:'East Godavari',applicationStatus:'APPROVED',membershipStatus:'ACTIVE',planCode:'ELITE',aadhaarFrontPath:'/private/secret.pdf'};
const pending={id:2,memberNumber:'AP-EGD-000002',fullNameEnglish:'Pending Member',phoneNumber:'9000000002',district:'Hyderabad',applicationStatus:'PENDING_REVIEW',planCode:'PREMIUM'};
const stats={totalApplications:2,pendingApplications:1,approvedMembers:1,correctionRequired:0,activeMembers:1,premiumMembers:0,eliteMembers:1,directRegistrations:2,referredRegistrations:0,draftRegistrations:0,publishedActivities:0,totalActivityEnrollments:0};

test.beforeEach(async({page})=>{
  await page.addInitScript(()=>{localStorage.setItem('token','test-admin');localStorage.setItem('role','ADMIN')});
  await page.route('**/api/admin/dashboard',route=>route.fulfill({json:{stats,recentApplications:[approved]}}));
  await page.route('**/api/admin/members',route=>route.fulfill({json:[approved,pending]}));
  await page.route('**/api/admin/applications*',route=>route.fulfill({json:[approved,pending]}));
});
async function read(download:Download){
  expect(download.suggestedFilename()).toMatch(/\.xlsx$/);
  const workbook=new ExcelJS.Workbook();await workbook.xlsx.readFile((await download.path())!);return workbook;
}

test('dashboard exports summary, all records, and recent applications',async({page})=>{
  await page.goto('/admin');
  const [download]=await Promise.all([page.waitForEvent('download'),page.getByRole('button',{name:'Export Excel',exact:true}).click()]);
  const workbook=await read(download);
  expect(workbook.worksheets.map(sheet=>sheet.name)).toEqual(['Dashboard Summary','All Members and Applications','Recent Applications']);
  expect(workbook.getWorksheet('Dashboard Summary')!.getCell('B2').value).toBe(2);
  const all=workbook.getWorksheet('All Members and Applications')!;
  expect(all.rowCount).toBe(3);
  expect(all.getCell('C2').value).toBe('పరీక్ష సభ్యుడు');
  expect(all.getCell('D2').value).toBe('=1+1');
  expect(all.getCell('D2').type).toBe(ExcelJS.ValueType.String);
  expect(all.getCell('E2').value).toBe('0900000001');
  expect(JSON.stringify(all.getSheetValues())).not.toContain('/private/secret.pdf');
  expect(workbook.getWorksheet('Recent Applications')!.rowCount).toBe(2);
});

test('members export includes only the displayed approved and searched rows',async({page})=>{
  await page.goto('/admin/members');
  await page.locator('.admin-search input').fill('East Godavari');
  const [download]=await Promise.all([page.waitForEvent('download'),page.getByRole('button',{name:'Export Excel',exact:true}).click()]);
  const sheet=(await read(download)).worksheets[0];
  expect(sheet.rowCount).toBe(2);expect(sheet.getCell('B2').value).toBe(approved.memberNumber);
  await page.locator('.admin-search input').fill('missing');
  await expect(page.getByRole('button',{name:'Export Excel',exact:true})).toBeDisabled();
});

test('applications export follows the current search',async({page})=>{
  await page.goto('/admin/applications');
  await page.locator('.admin-search input').fill('Hyderabad');
  const [download]=await Promise.all([page.waitForEvent('download'),page.getByRole('button',{name:'Export Excel',exact:true}).click()]);
  const sheet=(await read(download)).worksheets[0];
  expect(sheet.rowCount).toBe(2);expect(sheet.getCell('B2').value).toBe(pending.memberNumber);
  await page.locator('.admin-search input').fill('');
  await page.locator('.admin-filter select').selectOption('APPROVED');
  const [filteredDownload]=await Promise.all([page.waitForEvent('download'),page.getByRole('button',{name:'Export Excel',exact:true}).click()]);
  const filteredSheet=(await read(filteredDownload)).worksheets[0];
  expect(filteredSheet.rowCount).toBe(2);expect(filteredSheet.getCell('B2').value).toBe(approved.memberNumber);
});

test('failed dashboard data request shows an error and allows retry',async({page})=>{
  await page.route('**/api/admin/members',route=>route.fulfill({status:403}));
  await page.goto('/admin');
  await page.getByRole('button',{name:'Export Excel',exact:true}).click();
  await expect(page.getByRole('alert')).toContainText('Excel export failed');
  await expect(page.getByRole('button',{name:'Export Excel',exact:true})).toBeEnabled();
});
