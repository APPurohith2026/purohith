import {FormEvent,useEffect,useRef,useState} from 'react';
import type {PDFDocumentProxy} from 'pdfjs-dist';
import workerUrl from 'pdfjs-dist/build/pdf.worker.min.mjs?url';

export default function AdminPdfPreview({blob,title}:{blob:Blob;title:string}){
  const canvas=useRef<HTMLCanvasElement>(null);
  const [document,setDocument]=useState<PDFDocumentProxy|null>(null);
  const [page,setPage]=useState(1),[rendered,setRendered]=useState(false);
  const [error,setError]=useState(''),[password,setPassword]=useState('');
  const [unlock,setUnlock]=useState<((password:string)=>void)|null>(null);
  const [passwordError,setPasswordError]=useState('');
  useEffect(()=>{
    let active=true;
    let task:ReturnType<typeof import('pdfjs-dist').getDocument>|undefined;
    setDocument(null);setPage(1);setError('');setUnlock(null);setPassword('');
    async function load(){
      try{
        const pdf=await import('pdfjs-dist');
        const data=new Uint8Array(await blob.arrayBuffer());
        if(!active)return;
        pdf.GlobalWorkerOptions.workerSrc=workerUrl;
        task=pdf.getDocument({data});
        task.onPassword=(callback:(password:string)=>void,reason:number)=>{
          if(!active)return;
          setPassword('');setPasswordError(reason===pdf.PasswordResponses.INCORRECT_PASSWORD?'Incorrect PDF password. Please try again.':'');
          setUnlock(()=>callback);
        };
        const result=await task.promise;
        if(active){setDocument(result);setUnlock(null);setPassword('');setPasswordError('')}
      }catch{if(active){setUnlock(null);setPassword('');setError('This PDF could not be read. It may be damaged or unsupported. Download the original to check it, or request a new upload.')}}
    }
    void load();
    return ()=>{active=false;void task?.destroy().catch(()=>{})};
  },[blob]);
  useEffect(()=>{
    if(!document||!canvas.current)return;
    let active=true;
    let render:ReturnType<Awaited<ReturnType<PDFDocumentProxy['getPage']>>['render']>|undefined;
    setRendered(false);
    async function draw(){
      try{
        const pdfPage=await document!.getPage(page);
        if(!active||!canvas.current)return;
        const viewport=pdfPage.getViewport({scale:1});
        const scaled=pdfPage.getViewport({scale:600/viewport.width});
        const element=canvas.current;
        element.width=Math.ceil(scaled.width);element.height=Math.ceil(scaled.height);
        render=pdfPage.render({canvas:element,viewport:scaled});
        await render.promise;
        if(active)setRendered(true);
      }catch{if(active)setError('This PDF page could not be displayed. Download the original to check it.')}
    }
    void draw();
    return ()=>{active=false;render?.cancel()};
  },[document,page]);
  function submit(e:FormEvent){e.preventDefault();const callback=unlock;setUnlock(null);setPasswordError('');setPassword('');callback?.(password)}
  return <div className="admin-pdf-preview">
    {error?<p role="alert">{error}</p>:unlock?<form className="admin-pdf-unlock" onSubmit={submit}>
      <p>This PDF is password-protected. Enter its password to preview it.</p>
      <label>{title} PDF password<input type="password" autoComplete="off" value={password} onChange={e=>setPassword(e.target.value)} required/></label>
      {passwordError&&<p role="alert">{passwordError}</p>}
      <button type="submit" className="btn btn-outline">Unlock {title}</button>
    </form>:<>
      {!rendered&&<p role="status">Loading {title} PDF…</p>}
      <canvas ref={canvas} role="img" aria-label={`${title} PDF page ${page}`} hidden={!rendered}/>
      {document&&<div className="admin-pdf-pages"><button type="button" className="btn btn-outline" disabled={page===1||!rendered} onClick={()=>setPage(p=>p-1)}>Previous</button><span>Page {page} of {document.numPages}</span><button type="button" className="btn btn-outline" disabled={page===document.numPages||!rendered} onClick={()=>setPage(p=>p+1)}>Next</button></div>}
    </>}
  </div>;
}
