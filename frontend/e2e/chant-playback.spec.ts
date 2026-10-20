import {test,expect} from '@playwright/test';

test.use({launchOptions:{args:['--autoplay-policy=user-gesture-required']}});

test('actual MP3 plays after one click when autoplay is blocked',async({page})=>{
  await page.goto('/');
  const play=page.getByRole('button',{name:'Play Gayathri chant'});
  await expect(play).toContainText('Tap for sound');
  await play.click();
  await expect(page.getByRole('button',{name:'Stop Gayathri chant'})).toContainText('Sound on');
  await expect.poll(()=>page.locator('audio').evaluate((audio:HTMLAudioElement)=>({paused:audio.paused,advanced:audio.currentTime>0,error:audio.error?.code??null})))
    .toEqual({paused:false,advanced:true,error:null});
  await page.getByRole('button',{name:'Stop Gayathri chant'}).click();
  await expect(play).toContainText('Sound off');
  await page.locator('#home-title').click();
  expect(await page.locator('audio').evaluate((audio:HTMLAudioElement)=>audio.paused)).toBe(true);
});
