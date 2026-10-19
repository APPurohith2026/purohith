import { ReactNode, useEffect, useState } from 'react';
import { api } from '../services/api';

export type CardMember = {
  id?: number; memberNumber: string; fullNameTelugu?: string; fullNameEnglish?: string;
  phoneNumber?: string; district?: string; state?: string; bloodGroup?: string;
  gotram?: string; planCode?: string; membershipStatus?: string; expiryDate?: string;
};

export default function MemberIdCard({ member, photoEndpoint, onPhotoReady, verificationQr }: {
  member: CardMember; photoEndpoint: string; onPhotoReady?: (ready: boolean) => void; verificationQr?: ReactNode;
}) {
  const [photo, setPhoto] = useState('');
  const [error, setError] = useState('');
  useEffect(() => {
    let active = true;
    let url = '';
    setPhoto(''); setError(''); onPhotoReady?.(false);
    api.get(photoEndpoint, { responseType: 'blob' }).then(response => {
      if (!active) return;
      url = URL.createObjectURL(response.data);
      setPhoto(url);
    }).catch(() => { if (active) setError('Passport photo unavailable. Check the uploaded photo and photo service.'); });
    return () => { active = false; if (url) URL.revokeObjectURL(url); };
  }, [photoEndpoint, onPhotoReady]);
  return <article className={'member-id-card ' + (member.planCode === 'ELITE' ? 'member-id-elite' : '')} aria-label="Member ID card">
    <header><span className="id-emblem">ॐ</span><div><b>పురోహిత సమాజం</b><small>PUROHIT COMMUNITY · MEMBER IDENTITY CARD</small></div></header>
    <div className="id-card-body">
      <div className="id-photo">{photo ? <img src={photo} alt="Member passport photo" onLoad={() => onPhotoReady?.(true)} onError={() => { setError('Passport photo could not be displayed.'); onPhotoReady?.(false); }} /> : <span>{error || 'Loading photo…'}</span>}</div>
      <div className="id-details"><h2>{member.fullNameTelugu || member.fullNameEnglish}</h2>
        {member.fullNameEnglish && member.fullNameEnglish !== member.fullNameTelugu && <p>{member.fullNameEnglish}</p>}
        <div className="id-number"><small>MEMBER ID</small><strong>{member.memberNumber}</strong></div>
        <dl>{[['District', member.district], ['State', member.state], ['Phone', member.phoneNumber], ['Blood group', member.bloodGroup], ['Gotram', member.gotram]].map(([label, value]) => value && <div key={label}><dt>{label}</dt><dd>{value}</dd></div>)}</dl>
      </div>
    </div>
    {error && <p className="id-photo-error" role="alert">{error}</p>}
    <footer><b>{member.planCode || 'COMMUNITY'} MEMBER</b><span>{member.membershipStatus}</span>{verificationQr}<span>Valid until: {member.expiryDate || 'Not specified'}</span></footer>
  </article>;
}
