export type ExportSheet={name:string;headers:string[];rows:(string|number)[][]};
export type ExportMember={
  memberNumber?:string;fullNameEnglish?:string;fullNameTelugu?:string;phoneNumber?:string;whatsappNumber?:string;dateOfBirth?:string;
  gender?:string;profession?:string;gotram?:string;bloodGroup?:string;doorNumber?:string;cityTown?:string;constituency?:string;
  district?:string;state?:string;pinCode?:string;registrationSource?:string;referrerMemberNumber?:string;referrerName?:string;
  planCode?:string;applicationStatus?:string;membershipStatus?:string;expiryDate?:string;createdAt?:string;
};
const value=(text:string|undefined)=>text??'';
const status=(text:string|undefined)=>value(text).replace(/_/g,' ');
const source=(text:string|undefined)=>({MEMBER_REFERENCE:'Member Reference',ADMIN_CREATED:'Admin Created',IMPORTED:'Imported',DIRECT:'Direct'}[text??'DIRECT']??value(text));

export function memberSheet(name:string,members:ExportMember[]):ExportSheet{
  if(!Array.isArray(members))throw new Error('Invalid member response');
  return {name,headers:['#','Member ID','Name (Telugu)','Name (English)','Phone','WhatsApp','Date of Birth','Gender','Profession','Gotram','Blood Group','Door / Flat Number','City / Town','Constituency','District','State','PIN Code','Registration Source','Reference Member ID','Reference Member Name','Plan','Application Status','Membership Status','Valid Until','Submitted At'],
    rows:members.map((m,index)=>[index+1,value(m.memberNumber),value(m.fullNameTelugu),value(m.fullNameEnglish),value(m.phoneNumber),value(m.whatsappNumber),value(m.dateOfBirth),value(m.gender),value(m.profession),value(m.gotram),value(m.bloodGroup),value(m.doorNumber),value(m.cityTown),value(m.constituency),value(m.district),value(m.state),value(m.pinCode),source(m.registrationSource),value(m.referrerMemberNumber),value(m.referrerName),value(m.planCode),status(m.applicationStatus),status(m.membershipStatus),value(m.expiryDate),value(m.createdAt)])};
}

export function dashboardSheets(dashboard:{stats:Record<string,number>;recentApplications:ExportMember[]},members:ExportMember[]):ExportSheet[]{
  const metrics=[['Total Applications','totalApplications'],['Pending Review','pendingApplications'],['Approved Members','approvedMembers'],['Correction Required','correctionRequired'],['Active Members','activeMembers'],['Premium Members','premiumMembers'],['Elite Members','eliteMembers'],['Direct Registrations','directRegistrations'],['Referred Registrations','referredRegistrations'],['Saved Drafts','draftRegistrations'],['Published Activities','publishedActivities'],['Activity Enrollments','totalActivityEnrollments']];
  return [{name:'Dashboard Summary',headers:['Metric','Count'],rows:metrics.map(([label,key])=>[label,dashboard.stats[key]??0])},memberSheet('All Members and Applications',members),memberSheet('Recent Applications',dashboard.recentApplications)];
}

export async function downloadExcel(prefix:string,sheets:ExportSheet[]){
  const {default:ExcelJS}=await import('exceljs');
  const workbook=new ExcelJS.Workbook();
  workbook.creator='AP Brahmana Purohit Samakya';workbook.created=new Date();
  for(const sheet of sheets){
    const worksheet=workbook.addWorksheet(sheet.name,{views:[{state:'frozen',ySplit:1}]});
    worksheet.columns=sheet.headers.map((header,index)=>({header,key:String(index),width:header==='#'?7:Math.min(32,Math.max(18,header.length+3))}));
    for(const row of sheet.rows){
      // Explicit string values preserve phone numbers and never become formulas.
      worksheet.addRow(row.map(cell=>typeof cell==='number'?cell:String(cell)));
    }
    const header=worksheet.getRow(1);header.height=26;
    header.eachCell(cell=>{cell.font={bold:true,color:{argb:'FFFFFFFF'}};cell.fill={type:'pattern',pattern:'solid',fgColor:{argb:'FF8A3518'}};cell.alignment={vertical:'middle',wrapText:true}});
    worksheet.autoFilter={from:{row:1,column:1},to:{row:Math.max(1,worksheet.rowCount),column:sheet.headers.length}};
    worksheet.eachRow((row,index)=>{if(index>1)row.eachCell(cell=>{if(typeof cell.value==='string')cell.numFmt='@'})});
  }
  const buffer=await workbook.xlsx.writeBuffer();
  const url=URL.createObjectURL(new Blob([new Uint8Array(buffer)],{type:'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'}));
  const link=document.createElement('a');
  link.href=url;link.download=`${prefix}-${new Date().toISOString().slice(0,10)}.xlsx`;
  document.body.appendChild(link);link.click();link.remove();
  window.setTimeout(()=>URL.revokeObjectURL(url),1000);
}
