import {todayDate} from '../services/registrationValidation';

export default function BirthDateInput({value,onChange,onError,...props}:{value:string;onChange:(value:string)=>void;onError:(message:string)=>void;'aria-invalid'?:boolean;'aria-describedby'?:string}){
  return <input {...props} data-testid="date-of-birth" type="date" min="1000-01-01" max={todayDate()} value={value}
    onChange={e=>{
      const next=e.currentTarget.value;
      if(next&&!/^[0-9]{4}-[0-9]{2}-[0-9]{2}$/.test(next)){
        e.currentTarget.value=value;
        onError('The year must contain exactly four digits.');return;
      }
      onError('');onChange(next);
    }}/>
}
