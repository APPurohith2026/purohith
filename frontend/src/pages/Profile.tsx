import {useEffect,useState,type ReactNode} from 'react';
import {api} from '../services/api';
import {FileCheck2,Home,IdCard,UserRound} from 'lucide-react';

export default function Profile(){
  const [m,setM]=useState<any>(null);
  useEffect(()=>{api.get('/member/profile').then(r=>setM(r.data))},[]);
  if(!m)return <section className="section">Loading...</section>;
  return <section className="section profile-summary">
    <div className="section-heading left"><span>నా వివరాలు</span><h1>సభ్యత్వ దరఖాస్తు వివరాలు</h1><p>నమోదు సమయంలో సమర్పించిన వివరాల సారాంశం.</p></div>
    <div className="profile-summary-grid">
      <Summary title="వ్యక్తిగత వివరాలు" icon={<UserRound/>} rows={[
        ['పూర్తి పేరు',m.fullNameTelugu||m.fullNameEnglish],['Member Reference ID',m.memberReferenceId],['పుట్టిన తేదీ',m.dateOfBirth],['లింగం',m.gender],['వృత్తి',m.profession],['గోత్రం',m.gotram],['రక్త గ్రూప్',m.bloodGroup],['ఆధార్',m.aadhaarMasked]
      ]}/>
      <Summary title="చిరునామా & సంప్రదింపు" icon={<Home/>} rows={[
        ['ప్రత్యామ్నాయ సంప్రదింపు',m.alternateContactNumber],['వాట్సాప్',m.whatsappNumber],['డోర్ నంబర్',m.doorNumber],['నగరం / పట్టణం',m.cityTown],['నియోజకవర్గం',m.constituency],['జిల్లా',m.district],['District Code',m.districtCode],['పిన్ కోడ్',m.pinCode],['రాష్ట్రం',m.state]
      ]}/>
      <Summary title="సభ్యత్వం" icon={<IdCard/>} rows={[
        ['Plan',m.planCode],['Application Status',m.applicationStatus],['Membership Status',m.membershipStatus],['Member ID',m.memberNumber||'—'],['Validity',m.expiryDate||'—']
      ]}/>
      <Summary title="పత్రాలు" icon={<FileCheck2/>} rows={[
        ['ఆధార్ ముందు వైపు',m.aadhaarFrontPath?'Uploaded':'—'],['ఆధార్ వెనుక వైపు',m.aadhaarBackPath?'Uploaded':'—'],['పాస్‌పోర్ట్ ఫోటో',m.passportPhotoPath?'Uploaded':'—']
      ]}/>
    </div>
  </section>
}

function Summary({title,icon,rows}:{title:string;icon:ReactNode;rows:[string,any][]}){
  return <article className="panel profile-summary-card"><header>{icon}<h2>{title}</h2></header><dl>{rows.map(([k,v])=><div key={k}><dt>{k}</dt><dd>{v||'—'}</dd></div>)}</dl></article>
}
