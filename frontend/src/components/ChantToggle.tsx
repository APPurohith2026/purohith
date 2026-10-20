import {useCallback,useEffect,useRef,useState} from 'react';
import {Volume2,VolumeX} from 'lucide-react';
import './ChantToggle.css';

export default function ChantToggle(){
  const audioRef=useRef<HTMLAudioElement>(null);
  const enabledRef=useRef(true),pendingRef=useRef(false),attemptRef=useRef(0);
  const [enabled,setEnabled]=useState(true),[playing,setPlaying]=useState(false);
  const [loading,setLoading]=useState(false),[error,setError]=useState('');
  const start=useCallback(async()=>{
    const audio=audioRef.current;
    if(!audio||!enabledRef.current||pendingRef.current||!audio.paused)return;
    const attempt=++attemptRef.current;
    pendingRef.current=true;setLoading(true);setError('');audio.volume=0.35;
    try{await audio.play()}
    catch(reason:unknown){
      if(attempt!==attemptRef.current)return;
      if(!(reason instanceof DOMException&&(reason.name==='NotAllowedError'||reason.name==='AbortError')))
        setError('The chant is unavailable right now. Please try again later.');
    }finally{
      if(attempt===attemptRef.current){pendingRef.current=false;setLoading(false)}
    }
  },[]);
  useEffect(()=>{
    void start();
    const resume=(event:Event)=>{
      // Stopping must not also trigger the page's playback retry.
      if(event.target instanceof Element&&event.target.closest('.chant-toggle'))return;
      if(enabledRef.current)void start();
    };
    window.addEventListener('pointerup',resume);window.addEventListener('click',resume);window.addEventListener('keydown',resume);
    const audio=audioRef.current;
    return ()=>{
      window.removeEventListener('pointerup',resume);window.removeEventListener('click',resume);window.removeEventListener('keydown',resume);
      ++attemptRef.current;pendingRef.current=false;audio?.pause();
    };
  },[start]);
  function toggle(){
    const audio=audioRef.current;if(!audio)return;setError('');
    if(!audio.paused||pendingRef.current){
      enabledRef.current=false;setEnabled(false);++attemptRef.current;
      pendingRef.current=false;setLoading(false);audio.pause();audio.currentTime=0;
    }else{enabledRef.current=true;setEnabled(true);void start()}
  }
  return <div className="chant-control">
    <audio ref={audioRef} src="/audio/gayathri-mantra.mp3" loop preload="auto"
      onPlay={()=>{if(!enabledRef.current)audioRef.current?.pause()}}
      onPlaying={()=>{if(enabledRef.current){setPlaying(true);setLoading(false)}else audioRef.current?.pause()}}
      onPause={()=>setPlaying(false)}
      onError={()=>{setPlaying(false);setLoading(false);setError('The chant is unavailable right now. Please try again later.')}}/>
    <button type="button" className={`chant-toggle${playing?' is-playing':''}`} onClick={toggle}
      aria-pressed={playing} aria-label={playing||loading?'Stop Gayathri chant':'Play Gayathri chant'}
      aria-describedby={error?'chant-error':undefined}
      title={enabled&&!playing?'Click to play the chant. Your browser may require a click before starting sound.':'Gayathri chant'}>
      {enabled?<Volume2 size={18} aria-hidden="true"/>:<VolumeX size={18} aria-hidden="true"/>}
      <span>{loading?'Loading…':playing?'Sound on':enabled?'Tap for sound':'Sound off'}</span>
    </button>
    {error&&<span id="chant-error" className="chant-error" role="status">{error}</span>}
  </div>;
}
