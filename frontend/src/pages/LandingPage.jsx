import ErrorMessage from '../components/ErrorMessage';

function LandingPage({ health, healthLoading, healthError, onCheckBackend, onNavigate }) {
  const statusLabel = healthLoading
    ? 'Checking...'
    : health
      ? `${health.status} / ${health.database}`
      : 'Unavailable';

  return (
    <section className="page-stack">
      <section className="hero-panel">
        <p className="eyebrow">Career Center volunteer coordination</p>
        <h1>Locked In For The Career Center</h1>
        <p>
          Browse upcoming events, register as a volunteer, and manage event creation from one focused dashboard.
        </p>
        <div className="action-row">
          <button type="button" onClick={() => onNavigate('events')}>View Events</button>
          <button type="button" className="secondary-button" onClick={() => onNavigate('dashboard')}>
            Open Dashboard
          </button>
        </div>
      </section>

      <section className="status-panel" aria-labelledby="connection-heading">
        <div>
          <p className="eyebrow">Backend connection</p>
          <h2 id="connection-heading">{statusLabel}</h2>
          <ErrorMessage message={healthError} />
          {health?.checkedAt && (
            <p className="muted">Last checked: {new Date(health.checkedAt).toLocaleString()}</p>
          )}
        </div>
        <button type="button" onClick={onCheckBackend} disabled={healthLoading}>
          {healthLoading ? 'Checking' : 'Check backend'}
        </button>
      </section>
    </section>
  );
}

export default LandingPage;
