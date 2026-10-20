import {test,expect} from '@playwright/test';

test('temple image stays in the right hero panel on desktop and fills the hero on mobile',async({page})=>{
  for(const width of [1920,1366,1024,768,683,390]){
    await page.setViewportSize({width,height:900});await page.goto('/');
    const image=page.locator('.home-hero-image img');
    await expect(image).toBeVisible();
    await expect.poll(()=>image.evaluate((img:HTMLImageElement)=>img.complete&&img.naturalWidth>0)).toBe(true);
    const geometry=await page.evaluate(()=>{
      const hero=document.querySelector('.home-hero')!.getBoundingClientRect();
      const panel=document.querySelector('.home-hero-image')!.getBoundingClientRect();
      const img=document.querySelector('.home-hero-image img')!.getBoundingClientRect();
      return {hero:{x:hero.x,y:hero.y,right:hero.right,bottom:hero.bottom,width:hero.width},panel:{x:panel.x,y:panel.y,right:panel.right,bottom:panel.bottom},img:{width:img.width,height:img.height},scrollWidth:document.documentElement.scrollWidth,viewport:innerWidth};
    });
    expect(Math.abs(geometry.panel.y-geometry.hero.y)).toBeLessThan(1);
    expect(Math.abs(geometry.panel.bottom-geometry.hero.bottom)).toBeLessThan(1);
    expect(Math.abs(geometry.panel.right-geometry.hero.right)).toBeLessThan(1);
    expect(geometry.img.width).toBeGreaterThan(0);expect(geometry.img.height).toBeGreaterThan(0);
    if(width>760)expect(geometry.panel.x-geometry.hero.x).toBeCloseTo(geometry.hero.width*.46,0);
    else expect(Math.abs(geometry.panel.x-geometry.hero.x)).toBeLessThan(1);
    expect(geometry.scrollWidth).toBeLessThanOrEqual(geometry.viewport+1);
    if(width===1366||width===390)await page.screenshot({path:`test-results/home-hero-${width}.png`});
  }
});
