function LandingPage({ onNavigate }) {
  return (
    <section className="page-stack landing-page">
      <section className="hero-panel">
        <div className="hero-content">
          <h1>Locked In For The Career Center</h1>
          <p>
            Browse upcoming events, register as a volunteer, and manage event creation from one focused dashboard.
          </p>
        </div>
        <div className="action-row">
          <button type="button" onClick={() => onNavigate('events')}>View Events</button>
          <button type="button" className="secondary-button" onClick={() => onNavigate('login')}>
            Sign in
          </button>
          <button type="button" className="secondary-button" onClick={() => onNavigate('register')}>
            Create account
          </button>
        </div>
      </section>
    </section>
  );
}

export default LandingPage;
