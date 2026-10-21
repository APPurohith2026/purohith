export type RegistrationForm={
  fullName:string;registrationSource:'DIRECT'|'MEMBER_REFERENCE';memberReferenceId:string;gender:string;profession:string;gotram:string;dateOfBirth:string;
  aadhaarNumber:string;bloodGroup:string;phoneNumber:string;whatsappNumber:string;doorNumber:string;cityTown:string;constituency:string;district:string;pinCode:string;state:string;
  password:string;confirmPassword:string;planCode:'PREMIUM'|'ELITE'|'';
};
export type FieldErrors=Record<string,string>;
export function todayDate(){const now=new Date();return `${now.getFullYear()}-${String(now.getMonth()+1).padStart(2,'0')}-${String(now.getDate()).padStart(2,'0')}`}
export function validBirthDate(value:string){
  if(!/^[1-9][0-9]{3}-[0-9]{2}-[0-9]{2}$/.test(value)||value>todayDate())return false;
  const parsed=new Date(`${value}T00:00:00Z`);
  return !Number.isNaN(parsed.getTime())&&parsed.toISOString().slice(0,10)===value;
}
export function validPhone(value:string){return /^\+?[0-9][0-9 -]*[0-9]$/.test(value)&&value.length<=25&&/^[0-9]{10,15}$/.test(value.replace(/\D/g,''))}
export function uploadError(file:File,photo=false){
  if(file.size===0)return 'Choose a non-empty file.';
  if(file.size>5*1024*1024)return 'File must be 5 MB or smaller.';
  if(!(photo?['image/jpeg','image/png']:['image/jpeg','image/png','application/pdf']).includes(file.type))return photo?'Photo must be JPG or PNG.':'Document must be PDF, JPG or PNG.';
  return '';
}
export function validateRegistrationStep(f:RegistrationForm,step:number,options:{aadhaarSaved:boolean;districtNames:string[];files:(File|null)[];savedFiles:string[];declarations:boolean[];passwordSaved?:boolean}):FieldErrors{
  const errors:FieldErrors={};
  const text=(key:keyof RegistrationForm,id:string,label:string,max:number)=>{
    if(!f[key].trim())errors[id]=`${label} is required.`;
    else if(f[key].length>max)errors[id]=`${label} must be ${max} characters or fewer.`;
  };
  if(step===1){
    text('fullName','full-name','Full name',140);text('profession','profession','Profession',120);text('gotram','gotram','Gotram',120);
    if(!['MALE','FEMALE','OTHER'].includes(f.gender))errors.gender='Choose a gender.';
    if(!validBirthDate(f.dateOfBirth))errors['date-of-birth']='Enter a valid date of birth with a four-digit year, no later than today.';
    if((!options.aadhaarSaved||f.aadhaarNumber)&&!/^[0-9]{12}$/.test(f.aadhaarNumber))errors['aadhaar-number']='Aadhaar number must contain exactly 12 digits.';
    if(!['A+','A-','B+','B-','AB+','AB-','O+','O-'].includes(f.bloodGroup))errors['blood-group']='Choose a blood group.';
    if(f.registrationSource==='MEMBER_REFERENCE'&&!/^(AP|TS)-[A-Z0-9]+-[0-9]{6}$/.test(f.memberReferenceId.trim()))errors['member-reference-id']='Enter a valid Member Reference ID, such as AP-EGD-000001.';
  }
  if(step===2){
    if(!validPhone(f.phoneNumber))errors['phone-number']='Enter a valid phone number with 10 to 15 digits; an optional leading +, spaces and hyphens are allowed.';
    if(!validPhone(f.whatsappNumber))errors['whatsapp-number']='Enter a valid WhatsApp number with 10 to 15 digits.';
    text('doorNumber','door-number','Door / flat number',80);text('cityTown','city-town','City / town',100);text('constituency','constituency','Constituency',120);
    if(!['Andhra Pradesh','Telangana'].includes(f.state))errors.state='Choose a state.';
    if(!options.districtNames.includes(f.district))errors.district='Choose a district in the selected state.';
    if(!/^[1-9][0-9]{5}$/.test(f.pinCode))errors['pin-code']='PIN code must contain 6 digits and must not start with zero.';
  }
  if(step===3){
    options.files.forEach((file,index)=>{
      const key=`upload-0${index+1}`;
      if(file){const error=uploadError(file,index===2);if(error)errors[key]=error}
      else if(!options.savedFiles[index])errors[key]='Upload this required document.';
    });
  }
  if(step===4&&!['PREMIUM','ELITE'].includes(f.planCode))errors.plan='Choose a membership plan.';
  if((step===2||step===5)&&!options.passwordSaved){
    if(!f.password.trim()||f.password.length<8)errors.password='Password must contain at least 8 characters and cannot be blank.';
    else if(new TextEncoder().encode(f.password).length>72)errors.password='Password must not exceed 72 UTF-8 bytes.';
    if(f.password!==f.confirmPassword||!f.confirmPassword)errors['confirm-password']='Passwords must match.';
  }
  if(step===5){
    if(options.declarations.length!==6||!options.declarations.every(Boolean))errors.declarations='Read and accept all six declarations before submitting.';
  }
  return errors;
}
