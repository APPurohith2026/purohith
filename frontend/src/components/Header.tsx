import ChantToggle from './ChantToggle';
import OrganizationBrand from './OrganizationBrand';
import { Link, NavLink } from 'react-router-dom';
import { Menu, UserRound } from 'lucide-react';
import { useState } from 'react';
import { useAuth } from '../context/AuthContext';
export default function Header(){const [open,setOpen]=useState(false);const {token,role,logout}=useAuth();return <header className="header"><Link className="brand" to="/"><OrganizationBrand/></Link><ChantToggle/><button className="menu-btn" onClick={()=>setOpen(!open)} aria-label="Menu"><Menu/></button><nav className={open?'nav open':'nav'}><NavLink to="/">హోమ్</NavLink><NavLink to="/membership">సభ్యత్వం</NavLink><NavLink to="/activities">కార్యక్రమాలు</NavLink>{token&&role==='MEMBER'&&<NavLink to="/dashboard">డ్యాష్‌బోర్డ్</NavLink>}{token&&role==='ADMIN'&&<NavLink to="/admin">అడ్మిన్</NavLink>}{!token?<Link className="btn btn-primary" to="/login"><UserRound size={17}/> లాగిన్</Link>:<button className="btn btn-outline" onClick={logout}>లాగౌట్</button>}</nav></header>}
