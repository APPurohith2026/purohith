import {useEffect,useState} from 'react';
import {FileText} from 'lucide-react';
import {api} from '../services/api';
import './AdminApplicationDocuments.css';

type MemberDocuments={id:number;aadhaarFrontPath?:string;aadhaarBackPath?:string;passportPhotoPath?:string};

export default function AdminApplicationDocuments({member}:{member:MemberDocuments}){
  return <section className="admin-detail-card admin-documents"><header><FileText/><h2>Uploaded documents / పత్రాలు</h2></header>
    <div className="admin-document-grid">
      <DocumentPreview key={`${member.id}-front`} title="Aadhaar Front" path={`/admin/members/${member.id}/documents/aadhaar-front`} present={!!member.aadhaarFrontPath}/>
      <DocumentPreview key={`${member.id}-back`} title="Aadhaar Back" path={`/admin/members/${member.id}/documents/aadhaar-back`} present={!!member.aadhaarBackPath}/>
      <DocumentPreview key={`${member.id}-photo`} title="Passport Photo" path={`/admin/members/${member.id}/passport-photo`} present={!!member.passportPhotoPath}/>
    </div>
  </section>;
}

function DocumentPreview({title,path,present}:{title:string;path:string;present:boolean}){
  const [file,setFile]=useState<{url:string;type:string}|null>(null);
  const [error,setError]=useState(''),[retry,setRetry]=useState(0);
  useEffect(()=>{
    setFile(null);setError('');
    if(!present)return;
    const controller=new AbortController();let objectUrl:string|undefined;
    api.get(path,{responseType:'blob',signal:controller.signal}).then(({data})=>{
      if(controller.signal.aborted)return;
      const type=data.type.split(';')[0];
      if(!['image/jpeg','image/png','application/pdf'].includes(type))throw new Error('Unsupported file');
      objectUrl=URL.createObjectURL(data);setFile({url:objectUrl,type});
    }).catch(()=>{if(!controller.signal.aborted)setError(`${title} could not be loaded.`)});
    return ()=>{controller.abort();if(objectUrl)URL.revokeObjectURL(objectUrl)};
  },[path,present,title,retry]);
  return <article className="admin-document" aria-label={title}><h3>{title}</h3>
    {!present?<p>Not uploaded</p>:error?<><p role="alert">{error}</p><button type="button" className="btn btn-outline" onClick={()=>setRetry(x=>x+1)}>Retry {title}</button></>:!file?<p role="status">Loading {title}…</p>:<>
      {file.type==='application/pdf'?<iframe title={`${title} preview`} src={file.url}/>:<img alt={title} src={file.url}/>}
      <a className="table-link" href={file.url} target="_blank" rel="noopener noreferrer">Open {title}</a>
    </>}
  </article>;
}
