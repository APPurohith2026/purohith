import './RegistrationValidation.css';
import BirthDateInput from '../components/BirthDateInput';
import {validateRegistrationStep,uploadError,type RegistrationForm,type FieldErrors} from '../services/registrationValidation';
import './AuthBackground.css';
import {FormEvent,useEffect,useMemo,useState,type ReactNode,cloneElement,isValidElement,type ReactElement} from 'react';
import {useNavigate} from 'react-router-dom';
import {api,Plan} from '../services/api';
import {useAuth} from '../context/AuthContext';
import {ArrowLeft,ArrowRight,CheckCircle2,FileText,Home,IdCard,LockKeyhole,Save,Upload,UserRound} from 'lucide-react';

type DistrictOption={name:string;code:string};
type DraftView={
  draftToken:string;lastCompletedStep:number;fullName?:string;registrationSource?:'DIRECT'|'MEMBER_REFERENCE';memberReferenceId?:string;gender?:string;profession?:string;gotram?:string;
  dateOfBirth?:string;aadhaarMasked?:string;bloodGroup?:string;phoneNumber?:string;whatsappNumber?:string;doorNumber?:string;cityTown?:string;
  constituency?:string;district?:string;districtCode?:string;pinCode?:string;state?:string;aadhaarFrontName?:string;aadhaarBackName?:string;passportPhotoName?:string;
  planCode?:'PREMIUM'|'ELITE';
};

const DISTRICTS:Record<string,DistrictOption[]>={
  'Andhra Pradesh':[
    ['Alluri Sitharama Raju','ASR'],['Anakapalli','AKP'],['Ananthapuramu','ATP'],['Annamayya','ANN'],['Bapatla','BAP'],['Chittoor','CTR'],
    ['Dr. B. R. Ambedkar Konaseema','KON'],['East Godavari','EGD'],['Eluru','ELR'],['Guntur','GNT'],['Kakinada','KAK'],['Krishna','KRI'],
    ['Kurnool','KUR'],['Nandyal','NDY'],['NTR','NTR'],['Palnadu','PLN'],['Parvathipuram Manyam','PVM'],['Prakasam','PKM'],['Sri Potti Sriramulu Nellore','NLR'],
    ['Sri Sathya Sai','SSS'],['Srikakulam','SKL'],['Tirupati','TPT'],['Visakhapatnam','VSP'],['Vizianagaram','VZM'],['West Godavari','WGD'],['YSR Kadapa','KDP']
  ].map(([name,code])=>({name,code})),
  'Telangana':[
    ['Adilabad','ADB'],['Bhadradri Kothagudem','BKG'],['Hanumakonda','HNK'],['Hyderabad','HYD'],['Jagtial','JGL'],['Jangaon','JGN'],['Jayashankar Bhupalpally','JSB'],
    ['Jogulamba Gadwal','JOG'],['Kamareddy','KMR'],['Karimnagar','KRM'],['Khammam','KHM'],['Komaram Bheem Asifabad','KBA'],['Mahabubabad','MBD'],
    ['Mahabubnagar','MBN'],['Mancherial','MCL'],['Medak','MDK'],['Medchal–Malkajgiri','MCM'],['Mulugu','MLG'],['Nagarkurnool','NGK'],['Nalgonda','NLG'],
    ['Narayanpet','NRP'],['Nirmal','NRM'],['Nizamabad','NZB'],['Peddapalli','PDL'],['Rajanna Sircilla','RJS'],['Rangareddy','RRD'],['Sangareddy','SGR'],
    ['Siddipet','SDP'],['Suryapet','SRP'],['Vikarabad','VKB'],['Wanaparthy','WNP'],['Warangal','WRG'],['Yadadri Bhuvanagiri','YBG']
  ].map(([name,code])=>({name,code}))
};

