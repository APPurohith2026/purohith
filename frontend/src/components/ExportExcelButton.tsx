import {useState} from 'react';
import {Download} from 'lucide-react';
import {downloadExcel,type ExportSheet} from '../services/excelExport';
import './ExportExcelButton.css';

export default function ExportExcelButton({filename,loadSheets,disabled=false}:{filename:string;loadSheets:()=>ExportSheet[]|Promise<ExportSheet[]>;disabled?:boolean}){
  const [busy,setBusy]=useState(false),[error,setError]=useState('');
  async function exportFile(){
    if(busy||disabled)return;
    setBusy(true);setError('');
    try{await downloadExcel(filename,await loadSheets())}
    catch{setError('Excel export failed. Please try again. If your session expired, log in again.')}
    finally{setBusy(false)}
  }
  return <div className="excel-export"><button type="button" className="btn btn-outline" onClick={()=>void exportFile()} disabled={busy||disabled}><Download size={18}/>{busy?'Exporting…':'Export Excel'}</button>{error&&<small role="alert">{error}</small>}</div>;
}
