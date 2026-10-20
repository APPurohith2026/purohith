import './AuthBackground.css';
import {FormEvent,useEffect,useState} from 'react';
import {Link} from 'react-router-dom';
import {api} from '../services/api';

export default function ForgotPassword(){
  const [step,setStep]=useState<'identify'|'otp'|'password'|'done'>('identify');
  const [memberId,setMemberId]=useState(''),[phoneNumber,setPhoneNumber]=useState('');
  const [otp,setOtp]=useState(''),[password,setPassword]=useState(''),[confirm,setConfirm]=useState('');
  const [challengeId,setChallengeId]=useState(''),[resetToken,setResetToken]=useState('');
  const [busy,setBusy]=useState(false),[error,setError]=useState(''),[notice,setNotice]=useState('');
  const [cooldown,setCooldown]=useState(0);
  useEffect(()=>{if(cooldown<=0)return;const timer=window.setTimeout(()=>setCooldown(cooldown-1),1000);return ()=>window.clearTimeout(timer)},[cooldown]);
  async function request(){
    setBusy(true);setError('');setNotice('');
    try{
      const {data}=await api.post('/auth/password-reset/request',{memberId:memberId.trim(),phoneNumber});
      setChallengeId(data.challengeId);setNotice(data.message);setOtp('');setCooldown(60);setStep('otp');
    }catch(e:any){setError(e.response?.data?.message||'Could not request OTP. Please try again later.')}
    finally{setBusy(false)}
  }
  async function submit(e:FormEvent){
    e.preventDefault();if(busy)return;
    if(step==='identify'){await request();return}
    setBusy(true);setError('');
    try{
      if(step==='otp'){
        const {data}=await api.post('/auth/password-reset/verify',{challengeId,otp});
        setResetToken(data.resetToken);setOtp('');setNotice('Phone verified. Set your new password within 5 minutes.');setStep('password');
      }else if(step==='password'){
        if(password!==confirm){setError('Passwords do not match.');return}
        if(new TextEncoder().encode(password).length>72){setError('Password must be at most 72 UTF-8 bytes.');return}
        const {data}=await api.post('/auth/password-reset/confirm',{challengeId,resetToken,newPassword:password});
        setPassword('');setConfirm('');setResetToken('');setChallengeId('');setNotice(data.message);setStep('done');
      }
    }catch(e:any){setError(e.response?.data?.message||'Verification failed. Please try again.')}
    finally{setBusy(false)}
  }
  function restart(){setStep('identify');setChallengeId('');setResetToken('');setOtp('');setPassword('');setConfirm('');setError('');setNotice('')}
  return <section className="auth-wrap auth-temple"><form className="panel form" onSubmit={submit}>
    <span className="eyebrow">Member account recovery</span><h1>Forgot password?</h1>
    {step==='identify'&&<>
      <p>Enter your Member ID and registered phone number. We will send an SMS OTP to that phone.</p>
      <label>Member ID<input autoComplete="username" value={memberId} onChange={e=>setMemberId(e.target.value)} placeholder="AP-EGD-000123" maxLength={40} required/></label>
      <label>Registered phone number<input type="tel" autoComplete="tel" value={phoneNumber} onChange={e=>setPhoneNumber(e.target.value)} placeholder="Phone number used during registration" pattern="[+0-9 ()-]{10,25}" required/></label>
    </>}
    {step==='otp'&&<label>6-digit OTP<input autoComplete="one-time-code" inputMode="numeric" pattern="[0-9]{6}" maxLength={6} value={otp} onChange={e=>setOtp(e.target.value.replace(/\D/g,''))} required/></label>}
    {step==='password'&&<>
      <label>New password<input type="password" autoComplete="new-password" minLength={8} maxLength={72} value={password} onChange={e=>setPassword(e.target.value)} required/></label>
      <label>Confirm new password<input type="password" autoComplete="new-password" minLength={8} maxLength={72} value={confirm} onChange={e=>setConfirm(e.target.value)} required/></label>
    </>}
    {notice&&<p role="status">{notice}</p>}{error&&<div role="alert" className="alert error">{error}</div>}
    {step!=='done'&&<button className="btn btn-primary btn-lg" disabled={busy}>{busy?'Please wait…':step==='identify'?'Send OTP':step==='otp'?'Verify OTP':'Save new password'}</button>}
    {step==='otp'&&<button type="button" className="btn btn-outline" disabled={busy||cooldown>0} onClick={()=>void request()}>{cooldown>0?`Resend OTP in ${cooldown}s`:'Resend OTP'}</button>}
    {(step==='otp'||step==='password')&&<button type="button" className="btn btn-outline" disabled={busy} onClick={restart}>Start again</button>}
    <Link to="/login">Back to login</Link>
  </form></section>;
}
