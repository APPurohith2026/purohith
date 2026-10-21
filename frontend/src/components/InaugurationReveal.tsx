import {useEffect,useRef,useState} from 'react';
import './InaugurationReveal.css';
const START=Date.parse('2026-10-21T00:00:00+05:30');
export const INAUGURATION_END=Date.parse('2026-10-22T00:00:00+05:30');
const SEEN_KEY='purohit_inauguration_2026_10_21_seen';
export function shouldShowInauguration(pathname:string){
 if(pathname!=='/'||Date.now()<START||Date.now()>=INAUGURATION_END)return false;
 try{return sessionStorage.getItem(SEEN_KEY)!=='yes'}catch{return true}
}
export function markInaugurationSeen(){try{sessionStorage.setItem(SEEN_KEY,'yes')}catch{}}
export default function InaugurationReveal({onClose}:{onClose:()=>void}){
 const video=useRef<HTMLVideoElement>(null),skip=useRef<HTMLButtonElement>(null);
 const [sound,setSound]=useState(true),[needsPlay,setNeedsPlay]=useState(false);
 useEffect(()=>{
  let active=true;
  skip.current?.focus();
  const expiry=window.setTimeout(onClose,Math.max(0,INAUGURATION_END-Date.now()));
  // Never leave a visitor waiting on a stalled video connection.
  const fallback=window.setTimeout(onClose,30000);
  const key=(e:KeyboardEvent)=>{if(e.key==='Escape')onClose()};window.addEventListener('keydown',key);
  const media=video.current;media?.play().catch(()=>{if(active)setNeedsPlay(true)});
  return ()=>{active=false;clearTimeout(expiry);clearTimeout(fallback);window.removeEventListener('keydown',key);media?.pause()};
 },[onClose]);
 async function playWithSound(){
  const media=video.current;if(!media)return;
  const nextSound=needsPlay?true:!sound;
  media.muted=!nextSound;setSound(nextSound);
  try{await media.play();setNeedsPlay(false)}catch{setNeedsPlay(true)}
 }
 return <section className="inauguration-reveal" role="dialog" aria-modal="true" aria-labelledby="inauguration-title">
  <header><h1 id="inauguration-title">AP Brahmana Purohit Samakya</h1><button ref={skip} className="reveal-skip" onClick={onClose}>Skip / Continue to website</button></header>
  <video ref={video} className="inauguration-video" src="/videos/inauguration-logo-reveal.mp4" autoPlay muted={!sound} playsInline preload="auto" onPlaying={()=>setNeedsPlay(false)} onEnded={onClose} onError={onClose} aria-label="Inauguration logo reveal"/>
  <button className="reveal-sound" onClick={()=>void playWithSound()}>{needsPlay?'Play reveal with sound':sound?'Mute video':'Enable video sound'}</button>
 </section>
}
