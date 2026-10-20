import {test,expect} from '@playwright/test';

const sizes=[{width:1920,height:1080},{width:1366,height:768},{width:1280,height:800},{width:1024,height:768},{width:911,height:512},{width:683,height:384},{width:390,height:844}];

async function checkPageBounds(page:import('@playwright/test').Page){
  const bounds=await page.evaluate(()=>{
    const main=document.querySelector('.site-main')!.getBoundingClientRect();
    const footer=document.querySelector('.footer')!.getBoundingClientRect();
    return {width:document.documentElement.scrollWidth,viewport:innerWidth,footerTop:footer.top,footerBottom:footer.bottom,mainBottom:main.bottom,height:innerHeight};
  });
  expect(bounds.width).toBeLessThanOrEqual(bounds.viewport+1);
  expect(Math.abs(bounds.footerTop-bounds.mainBottom)).toBeLessThanOrEqual(1);
  expect(bounds.footerBottom).toBeGreaterThanOrEqual(bounds.height-1);
}

test('admin details and actions adapt to desktop widths and effective zoom sizes',async({page})=>{
  await page.addInitScript(()=>{localStorage.setItem('token','test-admin');localStorage.setItem('role','ADMIN')});
  await page.route('**/api/admin/applications/3',route=>route.fulfill({json:{id:3,fullNameEnglish:'Responsive Test Member',applicationStatus:'APPROVED',phoneNumber:'9000000001',cityTown:'Rajamahendravaram',district:'East Godavari',state:'Andhra Pradesh',planCode:'ELITE',membershipStatus:'ACTIVE'}}));
  for(const size of sizes){
    await page.setViewportSize(size);await page.goto('/admin/applications/3');
    await expect(page.getByRole('heading',{name:'Responsive Test Member'})).toBeVisible();
    await checkPageBounds(page);
    const buttons=await page.locator('.admin-decision button').evaluateAll(elements=>elements.map(element=>{
      const r=element.getBoundingClientRect();return {left:r.left,right:r.right,top:r.top,bottom:r.bottom,width:r.width,scroll:element.scrollWidth};
    }));
    for(let i=0;i<buttons.length;i++){
      expect(buttons[i].scroll).toBeLessThanOrEqual(buttons[i].width+2);
      for(let j=i+1;j<buttons.length;j++)expect(buttons[j].left-buttons[i].right>=10||buttons[j].top-buttons[i].bottom>=10).toBe(true);
    }
    if(size.width>=1280){
      const address=await page.locator('.admin-detail-card').filter({hasText:'Mobile'}).boundingBox();
      const membership=await page.locator('.admin-detail-card').filter({hasText:'Membership Status'}).boundingBox();
      expect(Math.abs(address!.y-membership!.y)).toBeLessThan(2);
      expect(membership!.x).toBeGreaterThan(address!.x);
    }
    if(size.width===1366)await page.screenshot({path:'test-results/admin-details-desktop.png',fullPage:true});
  }
});

test('short public pages keep the footer at the bottom without horizontal gaps',async({page})=>{
  await page.route('**/api/public/activities',route=>route.fulfill({json:[]}));
  for(const size of sizes){
    await page.setViewportSize(size);
    for(const path of ['/login','/activities']){
      await page.goto(path);await checkPageBounds(page);
      const contact=await page.locator('.footer-contact').boundingBox();
      expect(contact!.x).toBeGreaterThanOrEqual(0);
      expect(contact!.x+contact!.width).toBeLessThanOrEqual(size.width+1);
    }
  }
});
