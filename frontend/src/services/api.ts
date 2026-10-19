import axios from 'axios';
export const api=axios.create({baseURL:import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'});
api.interceptors.request.use(c=>{const t=localStorage.getItem('token');if(t)c.headers.Authorization=`Bearer ${t}`;return c});
export type Plan={id:number;code:'PREMIUM'|'ELITE';nameTelugu:string;nameEnglish:string;price:number;validityMonths:number};
export type Activity={id:number;titleTelugu:string;titleEnglish:string;descriptionTelugu?:string;descriptionEnglish?:string;venue?:string;startDateTime?:string;capacity?:number;allowedPlans:string[];eligible:boolean;registered:boolean};
