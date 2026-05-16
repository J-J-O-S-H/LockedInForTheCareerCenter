import EventCard from '../components/EventCard';
import ErrorMessage from '../components/ErrorMessage';
import RoleGate from '../components/RoleGate';

function DashboardPage({
  user,
  events,
  registrations,
  eventsLoading,
  error,
  eventFeedback,
  onNavigate,
  onSignUp,
  onWithdraw,
  onDelete,
  onLogout
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
    <section className="page-stack dashboard-page">
      <section className="dashboard-hero">
        <div className="hero-content">
          <h1>Welcome, {user.firstName} {user.lastName}</h1>
          <p className="muted">{user.email}</p>
        </div>
        <div className="account-actions">
          <button type="button" className="secondary-button" onClick={onLogout}>
            Logout
          </button>
        </div>
      </section>

      <ErrorMessage message={error} />

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
          <span className="role-value">{user.role}</span>
          <p>Current role</p>
        </article>
      </section>

      <section className="panel action-panel">
        <div className="panel-header">
          <div>
            <h2>Available actions</h2>
          </div>
        </div>
        <div className="action-grid">
          <button type="button" onClick={() => onNavigate('events')}>
            View events
          </button>
          <RoleGate user={user} roles={['ADMIN']}>
            <button type="button" className="secondary-button" onClick={() => onNavigate('create-event')}>
              Create event
            </button>
          </RoleGate>
        </div>
      </section>

      <section className="content-grid">
        <section className="panel">
          <div className="panel-header">
            <div>
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
                showWithdraw
                onSignUp={onSignUp}
                onWithdraw={onWithdraw}
                onDelete={onDelete}
                feedback={eventFeedback?.eventId === event.id ? eventFeedback : null}
              />
            ))}
          </div>
        </section>

        <section className="panel">
          <div className="panel-header">
            <div>
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
                feedback={eventFeedback?.eventId === event.id ? eventFeedback : null}
              />
            ))}
          </div>
        </section>
      </section>
    </section>
  );
}

export default DashboardPage;
