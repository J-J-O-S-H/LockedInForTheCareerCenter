import { useEffect, useState } from 'react';
import { apiClient } from './api/client';

const emptyLoginForm = {
  email: '',
  password: ''
};

const emptyRegistrationForm = {
  firstName: '',
  lastName: '',
  role: 'VOLUNTEER',
  email: '',
  password: ''
};

const emptyEventForm = {
  title: '',
  description: '',
  location: '',
  eventDateTime: '',
  maxVolunteers: '5',
  priority: 'MEDIUM'
};

function getSavedAuth() {
  try {
    const saved = window.localStorage.getItem('lockedin-auth');
    return saved ? JSON.parse(saved) : { user: null, token: null };
  } catch {
    return { user: null, token: null };
  }
}

function App() {
  const [health, setHealth] = useState(null);
  const [healthError, setHealthError] = useState('');
  const [healthLoading, setHealthLoading] = useState(true);
  const [events, setEvents] = useState([]);
  const [eventsLoading, setEventsLoading] = useState(false);
  const [eventsError, setEventsError] = useState('');
  const [loginForm, setLoginForm] = useState(emptyLoginForm);
  const [loginStatus, setLoginStatus] = useState('');
  const [registrationForm, setRegistrationForm] = useState(emptyRegistrationForm);
  const [registrationStatus, setRegistrationStatus] = useState('');
  const [myRegistrations, setMyRegistrations] = useState([]);
  const [myRegistrationsStatus, setMyRegistrationsStatus] = useState('');
  const [eventSignupStatus, setEventSignupStatus] = useState('');
  const [eventForm, setEventForm] = useState(emptyEventForm);
  const [eventFormStatus, setEventFormStatus] = useState('');
  const [auth, setAuth] = useState(getSavedAuth);

  async function loadHealth() {
    setHealthLoading(true);
    setHealthError('');

    try {
      const data = await apiClient.getHealth();
      setHealth(data);
    } catch (error) {
      setHealth(null);
      setHealthError(error.message);
    } finally {
      setHealthLoading(false);
    }
  }

  async function loadEvents() {
    setEventsLoading(true);
    setEventsError('');

    try {
      const data = await apiClient.getEvents();
      setEvents(data);
    } catch (error) {
      setEventsError(error.message);
    } finally {
      setEventsLoading(false);
    }
  }

  async function loadMyRegistrations(token = auth.token) {
    if (!token) {
      setMyRegistrations([]);
      setMyRegistrationsStatus('');
      return;
    }

    setMyRegistrationsStatus('Loading your registered events...');

    try {
      const data = await apiClient.getMyRegistrations(token);
      setMyRegistrations(data);
      setMyRegistrationsStatus('');
    } catch (error) {
      setMyRegistrations([]);
      setMyRegistrationsStatus(error.message);
    }
  }

  async function refreshCurrentUser(token) {
    try {
      const user = await apiClient.getCurrentUser(token);
      setAuth({ user, token });
    } catch {
      setAuth({ user: null, token: null });
      setLoginStatus('Your session has expired. Please sign in again.');
    }
  }

  async function handleLogin(event) {
    event.preventDefault();
    setLoginStatus('Signing in...');

    try {
      const response = await apiClient.login(loginForm);
      setAuth({ user: response.user, token: response.token });
      setLoginForm(emptyLoginForm);
      setLoginStatus(response.message);
      await loadMyRegistrations(response.token);
    } catch (error) {
      setLoginStatus(error.message);
      setAuth({ user: null, token: null });
      setMyRegistrations([]);
    }
  }

  async function handleRegister(event) {
    event.preventDefault();
    setRegistrationStatus('Creating account...');

    try {
      const response = await apiClient.registerUser(registrationForm);
      setAuth({ user: response.user, token: response.token });
      setRegistrationForm(emptyRegistrationForm);
      setRegistrationStatus(response.message);
      setLoginStatus('');
      await loadMyRegistrations(response.token);
    } catch (error) {
      setRegistrationStatus(error.message);
    }
  }

  async function handleCreateEvent(event) {
    event.preventDefault();
    setEventFormStatus('Creating event...');

    try {
      const eventDateTime = new Date(eventForm.eventDateTime).toISOString();
      const createdEvent = await apiClient.createEvent({
        ...eventForm,
        eventDateTime,
        maxVolunteers: Number(eventForm.maxVolunteers)
      }, auth.token);

      setEvents((currentEvents) => [createdEvent, ...currentEvents]);
      setEventForm(emptyEventForm);
      setEventFormStatus('Event created.');
    } catch (error) {
      setEventFormStatus(error.message);
    }
  }

  async function handleRegisterForEvent(eventItem) {
    if (!auth.token) {
      setEventSignupStatus('Please sign in before registering for an event.');
      return;
    }

    if (auth.user?.role !== 'VOLUNTEER') {
      setEventSignupStatus('Only volunteer accounts can register for events.');
      return;
    }

    if (eventItem.availableSpots <= 0) {
      setEventSignupStatus('This event is full.');
      return;
    }

    setEventSignupStatus(`Registering for ${eventItem.title}...`);

    try {
      const registration = await apiClient.registerForEvent(eventItem.id, auth.token);
      setEventSignupStatus(`Registered for ${registration.event.title}.`);
      await loadEvents();
      await loadMyRegistrations(auth.token);
    } catch (error) {
      setEventSignupStatus(error.message);
    }
  }

  async function handleWithdrawFromEvent(registration) {
    if (!auth.token) {
      setEventSignupStatus('Please sign in before withdrawing from an event.');
      return;
    }

    if (auth.user?.role !== 'VOLUNTEER') {
      setEventSignupStatus('Only volunteer accounts can withdraw from volunteer registrations.');
      return;
    }

    setEventSignupStatus(`Withdrawing from ${registration.event.title}...`);

    try {
      const response = await apiClient.withdrawFromEvent(registration.eventId, auth.token);
      setEventSignupStatus(response.message);
      await loadEvents();
      await loadMyRegistrations(auth.token);
    } catch (error) {
      setEventSignupStatus(error.message);
    }
  }

  async function handleDeleteEvent(eventItem) {
    if (!auth.token) {
      setEventSignupStatus('Please sign in before deleting an event.');
      return;
    }

    if (auth.user?.role !== 'ADMIN') {
      setEventSignupStatus('Only admin accounts can delete events.');
      return;
    }

    setEventSignupStatus(`Deleting ${eventItem.title}...`);

    try {
      const response = await apiClient.deleteEvent(eventItem.id, auth.token);
      setEventSignupStatus(response.message);
      await loadEvents();
      if (auth.token) {
        await loadMyRegistrations(auth.token);
      }
    } catch (error) {
      setEventSignupStatus(error.message);
    }
  }

  function handleLogout() {
    setAuth({ user: null, token: null });
    setMyRegistrations([]);
    setEventSignupStatus('');
    setLoginStatus('You have been signed out.');
  }

  useEffect(() => {
    loadHealth();
    loadEvents();

    if (auth.token) {
      refreshCurrentUser(auth.token);
      loadMyRegistrations(auth.token);
    }
  }, []);

  useEffect(() => {
    if (auth.user) {
      window.localStorage.setItem('lockedin-auth', JSON.stringify(auth));
    } else {
      window.localStorage.removeItem('lockedin-auth');
    }
  }, [auth]);

  const statusLabel = healthLoading
    ? 'Checking...'
    : health
      ? `${health.status} / ${health.database}`
      : 'Unavailable';
  const registeredEventIds = new Set(myRegistrations.map((registration) => registration.eventId));

  return (
    <main className="app-shell">
      <section className="intro">
        <p className="eyebrow">Phase 1 foundation</p>
        <h1>Locked In For The Career Center</h1>
        <p>Email/password account registration and login are now the active authentication path.</p>
      </section>

      <section className="status-panel" aria-labelledby="connection-heading">
        <div>
          <p className="eyebrow">Backend connection</p>
          <h2 id="connection-heading">{statusLabel}</h2>
          {healthError && <p className="error-text">{healthError}</p>}
          {health?.checkedAt && (
            <p className="muted">Last checked: {new Date(health.checkedAt).toLocaleString()}</p>
          )}
        </div>
        <button type="button" onClick={loadHealth} disabled={healthLoading}>
          {healthLoading ? 'Checking' : 'Check backend'}
        </button>
      </section>

      {auth.user && (
        <section className="panel dashboard-panel">
          <div className="panel-header">
            <div>
              <p className="eyebrow">Signed in</p>
              <h2>{auth.user.firstName} {auth.user.lastName}</h2>
              <p className="muted">{auth.user.email} / {auth.user.role}</p>
            </div>
            <button type="button" onClick={handleLogout}>
              Sign out
            </button>
          </div>
          <p className="muted">JWT auth is active for protected API requests.</p>
          {myRegistrationsStatus && <p className="muted">{myRegistrationsStatus}</p>}
          {myRegistrations.length > 0 && (
            <div className="registration-list">
              <p className="eyebrow">My registered events</p>
              {myRegistrations.map((registration) => (
                <article className="compact-item" key={registration.id}>
                  <strong>{registration.event.title}</strong>
                  <span>{new Date(registration.event.eventDateTime).toLocaleString()}</span>
                  <span>{registration.status} / {registration.event.status}</span>
                  {auth.user?.role === 'VOLUNTEER' && registration.status === 'REGISTERED' && (
                    <button type="button" onClick={() => handleWithdrawFromEvent(registration)}>
                      Withdraw
                    </button>
                  )}
                </article>
              ))}
            </div>
          )}
        </section>
      )}

      <section className="workspace-grid">
        <section className="panel form-panel">
          <div>
            <p className="eyebrow">POST /api/auth/login</p>
            <h2>Sign in</h2>
          </div>

          <form onSubmit={handleLogin}>
            <label>
              Email
              <input
                type="email"
                name="email"
                value={loginForm.email}
                onChange={(event) => setLoginForm({ ...loginForm, email: event.target.value })}
                required
                placeholder="returninguser@example.com"
              />
            </label>

            <label>
              Password
              <input
                type="password"
                name="password"
                value={loginForm.password}
                onChange={(event) => setLoginForm({ ...loginForm, password: event.target.value })}
                required
                placeholder="Enter your password"
              />
            </label>

            <button type="submit">Sign in</button>
            {loginStatus && <p className="muted">{loginStatus}</p>}
          </form>
        </section>

        <section className="panel form-panel">
          <div>
            <p className="eyebrow">POST /api/auth/register</p>
            <h2>Create account</h2>
          </div>

          <form onSubmit={handleRegister}>
            <label>
              First name
              <input
                name="firstName"
                value={registrationForm.firstName}
                onChange={(event) => setRegistrationForm({ ...registrationForm, firstName: event.target.value })}
                required
                placeholder="Jane"
              />
            </label>

            <label>
              Last name
              <input
                name="lastName"
                value={registrationForm.lastName}
                onChange={(event) => setRegistrationForm({ ...registrationForm, lastName: event.target.value })}
                required
                placeholder="Hornet"
              />
            </label>

            <label>
              Role
              <select
                name="role"
                value={registrationForm.role}
                onChange={(event) => setRegistrationForm({ ...registrationForm, role: event.target.value })}
                required
              >
                <option value="STUDENT">Student</option>
                <option value="VOLUNTEER">Volunteer</option>
                <option value="EMPLOYER">Employer</option>
                <option value="ADMIN">Admin</option>
              </select>
            </label>

            <label>
              Email
              <input
                type="email"
                name="email"
                value={registrationForm.email}
                onChange={(event) => setRegistrationForm({ ...registrationForm, email: event.target.value })}
                required
                placeholder="jane@example.com"
              />
            </label>

            <label>
              Password
              <input
                type="password"
                name="password"
                value={registrationForm.password}
                onChange={(event) => setRegistrationForm({ ...registrationForm, password: event.target.value })}
                required
                placeholder="Include a special character"
              />
            </label>

            <button type="submit">Create account</button>
            {registrationStatus && <p className="muted">{registrationStatus}</p>}
          </form>
        </section>

        <section className="panel events-panel">
          <div className="panel-header">
            <div>
              <p className="eyebrow">GET /api/events</p>
              <h2>Upcoming events</h2>
            </div>
            <button type="button" onClick={loadEvents} disabled={eventsLoading}>
              {eventsLoading ? 'Loading' : 'Refresh events'}
            </button>
          </div>

          {eventsError && <p className="error-text">{eventsError}</p>}
          {eventSignupStatus && <p className="muted">{eventSignupStatus}</p>}
          {auth.user && auth.user.role !== 'VOLUNTEER' && (
            <p className="muted">Event sign-up is available to volunteer accounts.</p>
          )}

          <div className="item-list">
            {events.length === 0 && !eventsLoading && (
              <p className="muted">No events available yet.</p>
            )}

            {events.map((event) => (
              <article className="list-item" key={event.id}>
                <div className="item-heading">
                  <h3>{event.title}</h3>
                  <span>{event.priority}</span>
                </div>
                <p>{event.description}</p>
                <p className="muted">{event.location}</p>
                <p className="muted">
                  {event.currentVolunteers} / {event.maxVolunteers} volunteers registered
                </p>
                <p className="muted">{event.availableSpots} spots available</p>
                <small>{new Date(event.eventDateTime).toLocaleString()}</small>
                {auth.user?.role === 'VOLUNTEER' && (
                  <div className="action-row">
                    <button
                      type="button"
                      onClick={() => handleRegisterForEvent(event)}
                      disabled={event.availableSpots <= 0 || registeredEventIds.has(event.id)}
                    >
                      {registeredEventIds.has(event.id)
                        ? 'Registered'
                        : event.availableSpots <= 0
                          ? 'Full'
                          : 'Sign up'}
                    </button>
                  </div>
                )}
                {auth.user?.role === 'ADMIN' && (
                  <div className="action-row">
                    <button type="button" onClick={() => handleDeleteEvent(event)}>
                      Delete event
                    </button>
                  </div>
                )}
              </article>
            ))}
          </div>
        </section>

        {auth.user?.role === 'ADMIN' && (
          <section className="panel form-panel">
            <div>
              <p className="eyebrow">POST /api/events</p>
              <h2>Create event</h2>
            </div>

            <form onSubmit={handleCreateEvent}>
              <label>
                Title
                <input
                  name="title"
                  value={eventForm.title}
                  onChange={(event) => setEventForm({ ...eventForm, title: event.target.value })}
                  required
                  placeholder="Career Fair"
                />
              </label>

              <label>
                Description
                <textarea
                  name="description"
                  value={eventForm.description}
                  onChange={(event) => setEventForm({ ...eventForm, description: event.target.value })}
                  required
                  placeholder="Describe the event"
                />
              </label>

              <label>
                Location
                <input
                  name="location"
                  value={eventForm.location}
                  onChange={(event) => setEventForm({ ...eventForm, location: event.target.value })}
                  required
                  placeholder="University Union"
                />
              </label>

              <label>
                Date and time
                <input
                  type="datetime-local"
                  name="eventDateTime"
                  value={eventForm.eventDateTime}
                  onChange={(event) => setEventForm({ ...eventForm, eventDateTime: event.target.value })}
                  required
                />
              </label>

              <label>
                Maximum volunteers
                <input
                  type="number"
                  name="maxVolunteers"
                  min="1"
                  value={eventForm.maxVolunteers}
                  onChange={(event) => setEventForm({ ...eventForm, maxVolunteers: event.target.value })}
                  required
                />
              </label>

              <label>
                Priority
                <select
                  name="priority"
                  value={eventForm.priority}
                  onChange={(event) => setEventForm({ ...eventForm, priority: event.target.value })}
                  required
                >
                  <option value="HIGH">High</option>
                  <option value="MEDIUM">Medium</option>
                  <option value="LOW">Low</option>
                </select>
              </label>

              <button type="submit">Create event</button>
              {eventFormStatus && <p className="muted">{eventFormStatus}</p>}
            </form>
          </section>
        )}
      </section>
    </main>
  );
}

export default App;
