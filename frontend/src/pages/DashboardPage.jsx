import EventCard from '../components/EventCard';
import ErrorMessage from '../components/ErrorMessage';
import RoleGate from '../components/RoleGate';

function DashboardPage({
  user,
  events,
  registrations,
  eventsLoading,
  error,
  status,
  onNavigate,
  onSignUp,
  onWithdraw,
  onDelete
}) {
  if (!user) {
    return (
      <section className="panel empty-state">
        <h1>Sign in required</h1>
        <p className="muted">The dashboard is available after login.</p>
        <button type="button" onClick={() => onNavigate('login')}>Go to login</button>
      </section>
    );
  }

  const registeredEventIds = new Set(registrations.map((registration) => registration.eventId));
  const registeredEvents = registrations.map((registration) => registration.event);
  const previewEvents = events.slice(0, 4);

  return (
    <section className="page-stack">
      <section className="dashboard-hero">
        <div>
          <p className="eyebrow">Dashboard</p>
          <h1>Welcome, {user.firstName} {user.lastName}</h1>
          <p className="muted">{user.email} / {user.role}</p>
        </div>
        <div className="role-badge">{user.role}</div>
      </section>

      <ErrorMessage message={error} />
      {status && <p className="status-text">{status}</p>}

      <section className="summary-grid">
        <article className="metric-card">
          <span>{events.length}</span>
          <p>Upcoming events</p>
        </article>
                <article className="metric-card">
          <span>{registrations.length}</span>
          <p>Registered events</p>
        </article>
        <article className="metric-card">
          <span>{user.role}</span>
          <p>Current role</p>
        </article>
      </section>

      <section className="panel">
        <div className="panel-header">
          <div>
            <p className="eyebrow">Role actions</p>
            <h2>Available actions</h2>
          </div>
        </div>
        <div className="action-grid">
          <RoleGate user={user} roles={['VOLUNTEER']}>
            <button type="button" onClick={() => onNavigate('events')}>Find volunteer events</button>
          </RoleGate>
          <RoleGate user={user} roles={['ADMIN']}>
            <button type="button" onClick={() => onNavigate('create-event')}>Create event</button>
          </RoleGate>
          <button type="button" className="secondary-button" onClick={() => onNavigate('events')}>
            Browse events
          </button>
        </div>
      </section>

      <section className="content-grid">
        <section className="panel">
          <div className="panel-header">
            <div>
              <p className="eyebrow">My registrations</p>
              <h2>Registered events</h2>
            </div>
          </div>

          {registeredEvents.length === 0 && (
            <p className="muted">No active event registrations yet.</p>
          )}

          <div className="item-list">
            {registeredEvents.map((event) => (
              <EventCard
                key={event.id}
                event={event}
                user={user}
                isRegistered
                onSignUp={onSignUp}
                onWithdraw={onWithdraw}
                onDelete={onDelete}
              />
            ))}
          </div>
        </section>

        <section className="panel">
          <div className="panel-header">
            <div>
              <p className="eyebrow">Upcoming</p>
              <h2>Upcoming events</h2>
            </div>
            <button type="button" className="secondary-button" onClick={() => onNavigate('events')}>
              View all
            </button>
          </div>

          {eventsLoading && <p className="muted">Loading events...</p>}
          <div className="item-list">
            {previewEvents.map((event) => (
              <EventCard
                key={event.id}
                event={event}
                user={user}
                isRegistered={registeredEventIds.has(event.id)}
                onSignUp={onSignUp}
                onWithdraw={onWithdraw}
                onDelete={onDelete}
              />
            ))}
          </div>
        </section>
      </section>
    </section>
  );
}

export default DashboardPage;
