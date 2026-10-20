import {useState} from 'react';
import {api} from '../services/api';

export default function AdminDeleteMemberButton({member,onDeleted}:{member:{id:number;fullNameTelugu?:string;fullNameEnglish?:string};onDeleted:(id:number)=>void}){
  const [busy,setBusy]=useState(false);
  const [error,setError]=useState('');
  async function remove(){
    if(!window.confirm(`Permanently delete ${member.fullNameTelugu||member.fullNameEnglish||'this member'} and their application, login account, membership, ID card and activity registrations? This cannot be undone.`)) return;
    setBusy(true);setError('');
    try{await api.delete(`/admin/members/${member.id}`);onDeleted(member.id)}
    catch(e:any){setError(e.response?.data?.message||'Could not delete member. Please try again.')}
    finally{setBusy(false)}
  }
  return <><button type="button" className="btn" disabled={busy} onClick={remove}>{busy?'Deleting…':'Delete'}</button>{error&&<span role="alert" className="alert error">{error}</span>}</>;
}
