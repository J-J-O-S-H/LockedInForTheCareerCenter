function EventCard({
  event,
  user,
  isRegistered = false,
  showWithdraw = false,
  onSignUp,
  onWithdraw,
  onDelete,
  feedback
}) {
  const eventDateValue = event.eventDateTime || event.eventDate;
  const eventDate = eventDateValue ? new Date(eventDateValue) : null;
  const isFull = event.availableSpots <= 0;
  const canRegister = user?.role === 'VOLUNTEER' || user?.role === 'ADMIN';
  const cardClassName = isRegistered && !showWithdraw
    ? 'event-card event-card-registered'
    : 'event-card';

  return (
    <article className={cardClassName}>
      <div className="event-card-heading">
        <div>
          <h3>{event.title}</h3>
          <p className="event-card-description">
            {event.description || 'Event details will be posted by the Career Center.'}
          </p>
        </div>
        {event.priority && <span className="priority-pill">{event.priority}</span>}
      </div>

      <dl className="event-details">
        <div>
          <dt>Location</dt>
          <dd>{event.location}</dd>
        </div>
        <div>
          <dt>Date and time</dt>
          <dd>{eventDate ? eventDate.toLocaleString() : 'Not scheduled'}</dd>
        </div>
        <div>
          <dt>Volunteer slots</dt>
          <dd>{event.currentVolunteers} / {event.maxVolunteers} filled</dd>
        </div>
        <div>
          <dt>Available</dt>
          <dd>{event.availableSpots} spots</dd>
        </div>
      </dl>

      <div className="action-row">
        {canRegister && (
          isRegistered && showWithdraw ? (
            <button type="button" className="secondary-button" onClick={() => onWithdraw(event)}>
              Withdraw
            </button>
          ) : isRegistered ? (
            <span className="registered-pill">Registered</span>
          ) : (
            <button type="button" disabled={isFull} onClick={() => onSignUp(event)}>
              {isFull ? 'Full' : 'Sign up'}
            </button>
          )
        )}

        {user?.role === 'ADMIN' && (
          <button type="button" className="danger-button" onClick={() => onDelete(event)}>
            Delete
          </button>
        )}
      </div>

      {feedback?.message && (
        <p className={`event-feedback ${feedback.type === 'error' ? 'error-text' : 'status-text'}`}>
          {feedback.message}
        </p>
      )}
    </article>
  );
}

export default EventCard;
