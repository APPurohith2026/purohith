import { useEffect, useState } from 'react';
import { Printer, Search } from 'lucide-react';
import { api } from '../services/api';
import MemberIdCard, { CardMember } from '../components/MemberIdCard';

type Member = CardMember & { id: number; applicationStatus: string; passportPhotoPath?: string };
export default function AdminIdCards() {
  const [members, setMembers] = useState<Member[]>([]);
  const [selected, setSelected] = useState<Member | null>(null);
  const [query, setQuery] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);
  const [photoReady, setPhotoReady] = useState(false);
  useEffect(() => {
    let active = true;
    api.get('/admin/members').then(({ data }) => {
      if (!Array.isArray(data)) throw new Error('Invalid members response');
      if (active) setMembers(data.filter(m => m.applicationStatus === 'APPROVED' && m.memberNumber));
    }).catch(() => { if (active) setError('Members could not be loaded. Please refresh and try again.'); })
      .finally(() => { if (active) setLoading(false); });
    return () => { active = false; };
  }, []);
  const rows = members.filter(m => [m.memberNumber, m.fullNameTelugu, m.fullNameEnglish, m.phoneNumber, m.district].join(' ').toLowerCase().includes(query.toLowerCase()));
  return <div className="id-cards-page"><div className="admin-page-head"><div><span>MEMBERSHIP</span><h1>Member ID Cards</h1><p>Select an approved member to generate a card with their passport photo and registered details.</p></div></div>
    <label className="admin-search"><Search /><input aria-label="Search members" placeholder="Member ID, name, phone or district" value={query} onChange={e => setQuery(e.target.value)} /></label>
    {error && <div className="alert error" role="alert">{error}</div>}
    {loading ? <p>Loading members…</p> : <div className="id-cards-layout"><div className="admin-panel id-member-list">
      {rows.length === 0 && <p>No approved members found.</p>}
      {rows.map(member => <button key={member.id} className={'id-member-option ' + (selected?.id === member.id ? 'selected' : '')} onClick={() => { setSelected(member); setPhotoReady(false); }}><b>{member.fullNameTelugu || member.fullNameEnglish}</b><span>{member.memberNumber}</span><small>{member.district} · {member.planCode}</small></button>)}
    </div><div className="id-card-preview">{selected ? <><MemberIdCard key={selected.id} member={selected} photoEndpoint={`/admin/members/${selected.id}/passport-photo`} onPhotoReady={setPhotoReady} /><button className="btn btn-primary id-print-button" disabled={!photoReady} onClick={() => window.print()}><Printer /> Print / Save as PDF</button><p className="id-print-help">Choose “Save as PDF” in the print dialog. Printing is available once the passport photo loads.</p></> : <div className="admin-panel">Select a member to preview their ID card.</div>}</div></div>}
  </div>;
}
