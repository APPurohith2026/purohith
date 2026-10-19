export default function OrganizationBrand({ className = '' }: { className?: string }) {
  return <span className={`organization-brand ${className}`}><img className="brand-logo" src="/images/organization-logo-transparent.png" alt="" width="64" height="64"/><span className="organization-name"><b lang="te">ఆంధ్రప్రదేశ్<br/>బ్రాహ్మణ పురోహిత సమాఖ్య</b><small lang="en">AP BRAHMANA PUROHIT SAMAKYA</small></span></span>;
}
