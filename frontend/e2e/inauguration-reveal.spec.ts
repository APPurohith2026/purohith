import {test,expect} from '@playwright/test';
const TODAY=new Date('2026-10-21T12:00:00+05:30');
test('today shows reveal, skip opens home, and reload does not repeat it',async({page})=>{
 await page.clock.install({time:TODAY});
 // Keep media deterministic; playback success is checked separately with the real asset.
 await page.addInitScript(()=>{Object.defineProperty(HTMLMediaElement.prototype,'play',{configurable:true,value:()=>Promise.resolve()})});
 await page.route('**/videos/inauguration-logo-reveal.mp4',r=>new Promise(()=>{}));
 await page.goto('/',{waitUntil:'domcontentloaded'});await expect(page.getByRole('dialog')).toBeVisible();
 await expect(page.locator('.chant-control')).toHaveCount(0);
 await page.getByRole('button',{name:'Skip / Continue to website'}).click();await expect(page.locator('.home-hero')).toBeVisible();
 await page.reload();await expect(page.getByRole('dialog')).toHaveCount(0);
});
test('video ends and proceeds to home',async({page})=>{
 await page.clock.install({time:TODAY});await page.route('**/videos/inauguration-logo-reveal.mp4',r=>new Promise(()=>{}));
 await page.goto('/',{waitUntil:'domcontentloaded'});await expect(page.getByRole('dialog')).toBeVisible();
 await page.locator('.inauguration-video').dispatchEvent('ended');await expect(page.locator('.home-hero')).toBeVisible();
});
test('expires automatically at midnight India time and stays off tomorrow',async({page})=>{
 await page.clock.install({time:new Date('2026-10-21T23:59:59+05:30')});await page.route('**/videos/inauguration-logo-reveal.mp4',r=>new Promise(()=>{}));
 await page.goto('/',{waitUntil:'domcontentloaded'});await expect(page.getByRole('dialog')).toBeVisible();await page.clock.runFor(1500);await expect(page.getByRole('dialog')).toHaveCount(0);
 await page.reload();await expect(page.getByRole('dialog')).toHaveCount(0);await expect(page.locator('.home-hero')).toBeVisible();
});
test('login is accessible directly and video errors never block home',async({page})=>{
 await page.clock.install({time:TODAY});await page.goto('/login');await expect(page.getByTestId('login-identifier')).toBeVisible();await expect(page.getByRole('dialog')).toHaveCount(0);
 await page.route('**/videos/inauguration-logo-reveal.mp4',r=>r.abort());await page.goto('/',{waitUntil:'domcontentloaded'});await expect(page.locator('.home-hero')).toBeVisible();
});

test('a stalled video times out without blocking the site',async({page})=>{
 await page.clock.install({time:TODAY});await page.route('**/videos/inauguration-logo-reveal.mp4',r=>new Promise(()=>{}));
 await page.goto('/',{waitUntil:'domcontentloaded'});await expect(page.getByRole('dialog')).toBeVisible();await page.clock.runFor(31000);await expect(page.locator('.home-hero')).toBeVisible();
});
