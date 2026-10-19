import { createContext, useContext, useMemo, useState, ReactNode } from 'react';
type Auth={token:string|null;role:string|null;loginId:string|null;login:(token:string,role:string,loginId:string)=>void;logout:()=>void};
const C=createContext<Auth|null>(null);
export function AuthProvider({children}:{children:ReactNode}){const [token,setToken]=useState(localStorage.getItem('token'));const [role,setRole]=useState(localStorage.getItem('role'));const [loginId,setLoginId]=useState(localStorage.getItem('loginId'));const value=useMemo(()=>({token,role,loginId,login:(t:string,r:string,id:string)=>{localStorage.setItem('token',t);localStorage.setItem('role',r);localStorage.setItem('loginId',id);setToken(t);setRole(r);setLoginId(id)},logout:()=>{localStorage.clear();setToken(null);setRole(null);setLoginId(null)}}),[token,role,loginId]);return <C.Provider value={value}>{children}</C.Provider>}
export const useAuth=()=>{const v=useContext(C);if(!v)throw new Error('Auth missing');return v};
