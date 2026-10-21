import { Outlet,useLocation } from 'react-router-dom';
import {useCallback,useState} from 'react';
import InaugurationReveal,{shouldShowInauguration,markInaugurationSeen} from './InaugurationReveal';
import Header from './Header';
import OrganizationBrand from './OrganizationBrand';
export default function Layout(){const location=useLocation();const [reveal,setReveal]=useState(()=>shouldShowInauguration(location.pathname));const closeReveal=useCallback(()=>{markInaugurationSeen();setReveal(false)},[]);if(reveal&&location.pathname==='/')return <InaugurationReveal onClose={closeReveal}/>;return <><Header/><main className="site-main"><Outlet/></main><footer className="footer"><div><OrganizationBrand/><p>సంప్రదాయం • సేవ • సమైక్యత • జ్ఞానం</p><p>Telugu-first community membership platform</p></div><section className="footer-contact" aria-labelledby="contact-heading"><h2 id="contact-heading">Contact Us</h2><address><p>Phone: <a href="tel:+919440457161">+91 94404 57161</a></p><p>Email: <a href="mailto:apbrahmanapurohithsamakya@gmail.com">apbrahmanapurohithsamakya@gmail.com</a></p></address></section></footer></>}
