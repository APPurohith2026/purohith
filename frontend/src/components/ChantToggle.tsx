import { useEffect, useRef, useState } from 'react';
import { Volume2, VolumeX } from 'lucide-react';
import './ChantToggle.css';

export default function ChantToggle() {
  const audioRef = useRef<HTMLAudioElement>(null);
  const [playing, setPlaying] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');


  useEffect(() => {
    const audio = audioRef.current;
    if (!audio) return;
    let disposed = false;
    audio.volume = 0.35;
    void audio.play().catch((reason: unknown) => {
      // Blocked autoplay leaves the button ready for a visitor's click.
      if (disposed || (reason instanceof DOMException &&
        (reason.name === 'NotAllowedError' || reason.name === 'AbortError'))) return;
      setError('The chant is unavailable right now. Please try again later.');
    });
    return () => {
      disposed = true;
      audio.pause();
    };
  }, []);
  async function toggle() {
    const audio = audioRef.current;
    if (!audio || loading) return;
    setError('');
    if (!audio.paused) {
      audio.pause();
      audio.currentTime = 0;
      return;
    }
    setLoading(true);
    audio.volume = 0.35;
    try {
      await audio.play();
    } catch {
      setError('The chant is unavailable right now. Please try again later.');
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="chant-control">
      <audio
        ref={audioRef}
        src="/audio/gayathri-mantra.mp3"
        loop
        preload="auto"
        onPlay={() => setPlaying(true)}
        onPause={() => setPlaying(false)}
        onError={() => {
          setPlaying(false);
          setLoading(false);
          setError('The chant is unavailable right now. Please try again later.');
        }}
      />
      <button
        type="button"
        className={`chant-toggle${playing ? ' is-playing' : ''}`}
        onClick={toggle}
        aria-pressed={playing}
        aria-label={playing ? 'Stop Gayathri chant' : 'Play Gayathri chant'}
        aria-describedby={error ? 'chant-error' : undefined}
        disabled={loading}
        title="Gayathri chant"
      >
        {playing ? <Volume2 size={18} aria-hidden="true" /> : <VolumeX size={18} aria-hidden="true" />}
        <span>{loading ? 'Loading…' : playing ? 'Sound on' : 'Sound off'}</span>
      </button>
      {error && <span id="chant-error" className="chant-error" role="status">{error}</span>}
    </div>
  );
}