const initial:RegistrationForm={
  fullName:'',registrationSource:'DIRECT',memberReferenceId:'',gender:'',profession:'',gotram:'',dateOfBirth:'',aadhaarNumber:'',bloodGroup:'',
  phoneNumber:'',whatsappNumber:'',doorNumber:'',cityTown:'',constituency:'',district:'',pinCode:'',state:'',password:'',confirmPassword:'',planCode:''
};
const steps=[['01','వ్యక్తిగత వివరాలు'],['02','చిరునామా'],['03','పత్రాలు'],['04','సభ్యత్వం'],['05','సమీక్ష & సమర్పణ']] as const;
const DRAFT_KEY='purohit_registration_draft_token';

export default function Register(){
  const nav=useNavigate(),auth=useAuth();
  const [f,setF]=useState<RegistrationForm>(initial);
  const [step,setStep]=useState(1);
  const [fieldErrors,setFieldErrors]=useState<FieldErrors>({});
  const [plans,setPlans]=useState<Plan[]>([]);
  const [draftToken,setDraftToken]=useState<string|null>(localStorage.getItem(DRAFT_KEY));
  const [aadhaarSaved,setAadhaarSaved]=useState(false);
  const [aadhaarMasked,setAadhaarMasked]=useState('');
  const [aadhaarFront,setAadhaarFront]=useState<File|null>(null),[aadhaarBack,setAadhaarBack]=useState<File|null>(null),[passportPhoto,setPassportPhoto]=useState<File|null>(null);
  const [savedDocs,setSavedDocs]=useState({aadhaarFrontName:'',aadhaarBackName:'',passportPhotoName:''});
  const [declarationChecks,setDeclarationChecks]=useState<boolean[]>(()=>Array(6).fill(false));
  const [err,setErr]=useState(''),[notice,setNotice]=useState(''),[saving,setSaving]=useState(false),[loadingDraft,setLoadingDraft]=useState(!!draftToken);

  useEffect(()=>{api.get('/public/plans').then(r=>setPlans(r.data)).catch(()=>setErr('సభ్యత్వ పథకాలు లోడ్ కాలేదు. బ్యాక్‌ఎండ్ నడుస్తుందో చూడండి.'))},[]);
  useEffect(()=>{if(!draftToken){setLoadingDraft(false);return} api.get(`/registration/drafts/${draftToken}`).then(r=>hydrate(r.data)).catch(()=>{localStorage.removeItem(DRAFT_KEY);setDraftToken(null)}).finally(()=>setLoadingDraft(false))},[]);
  const districtOptions=useMemo(()=>DISTRICTS[f.state]||[],[f.state]);
  const set=(k:keyof RegistrationForm,v:any)=>{setF(x=>({...x,[k]:v}));setFieldErrors(errors=>({...errors,[k.replace(/[A-Z]/g,c=>'-'+c.toLowerCase())]:''}))};
  const goStep=(n:number)=>{setErr('');setFieldErrors({});setNotice('');setStep(n);window.scrollTo({top:0,behavior:'smooth'})};

  function hydrate(d:DraftView){
    setF(x=>({...x,
      fullName:d.fullName||'',registrationSource:d.registrationSource||'DIRECT',memberReferenceId:d.memberReferenceId||'',gender:d.gender||'',profession:d.profession||'',gotram:d.gotram||'',dateOfBirth:d.dateOfBirth||'',bloodGroup:d.bloodGroup||'',
      phoneNumber:d.phoneNumber||'',whatsappNumber:d.whatsappNumber||'',doorNumber:d.doorNumber||'',cityTown:d.cityTown||'',constituency:d.constituency||'',district:d.district||'',pinCode:d.pinCode||'',state:d.state||'',planCode:d.planCode||''
    }));
    if(d.aadhaarMasked){setAadhaarSaved(true);setAadhaarMasked(d.aadhaarMasked)}
    setSavedDocs({aadhaarFrontName:d.aadhaarFrontName||'',aadhaarBackName:d.aadhaarBackName||'',passportPhotoName:d.passportPhotoName||''});
    setStep(Math.min(5,(d.lastCompletedStep||0)+1));
    setNotice('మునుపటి డ్రాఫ్ట్ వివరాలు తిరిగి లోడ్ అయ్యాయి.');
  }

  function validateStep(current=step){
    const errors=validateRegistrationStep(f,current,{aadhaarSaved,districtNames:districtOptions.map(d=>d.name),files:[aadhaarFront,aadhaarBack,passportPhoto],savedFiles:[savedDocs.aadhaarFrontName,savedDocs.aadhaarBackName,savedDocs.passportPhotoName],declarations:declarationChecks});
    setFieldErrors(errors);
    const first=Object.keys(errors)[0];
    if(first)requestAnimationFrame(()=>document.querySelector<HTMLElement>('[data-testid="'+first+'"]')?.focus());
    return Object.values(errors)[0]||'';
  }

  async function saveStep(current=step){
    const message=validateStep(current); if(message){setErr(message);return false}
    setSaving(true);setErr('');setNotice('');
    try{
      let d:DraftView;
      if(current===1){
        const body={fullName:f.fullName,registrationSource:f.registrationSource,memberReferenceId:f.registrationSource==='MEMBER_REFERENCE'?f.memberReferenceId:'',gender:f.gender,profession:f.profession,gotram:f.gotram,dateOfBirth:f.dateOfBirth,aadhaarNumber:f.aadhaarNumber||'__KEEP__',bloodGroup:f.bloodGroup};
        if(!draftToken){const r=await api.post('/registration/drafts/start',body);d=r.data;setDraftToken(d.draftToken);localStorage.setItem(DRAFT_KEY,d.draftToken)}
        else d=(await api.put(`/registration/drafts/${draftToken}/step/1`,body)).data;
        if(f.aadhaarNumber){setAadhaarSaved(true);setAadhaarMasked('XXXX XXXX '+f.aadhaarNumber.slice(-4));set('aadhaarNumber','')}
      } else {
        if(!draftToken) throw new Error('ముందుగా వ్యక్తిగత వివరాలను సేవ్ చేయండి.');
        if(current===2){
          d=(await api.put(`/registration/drafts/${draftToken}/step/2`,{phoneNumber:f.phoneNumber,whatsappNumber:f.whatsappNumber,doorNumber:f.doorNumber,cityTown:f.cityTown,constituency:f.constituency,district:f.district,pinCode:f.pinCode,state:f.state})).data;
        }else if(current===3){
          const body=new FormData(); if(aadhaarFront)body.append('aadhaarFront',aadhaarFront);if(aadhaarBack)body.append('aadhaarBack',aadhaarBack);if(passportPhoto)body.append('passportPhoto',passportPhoto);
          d=(await api.put(`/registration/drafts/${draftToken}/step/3`,body)).data;
          setSavedDocs({aadhaarFrontName:d.aadhaarFrontName||savedDocs.aadhaarFrontName,aadhaarBackName:d.aadhaarBackName||savedDocs.aadhaarBackName,passportPhotoName:d.passportPhotoName||savedDocs.passportPhotoName});
          setAadhaarFront(null);setAadhaarBack(null);setPassportPhoto(null);
        }else if(current===4){d=(await api.put(`/registration/drafts/${draftToken}/step/4`,{planCode:f.planCode})).data}else return true;
      }
      setNotice('✓ వివరాలు సేవ్ చేయబడ్డాయి'); return true;
    }catch(x:any){setErr((x.response?.data?.errors?Object.values(x.response.data.errors).join(' '):x.response?.data?.message)||x.message||'వివరాలు సేవ్ కాలేదు. మళ్లీ ప్రయత్నించండి.');return false}finally{setSaving(false)}
  }

  async function next(){if(await saveStep(step))goStep(Math.min(5,step+1))}
  function back(){goStep(Math.max(1,step-1))}
  async function saveAndExit(){if(step<5 && !(await saveStep(step)))return;setNotice('✓ డ్రాఫ్ట్ సేవ్ చేయబడింది. మీరు తరువాత ఇదే బ్రౌజర్‌లో కొనసాగించవచ్చు.');setTimeout(()=>nav('/'),500)}

  async function submit(e:FormEvent){
    e.preventDefault();if(saving)return;setErr('');
    if(step<5){await next();return}
    for(let current=1;current<=5;current++){const message=validateStep(current);if(message){setStep(current);setErr(message);return}}
    if(!draftToken){setErr('డ్రాఫ్ట్ కనబడలేదు. దయచేసి మొదటి దశ నుండి సేవ్ చేయండి.');return}
    setSaving(true);
    try{
      const {data}=await api.post(`/registration/drafts/${draftToken}/confirm`,{password:f.password,declarationAccepted:true});
      localStorage.removeItem(DRAFT_KEY);setDraftToken(null);auth.login(data.token,data.role,data.loginId);nav('/dashboard',{state:{newMemberNumber:data.memberNumber}});
    }catch(x:any){setErr((x.response?.data?.errors?Object.values(x.response.data.errors).join(' '):x.response?.data?.message)||'నమోదు పూర్తి కాలేదు. దయచేసి వివరాలను తనిఖీ చేయండి.')}finally{setSaving(false)}
  }

  if(loadingDraft)return <section className="wizard-page auth-temple"><div className="wizard-card"><p>సేవ్ చేసిన నమోదు వివరాలు లోడ్ అవుతున్నాయి...</p></div></section>;

  return <section className="wizard-page auth-temple">
    <div className="wizard-heading"><span>కొత్త సభ్యత్వ నమోదు</span><h1>సభ్యత్వ దరఖాస్తు</h1><p>ప్రతి దశలో <b>తదుపరి</b> నొక్కినప్పుడు వివరాలు డేటాబేస్‌లో డ్రాఫ్ట్‌గా సేవ్ అవుతాయి. మధ్యలో నిష్క్రమించినా మళ్లీ ఇదే బ్రౌజర్‌లో కొనసాగించవచ్చు.</p></div>
    <div className="wizard-rail">{steps.map(([n,label],i)=>{const s=i+1;return <button type="button" key={n} className={(s===step?'active ':'')+(s<step?'complete':'')} onClick={()=>s<step&&goStep(s)}><b>{s<step?'✓':n}</b><span>{label}</span></button>})}</div>

    <form className="wizard-shell" onSubmit={submit}>
      {err&&<div className="wizard-error" role="alert">{err}</div>}{notice&&<div className="wizard-success">{notice}</div>}

      {step===1&&<section className="wizard-card">
        <Title icon={<UserRound/>} telugu="వ్యక్తిగత వివరాలు" english="Personal Details" step="దశ 1 / 5"/>
        <div className="registration-fields">
          <Field n="01" label="పూర్తి పేరు *" error={fieldErrors['full-name']}><input data-testid="full-name" maxLength={140} value={f.fullName} onChange={e=>set('fullName',e.target.value)} placeholder="మీ పూర్తి పేరు నమోదు చేయండి"/></Field>
          <div className="reg-field span-2"><i>02</i><span>నమోదు విధానం *</span><div className="source-choice"><label><input data-testid="source-direct" type="radio" checked={f.registrationSource==='DIRECT'} onChange={()=>setF(x=>({...x,registrationSource:'DIRECT',memberReferenceId:''}))}/> ప్రత్యక్ష నమోదు / Direct Registration</label><label><input data-testid="source-reference" type="radio" checked={f.registrationSource==='MEMBER_REFERENCE'} onChange={()=>set('registrationSource','MEMBER_REFERENCE')}/> సభ్యుని సూచనతో / Existing Member Reference</label></div></div>
          {f.registrationSource==='MEMBER_REFERENCE'&&<Field n="03" label="Member Reference ID *" error={fieldErrors['member-reference-id']}><input data-testid="member-reference-id" maxLength={40} value={f.memberReferenceId} onChange={e=>set('memberReferenceId',e.target.value.toUpperCase())} placeholder="ఉదా: TS-HYD-000001"/></Field>}
          <Field n="03" label="లింగం *" error={fieldErrors['gender']}><select data-testid="gender" value={f.gender} onChange={e=>set('gender',e.target.value)}><option value="">లింగాన్ని ఎంచుకోండి</option><option value="MALE">పురుషుడు</option><option value="FEMALE">స్త్రీ</option><option value="OTHER">ఇతర</option></select></Field>
          <Field n="04" label="వృత్తి *" error={fieldErrors['profession']}><input data-testid="profession" maxLength={120} value={f.profession} onChange={e=>set('profession',e.target.value)} placeholder="మీ వృత్తిని నమోదు చేయండి"/></Field>
          <Field n="05" label="గోత్రం *" error={fieldErrors['gotram']}><input data-testid="gotram" maxLength={120} value={f.gotram} onChange={e=>set('gotram',e.target.value)} placeholder="మీ గోత్రం నమోదు చేయండి"/></Field>
          <Field n="06" label="పుట్టిన తేదీ *" error={fieldErrors['date-of-birth']}><BirthDateInput data-testid="date-of-birth" value={f.dateOfBirth} onChange={value=>set('dateOfBirth',value)} onError={message=>setFieldErrors(errors=>({...errors,'date-of-birth':message}))}/></Field>
          <Field n="07" label="ఆధార్ సంఖ్య *" error={fieldErrors['aadhaar-number']}><input data-testid="aadhaar-number" inputMode="numeric" maxLength={12} value={f.aadhaarNumber} onChange={e=>set('aadhaarNumber',e.target.value.replace(/\D/g,''))} placeholder={aadhaarSaved?`${aadhaarMasked} — మార్చాలంటే కొత్త సంఖ్య నమోదు చేయండి`:'12 అంకెల ఆధార్ సంఖ్య'}/></Field>
          <Field n="08" label="రక్త గ్రూప్ *" error={fieldErrors['blood-group']}><select data-testid="blood-group" value={f.bloodGroup} onChange={e=>set('bloodGroup',e.target.value)}><option value="">రక్త గ్రూప్ ఎంచుకోండి</option>{['A+','A-','B+','B-','AB+','AB-','O+','O-'].map(x=><option key={x}>{x}</option>)}</select></Field>
        </div><StepActions first next={next} saveExit={saveAndExit} saving={saving}/>
      </section>}

      {step===2&&<section className="wizard-card">
        <Title icon={<Home/>} telugu="చిరునామా & సంప్రదింపు వివరాలు" english="Address & Contact Details" step="దశ 2 / 5"/>
        <div className="registration-fields">
          <Field n="01" label="మొబైల్ నంబర్ *" error={fieldErrors['phone-number']}><input data-testid="phone-number" maxLength={25} inputMode="tel" value={f.phoneNumber} onChange={e=>set('phoneNumber',e.target.value.replace(/[^0-9+ -]/g,''))} placeholder="Login కోసం మొబైల్ నంబర్"/></Field>
          <Field n="02" label="వాట్సాప్ సంఖ్య *" error={fieldErrors['whatsapp-number']}><input data-testid="whatsapp-number" maxLength={25} value={f.whatsappNumber} onChange={e=>set('whatsappNumber',e.target.value)} placeholder="WhatsApp number"/></Field>
          <Field n="03" label="డోర్ నంబర్ *" error={fieldErrors['door-number']}><input data-testid="door-number" maxLength={80} value={f.doorNumber} onChange={e=>set('doorNumber',e.target.value)} placeholder="H.No / Flat No"/></Field>
          <Field n="04" label="నగరం / పట్టణం *" error={fieldErrors['city-town']}><input data-testid="city-town" maxLength={100} value={f.cityTown} onChange={e=>set('cityTown',e.target.value)} placeholder="నగరం / పట్టణం"/></Field>
          <Field n="05" label="నియోజకవర్గం *" error={fieldErrors['constituency']}><input data-testid="constituency" maxLength={120} value={f.constituency} onChange={e=>set('constituency',e.target.value)} placeholder="నియోజకవర్గం"/></Field>
          <Field n="06" label="రాష్ట్రం *" error={fieldErrors['state']}><select data-testid="state" value={f.state} onChange={e=>setF(x=>({...x,state:e.target.value,district:''}))}><option value="">రాష్ట్రం ఎంచుకోండి</option><option>Andhra Pradesh</option><option>Telangana</option></select></Field>
          <Field n="07" label="జిల్లా *" error={fieldErrors['district']}><select data-testid="district" value={f.district} disabled={!f.state} onChange={e=>set('district',e.target.value)}><option value="">జిల్లా ఎంచుకోండి</option>{districtOptions.map(d=><option key={d.code} value={d.name}>{d.name}</option>)}</select></Field>
          <Field n="08" label="పిన్ కోడ్ *" error={fieldErrors['pin-code']}><input data-testid="pin-code" inputMode="numeric" maxLength={6} value={f.pinCode} onChange={e=>set('pinCode',e.target.value.replace(/\D/g,''))} placeholder="6 అంకెల PIN కోడ్"/></Field>
        </div><StepActions back={back} next={next} saveExit={saveAndExit} saving={saving}/>
      </section>}

      {step===3&&<section className="wizard-card">
        <Title icon={<FileText/>} telugu="పత్రాలు" english="Documents" step="దశ 3 / 5"/>
        <div className="upload-list">
          <UploadField n="01" label="ఆధార్ కార్డు — ముందు వైపు *" file={aadhaarFront} savedName={savedDocs.aadhaarFrontName} accept=".pdf,.jpg,.jpeg,.png" setFile={setAadhaarFront}/>
          <UploadField n="02" label="ఆధార్ కార్డు — వెనుక వైపు *" file={aadhaarBack} savedName={savedDocs.aadhaarBackName} accept=".pdf,.jpg,.jpeg,.png" setFile={setAadhaarBack}/>
          <UploadField n="03" label="పాస్‌పోర్ట్ ఫోటో *" file={passportPhoto} savedName={savedDocs.passportPhotoName} accept=".jpg,.jpeg,.png" setFile={setPassportPhoto}/>
        </div><StepActions back={back} next={next} saveExit={saveAndExit} saving={saving}/>
      </section>}

      {step===4&&<section className="wizard-card">
        <Title icon={<IdCard/>} telugu="సభ్యత్వం ఎంచుకోండి" english="Membership Plan" step="దశ 4 / 5"/>
        <div className="registration-plan-grid">{plans.map(p=><label key={p.code} className={'registration-plan '+(f.planCode===p.code?'selected ':'')+(p.code==='ELITE'?'elite-option':'')}><input data-testid={`plan-${p.code.toLowerCase()}`} type="radio" name="plan" value={p.code} checked={f.planCode===p.code} onChange={()=>set('planCode',p.code)}/><span className="plan-icon">{p.code==='ELITE'?'★':'♛'}</span><h3>{p.nameTelugu}</h3><strong>₹{Number(p.price).toLocaleString('en-IN')}</strong><small>{p.validityMonths} నెలల సభ్యత్వం</small><span className="choose"><CheckCircle2/> ఈ సభ్యత్వాన్ని ఎంచుకోండి</span></label>)}</div>
        <StepActions back={back} next={next} saveExit={saveAndExit} saving={saving}/>
      </section>}

      {step===5&&<section className="wizard-card">
        <Title icon={<LockKeyhole/>} telugu="సమీక్ష & సమర్పణ" english="Review & Submit" step="దశ 5 / 5"/>
        <div className="review-grid">
          <Review title="వ్యక్తిగత వివరాలు" edit={()=>goStep(1)} rows={[["పేరు",f.fullName],["Registration",f.registrationSource==='DIRECT'?'Direct Registration':'Member Reference'],["Reference ID",f.memberReferenceId||'Not Applicable'],["లింగం",f.gender],["గోత్రం",f.gotram],["వృత్తి",f.profession],["పుట్టిన తేదీ",f.dateOfBirth],["ఆధార్",aadhaarMasked||'సేవ్ చేయబడింది'],["రక్త గ్రూప్",f.bloodGroup]]}/>
          <Review title="చిరునామా & సంప్రదింపు" edit={()=>goStep(2)} rows={[["మొబైల్",f.phoneNumber],["WhatsApp",f.whatsappNumber],["డోర్ నంబర్",f.doorNumber],["నగరం / పట్టణం",f.cityTown],["నియోజకవర్గం",f.constituency],["జిల్లా",f.district],["రాష్ట్రం",f.state],["PIN",f.pinCode]]}/>
          <Review title="పత్రాలు" edit={()=>goStep(3)} rows={[["ఆధార్ ముందు",savedDocs.aadhaarFrontName||'Uploaded'],["ఆధార్ వెనుక",savedDocs.aadhaarBackName||'Uploaded'],["పాస్‌పోర్ట్ ఫోటో",savedDocs.passportPhotoName||'Uploaded']]}/>
          <Review title="సభ్యత్వం" edit={()=>goStep(4)} rows={[["పథకం",f.planCode||'—']]}/>
        </div>
        <div className="account-block"><h3>లాగిన్ వివరాలు</h3><p className="muted">నమోదు పూర్తయిన తర్వాత మొబైల్ నంబర్ + పాస్‌వర్డ్‌తో login చేయవచ్చు. Admin approval తర్వాత Generated Member ID + అదే పాస్‌వర్డ్‌తో కూడా login చేయవచ్చు.</p><div className="registration-fields compact"><Field n="01" label="పాస్‌వర్డ్ *" error={fieldErrors['password']}><input data-testid="password" maxLength={72} type="password" minLength={8} value={f.password} onChange={e=>set('password',e.target.value)} autoComplete="new-password"/></Field><Field n="02" label="పాస్‌వర్డ్ నిర్ధారించండి *" error={fieldErrors['confirm-password']}><input data-testid="confirm-password" maxLength={72} type="password" minLength={8} value={f.confirmPassword} onChange={e=>set('confirmPassword',e.target.value)} autoComplete="new-password"/></Field></div>
          <div className="declaration-panel"><h3>ప్రకటన / Declaration</h3><p>క్రింది ప్రతి అంశాన్ని పూర్తిగా చదివి అంగీకరించిన తర్వాత మాత్రమే నమోదు పూర్తి చేయండి.</p>{[
            'నేను బ్రాహ్మణ సామాజిక వర్గానికి చెందినవాడినై, పురోహిత వృత్తిలో ఉన్నాను.','అవి అవాస్తవమని తెలిసిన యెడల సమాఖ్య తీసుకునే చర్యలకు బాధ్యత వహించగలవాడను.','నేను సమాఖ్య నిర్ణయములు శిరసావహించి నియమ నిబంధనలకు కట్టుబడి ఉంటూ గుర్తింపు కార్డును దుర్వినియోగం చేయనని నిర్ధారణ చేయుచున్నాను.','సమాఖ్య యొక్క లక్ష్యాలకు ఆకర్షితుడినై నా పూర్తి సమ్మతముతో ఎటువంటి వత్తిడి లేదా ప్రమేయం లేకుండా నేను సభ్యుడిగా చేరుచున్నాను.','నా యొక్క వివరములు ఏవైనా ఉపాధి మరియు సమాఖ్య యొక్క సమాచార నిమిత్తమై ఉపయోగం కోసం నా పూర్తి అంగీకారం తెలుపుచున్నాను.','పైన తెలిపిన వివరాలు వాస్తవమని నిర్ధారణ చేయుచున్నాను.'
          ].map((text,i)=><label className="declaration-item" key={i}><input data-testid={`declaration-${i+1}`} type="checkbox" checked={declarationChecks[i]} onChange={e=>setDeclarationChecks(x=>x.map((v,idx)=>idx===i?e.target.checked:v))}/><span><b>{i+1}.</b> {text}</span></label>)}</div>
        </div>
        <div className="wizard-final-actions"><button type="button" className="btn btn-outline btn-lg" onClick={back}><ArrowLeft/> వెనుకకు</button><button type="button" className="btn btn-outline btn-lg" onClick={saveAndExit}><Save/> సేవ్ చేసి నిష్క్రమించండి</button><button data-testid="confirm-submit" type="submit" className="btn btn-primary btn-lg" disabled={saving}>{saving?'సమర్పిస్తోంది...':'నిర్ధారించి సమర్పించండి'} <CheckCircle2/></button></div>
      </section>}
    </form>
  </section>
}

