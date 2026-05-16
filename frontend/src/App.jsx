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
    } catch (error) {
      setLoginStatus(error.message);
      setAuth({ user: null, token: null });
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
    } catch (error) {
      setRegistrationStatus(error.message);
    }
  }

  function handleLogout() {
    setAuth({ user: null, token: null });
    setLoginStatus('You have been signed out.');
  }

  useEffect(() => {
    loadHealth();
    loadEvents();

    if (auth.token) {
      refreshCurrentUser(auth.token);
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

          <div className="item-list">
            {events.length === 0 && !eventsLoading && (
              <p className="muted">No events available yet.</p>
            )}

            {events.map((event) => (
              <article className="list-item" key={event.id}>
                <h3>{event.title}</h3>
                <p>{event.location}</p>
                <p className="muted">
                  {event.currentVolunteers} / {event.maxVolunteers} volunteers registered
                </p>
                <p className="muted">{event.availableSpots} spots available</p>
                <small>{new Date(event.eventDate).toLocaleString()}</small>
              </article>
            ))}
          </div>
        </section>
      </section>
    </main>
  );
}

export default App;
