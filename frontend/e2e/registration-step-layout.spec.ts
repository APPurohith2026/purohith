import {test,expect} from '@playwright/test';
test('all five registration steps share the form width and align at desktop and mobile sizes',async({page})=>{
 await page.route('**/api/public/plans',r=>r.fulfill({json:[]}));
 for(const width of [320,360,390,560,768,1024,1366,1920]){
  await page.setViewportSize({width,height:900});await page.goto('/register');
  await expect(page.locator('.wizard-rail button')).toHaveCount(5);
  await page.evaluate(()=>document.fonts.ready);
  const layout=await page.evaluate(()=>{
   const rect=(selector:string)=>{const r=document.querySelector(selector)!.getBoundingClientRect();return {x:r.x,y:r.y,width:r.width,right:r.right}};
   const circles=Array.from(document.querySelectorAll('.wizard-rail b')).map(e=>{const r=e.getBoundingClientRect();return {x:r.x+r.width/2,y:r.y,right:r.right}});
   const labels=Array.from(document.querySelectorAll('.wizard-rail span')).map(e=>({scroll:e.scrollWidth,width:e.clientWidth}));
   const inputs=Array.from(document.querySelectorAll('.reg-field > input,.reg-field > select')).map(e=>{const r=e.getBoundingClientRect(),card=e.closest('.account-block,.wizard-card')!.getBoundingClientRect();return {left:r.left,right:r.right,cardLeft:card.left,cardRight:card.right}});
   return {inputs,rail:rect('.wizard-rail'),heading:rect('.wizard-heading'),form:rect('.wizard-shell'),circles,labels,scroll:document.documentElement.scrollWidth,viewport:innerWidth};
  });
  expect(layout.scroll,`page overflow at ${width}`).toBeLessThanOrEqual(width+1);
  for(const container of [layout.heading,layout.form]){
   expect(Math.abs(container.x-layout.rail.x),`left edge at ${width}`).toBeLessThanOrEqual(1);
   expect(Math.abs(container.width-layout.rail.width),`width at ${width}`).toBeLessThanOrEqual(1);
  }
  for(const input of layout.inputs){expect(input.left).toBeGreaterThanOrEqual(input.cardLeft);expect(input.right,`input overflow at ${width}`).toBeLessThanOrEqual(input.cardRight+1)}
  const spacing=layout.circles[1].x-layout.circles[0].x;
  for(let i=0;i<5;i++){
   expect(Math.abs(layout.circles[i].y-layout.circles[0].y),`circle row at ${width}`).toBeLessThanOrEqual(1);
   expect(layout.circles[i].right).toBeLessThanOrEqual(width);
   expect(layout.labels[i].scroll).toBeLessThanOrEqual(layout.labels[i].width+1);
   if(i>0)expect(Math.abs(layout.circles[i].x-layout.circles[i-1].x-spacing)).toBeLessThanOrEqual(1);
  }
  if(width===1366||width===320)await page.screenshot({path:`test-results/registration-steps-${width}.png`,fullPage:true});
 }
});
