import {Capacitor,registerPlugin} from '@capacitor/core';
import {App} from '@capacitor/app';
import {Filesystem,Directory} from '@capacitor/filesystem';
import {Share} from '@capacitor/share';
if(Capacitor.isNativePlatform()){
 void App.addListener('backButton',({canGoBack})=>{if(canGoBack)window.history.back();else void App.minimizeApp()});
 void App.addListener('appStateChange',({isActive})=>{if(!isActive)document.querySelectorAll<HTMLMediaElement>('audio,video').forEach(media=>media.pause())});
 const NativePrint=registerPlugin<{print():Promise<void>}>('NativePrint');
 window.print=()=>{void NativePrint.print().catch(()=>window.alert('Unable to open Android printing. Please try again.'))};
 async function shareDownload(anchor:HTMLAnchorElement){
  const response=await fetch(anchor.href);if(!response.ok)throw new Error('Download unavailable');
  const blob=await response.blob();
  const base64=await new Promise<string>((resolve,reject)=>{const reader=new FileReader();reader.onload=()=>resolve(String(reader.result).split(',')[1]);reader.onerror=()=>reject(reader.error);reader.readAsDataURL(blob)});
  const filename=(anchor.download||anchor.href.split('/').pop()||'download').replace(/[^a-zA-Z0-9._-]/g,'_');
  const result=await Filesystem.writeFile({path:`downloads/${Date.now()}-${filename}`,data:base64,directory:Directory.Cache,recursive:true});
  await Share.share({title:filename,url:result.uri});
 }
 // Covers generated Excel/PDF files and the membership payment QR download.
 document.addEventListener('click',event=>{
  const anchor=event.target instanceof Element?event.target.closest<HTMLAnchorElement>('a[download]'):null;
  if(!anchor||event.defaultPrevented)return;
  event.preventDefault();void shareDownload(anchor).catch(()=>window.alert('Unable to save or share this file. Please try again.'));
 });
}
