import {test,expect,Page} from '@playwright/test';

async function mockAudio(page:Page,blocked:boolean){
  await page.addInitScript(({blocked})=>{
    const state=window as unknown as {blockAudio:boolean;playCalls:number};
    state.blockAudio=blocked;state.playCalls=0;
    const paused=new WeakMap<HTMLMediaElement,boolean>();
    Object.defineProperty(HTMLMediaElement.prototype,'paused',{configurable:true,get(){return paused.get(this)??true}});
    HTMLMediaElement.prototype.play=function(){
      state.playCalls++;
      if(state.blockAudio)return Promise.reject(new DOMException('Autoplay blocked','NotAllowedError'));
      paused.set(this,false);this.dispatchEvent(new Event('play'));this.dispatchEvent(new Event('playing'));return Promise.resolve();
    };
    HTMLMediaElement.prototype.pause=function(){paused.set(this,true);this.dispatchEvent(new Event('pause'))};
  },{blocked});
}

test('sound starts by default and stays stopped until the user enables it',async({page})=>{
  await mockAudio(page,false);await page.goto('/');
  const stop=page.getByRole('button',{name:'Stop Gayathri chant'});
  await expect(stop).toContainText('Sound on');
  await expect(stop).toHaveAttribute('aria-pressed','true');
  await stop.click();
  await expect(page.getByRole('button',{name:'Play Gayathri chant'})).toContainText('Sound off');
  await page.locator('#home-title').click();
  expect(await page.locator('audio').evaluate((audio:HTMLAudioElement)=>audio.paused)).toBe(true);
  await page.getByRole('button',{name:'Play Gayathri chant'}).click();
  await expect(stop).toContainText('Sound on');
});

test('blocked autoplay starts after the first page interaction',async({page})=>{
  await mockAudio(page,true);await page.goto('/');
  const stop=page.getByRole('button',{name:'Stop Gayathri chant'});
  await expect(page.getByRole('button',{name:'Play Gayathri chant'})).toContainText('Tap for sound');
  await page.evaluate(()=>{(window as unknown as {blockAudio:boolean}).blockAudio=false});
  await page.locator('#home-title').click();
  await expect(stop).toContainText('Sound on');
});

test('one click starts blocked audio and Stop prevents automatic restarts',async({page})=>{
  await mockAudio(page,true);await page.goto('/');
  await expect(page.getByRole('button',{name:'Play Gayathri chant'})).toContainText('Tap for sound');
  await page.evaluate(()=>{(window as unknown as {blockAudio:boolean}).blockAudio=false});
  await page.getByRole('button',{name:'Play Gayathri chant'}).click();
  await expect(page.getByRole('button',{name:'Stop Gayathri chant'})).toContainText('Sound on');
  await page.getByRole('button',{name:'Stop Gayathri chant'}).click();
  const calls=await page.evaluate(()=>(window as unknown as {playCalls:number}).playCalls);
  await page.evaluate(()=>{(window as unknown as {blockAudio:boolean}).blockAudio=false});
  await page.locator('#home-title').click();
  expect(await page.evaluate(()=>(window as unknown as {playCalls:number}).playCalls)).toBe(calls);
  await expect(page.getByRole('button',{name:'Play Gayathri chant'})).toContainText('Sound off');
});