function Title({icon,telugu,english,step}:{icon:ReactNode;telugu:string;english:string;step:string}){return <div className="registration-card-title wizard-title"><span className="wizard-title-icon">{icon}</span><div><h2>{telugu}</h2><p>{english}</p></div><strong>{step}</strong></div>}
function StepActions({first=false,back,next,saveExit,saving}:{first?:boolean;back?:()=>void;next:()=>void;saveExit:()=>void;saving:boolean}){return <div className="wizard-actions">{first?<span/>:<button type="button" className="btn btn-outline btn-lg" onClick={back}><ArrowLeft/> వెనుకకు</button>}<div className="wizard-action-right"><button data-testid="save-exit" type="button" className="btn btn-outline btn-lg" onClick={saveExit} disabled={saving}><Save/> సేవ్ చేసి నిష్క్రమించండి</button><button data-testid="next-step" type="button" className="btn btn-primary btn-lg" onClick={next} disabled={saving}>{saving?'సేవ్ అవుతోంది...':'తదుపరి'} <ArrowRight/></button></div></div>}
function Field({n,label,children,error}:{n:string;label:string;children:ReactNode;error?:string}){
  const child=isValidElement(children)?children as ReactElement<any>:null;
  const id=child?.props['data-testid']||'field-'+n;
  return <label className="reg-field"><i>{n}</i><span>{label}</span>{child?cloneElement(child,{'aria-invalid':!!error,'aria-describedby':error?id+'-error':undefined}):children}{error&&<small id={id+'-error'} className="field-error">{error}</small>}</label>;
}
function UploadField({n,label,file,savedName,accept,setFile}:{n:string;label:string;file:File|null;savedName:string;accept:string;setFile:(f:File|null)=>void}){
  const error=file?uploadError(file,n==='03'):'';
  return <div className="upload-field"><i>{n}</i><div><b>{label}</b><label className="upload-box"><Upload/><span>{file?file.name:(savedName?'Saved: '+savedName:'Choose a file')}</span><input data-testid={'upload-'+n} type="file" accept={accept} aria-invalid={!!error} aria-describedby={error?'upload-'+n+'-error':undefined} onChange={e=>setFile(e.target.files?.[0]||null)}/></label><small>{n==='03'?'JPG/PNG':'PDF/JPG/PNG'} · Maximum 5 MB</small>{error&&<small className="field-error" id={'upload-'+n+'-error'}>{error}</small>}</div></div>;
}
function Review({title,rows,edit}:{title:string;rows:[string,string][];edit:()=>void}){return <section className="review-card"><header><h3>{title}</h3><button type="button" onClick={edit}>సవరించండి</button></header><dl>{rows.map(([k,v])=><div key={k}><dt>{k}</dt><dd>{v||'—'}</dd></div>)}</dl></section>}
