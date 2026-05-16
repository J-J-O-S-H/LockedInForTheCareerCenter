import ErrorMessage from '../components/ErrorMessage';
import EventCard from '../components/EventCard';

function EventListPage({
  user,
  events,
  registrations,
  loading,
  error,
  status,
  onRefresh,
  onSignUp,
  onWithdraw,
  onDelete
}) {
  const registeredEventIds = new Set(registrations.map((registration) => registration.eventId));

  return (
    <section className="page-stack">
      <section className="page-title-row">
        <div>
          <p className="eyebrow">Events</p>
          <h1>Upcoming events</h1>
          <p className="muted">View active events and available volunteer slots.</p>
        </div>
        <button type="button" onClick={onRefresh} disabled={loading}>
          {loading ? 'Loading' : 'Refresh'}
        </button>
      </section>

      <ErrorMessage message={error} />
      {status && <p className="status-text">{status}</p>}
      {!user && <p className="muted">Sign in as a volunteer to register for events.</p>}

      <div className="event-grid">
        {events.length === 0 && !loading && (
          <section className="panel empty-state">
            <h2>No events available</h2>
            <p className="muted">Upcoming active events will appear here.</p>
          </section>
        )}

        {events.map((event) => (
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
  );
}

export default EventListPage;
