import { Link } from 'react-router-dom';
import { ArrowRight, BadgeCheck, CalendarDays, IdCard, Users } from 'lucide-react';
import './Home.css';

const benefits = [
  { icon: Users, title: 'సభ్యత్వ నమోదు', label: 'Community membership', description: 'Find your place in our community with Premium and Elite membership options.' },
  { icon: IdCard, title: 'డిజిటల్ ఐడి', label: 'Your digital identity', description: 'Receive a verified digital membership card once your application is approved.' },
  { icon: CalendarDays, title: 'కార్యక్రమాలు', label: 'Meaningful connections', description: 'Explore community activities and events available with your membership.' },
  { icon: BadgeCheck, title: 'ధృవీకరణ', label: 'Membership verification', description: 'Verify membership through a QR code while keeping personal details private.' },
];

export default function Home() {
  return (
    <div className="home-page">
      <section className="hero home-hero" aria-labelledby="home-title">
        <div className="home-hero-image" aria-hidden="true"><img src="/images/home-temple-background.jpeg" alt="" loading="eager" decoding="async"/></div>
        <div className="hero-content">
          <span className="home-location"><span /> ఆంధ్రప్రదేశ్</span>
          <p className="home-kicker">ROOTED IN TRADITION. UNITED IN PURPOSE.</p>
          <h1 id="home-title">పురోహిత<br />సమాజం</h1>
          <h2>PUROHIT COMMUNITY</h2>
          <div className="home-gold-rule" />
          <p className="telugu-lead">సంప్రదాయం • సేవ • సమైక్యత</p>
          <p className="home-intro">Honouring our heritage.<br />Building a stronger community, together.</p>
          <div className="hero-actions">
            <Link className="btn btn-primary btn-lg" to="/register">సభ్యత్వం పొందండి <ArrowRight size={18} aria-hidden="true" /></Link>
            <Link className="home-secondary-link" to="/activities">కార్యక్రమాలు చూడండి <ArrowRight size={17} aria-hidden="true" /></Link>
          </div>
          <p className="home-assurance"><BadgeCheck size={17} aria-hidden="true" /> A shared tradition. A trusted community.</p>
        </div>
        <div className="home-image-caption" aria-hidden="true"><span>OUR HERITAGE, OUR IDENTITY</span><p>ఆధ్యాత్మికతతో అనుబంధం</p></div>
      </section>

      <section className="home-values" aria-label="Our values">
        <div><span>01</span><p>సంప్రదాయం<small>Tradition</small></p></div>
        <div><span>02</span><p>నమ్మకం<small>Trust</small></p></div>
        <div><span>03</span><p>గౌరవం<small>Dignity</small></p></div>
        <div><span>04</span><p>సమైక్యత<small>Togetherness</small></p></div>
      </section>

      <section className="section home-community" aria-labelledby="community-title">
        <div className="home-section-heading">
          <div><span className="home-kicker">సమాజం గురించి · OUR COMMUNITY</span><h2 id="community-title">A tradition of belonging.<br />A community for tomorrow.</h2></div>
          <p>A welcoming space for our Telugu community to connect, participate, and carry our shared values forward.</p>
        </div>
        <div className="home-benefits">
          {benefits.map(({ icon: Icon, title, label, description }) => (
            <article className="home-benefit" key={label}>
              <span className="home-benefit-icon"><Icon size={24} strokeWidth={1.5} aria-hidden="true" /></span>
              <h3>{title}</h3><h4>{label}</h4><p>{description}</p>
            </article>
          ))}
        </div>
        <div className="home-membership-note"><p>Your next chapter in our community starts here.</p><Link to="/membership">Explore memberships <ArrowRight size={17} aria-hidden="true" /></Link></div>
      </section>
    </div>
  );
}
