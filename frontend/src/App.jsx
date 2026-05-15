import { useEffect, useState } from 'react';
import { GoogleOAuthProvider, GoogleLogin } from '@react-oauth/google';
import { apiClient } from './api/client';

const GOOGLE_CLIENT_ID = import.meta.env.VITE_GOOGLE_CLIENT_ID || '1049400448813-elm39rucr2eu08n0cvkl2ma845m72cj4.apps.googleusercontent.com';

const emptyForm = {
  title: '',
  message: ''
};

const emptyLoginForm = {
  email: '',
  password: ''
};

function getSavedAuth() {
  try {
    const saved = window.localStorage.getItem('lockedin-auth');
    return saved ? JSON.parse(saved) : { isAuthenticated: false, email: '' };
  } catch {
    return { isAuthenticated: false, email: '' };
  }
}

function App() {
  const [health, setHealth] = useState(null);
  const [healthError, setHealthError] = useState('');
  const [healthLoading, setHealthLoading] = useState(true);
  const [examples, setExamples] = useState([]);
  const [examplesLoading, setExamplesLoading] = useState(false);
  const [examplesError, setExamplesError] = useState('');
  const [form, setForm] = useState(emptyForm);
  const [formStatus, setFormStatus] = useState('');
  const [events, setEvents] = useState([]);
  const [registeredEvents, setRegisteredEvents] = useState([]);
  const [eventsLoading, setEventsLoading] = useState(false);
  const [eventsError, setEventsError] = useState('');
  const [loginForm, setLoginForm] = useState(emptyLoginForm);
  const [loginStatus, setLoginStatus] = useState('');
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

  async function loadExamples() {
    setExamplesLoading(true);
    setExamplesError('');

    try {
      const data = await apiClient.getExamples();
      setExamples(data);
    } catch (error) {
      setExamplesError(error.message);
    } finally {
      setExamplesLoading(false);
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

  async function handleRegisterForEvent(eventId) {
    try {
      const updatedEvent = await apiClient.registerForEvent(eventId);

      setEvents((currentEvents) =>
        currentEvents.map((event) =>
          event.id === updatedEvent.id ? updatedEvent : event)
      );
      setRegisteredEvents((currentEvents) => [...currentEvents, eventId]);
      alert('You have successfully signed up for this event.');
    } catch (error) {
      alert(error.message);
    }
  }

  async function handleSubmit(event) {
    event.preventDefault();
    setFormStatus('Saving example...');

    try {
      const createdExample = await apiClient.createExample(form);
      setExamples((currentExamples) => [createdExample, ...currentExamples]);
      setForm(emptyForm);
      setFormStatus('Example saved.');
    } catch (error) {
      setFormStatus(error.message);
    }
  }

  async function handleLogin(event) {
    event.preventDefault();
    setLoginStatus('Signing in...');

    try {
      const response = await apiClient.login(loginForm);
      setAuth({ isAuthenticated: true, email: response.email });
      setLoginForm(emptyLoginForm);
      setLoginStatus('Login successful. Welcome back!');
    } catch (error) {
      setLoginStatus(error.message);
      setAuth({ isAuthenticated: false, email: '' });
    }
  }

  async function handleGoogleLoginSuccess(credentialResponse) {
    setLoginStatus('Signing in with Google...');

    try {
      const response = await apiClient.loginWithGoogle(credentialResponse.credential);
      setAuth({ isAuthenticated: true, email: response.email });
      setLoginForm(emptyLoginForm);
      setLoginStatus('Login successful. Welcome back!');
    } catch (error) {
      setLoginStatus(error.message);
      setAuth({ isAuthenticated: false, email: '' });
    }
  }

  function handleGoogleLoginError() {
    setLoginStatus('Google login failed. Please try again.');
    setAuth({ isAuthenticated: false, email: '' });
  }

  function handleLogout() {
    setAuth({ isAuthenticated: false, email: '' });
    setLoginStatus('You have been signed out.');
    setExamples([]);
  }

  useEffect(() => {
    loadHealth();
    loadEvents();
  }, []);

  useEffect(() => {
    if (auth.isAuthenticated) {
      window.localStorage.setItem('lockedin-auth', JSON.stringify(auth));
      loadExamples();
    } else {
      window.localStorage.removeItem('lockedin-auth');
      setExamples([]);
    }
  }, [auth]);

  const statusLabel = healthLoading
    ? 'Checking...'
    : health
      ? `${health.status} / ${health.database}`
      : 'Unavailable';

  return (
    <GoogleOAuthProvider clientId={GOOGLE_CLIENT_ID}>
      <main className="app-shell">
        <section className="intro">
          <p className="eyebrow">Starter project</p>
          <h1>Locked In For The Career Center</h1>
          <p>
            Build Begin
          </p>
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

        <section className="workspace-grid">
          <section className="panel form-panel">
            <div>
              <p className="eyebrow">Authentication</p>
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

            <div style={{ marginTop: '1.5rem', display: 'flex', justifyContent: 'center' }}>
              <GoogleLogin
                onSuccess={handleGoogleLoginSuccess}
                onError={handleGoogleLoginError}
              />
            </div>

            {!auth.isAuthenticated && (
              <p className="muted">Use the seeded account email and password to sign in, or sign in with Google.</p>
            )}
          </section>

          {auth.isAuthenticated && (
            <section className="panel dashboard-panel">
              <div className="panel-header">
                <div>
                  <p className="eyebrow">Dashboard</p>
                  <h2>Welcome back, {auth.email}</h2>
                </div>
                <button type="button" onClick={handleLogout}>
                  Sign out
                </button>
              </div>
              <p>Successful login grants access to your dashboard and example content.</p>
            </section>
          )}

          <div className="panel">
            <div className="panel-header">
              <div>
                <p className="eyebrow">GET /api/events</p>
                <h2>Events</h2>
              </div>
              <button type="button" onClick={loadEvents} disabled={eventsLoading}>
                {eventsLoading ? 'Loading' : 'Refresh events'}
              </button>
            </div>

            {eventsError && <p className="error-text">{eventsError}</p>}

            <div className="example-list">
              {events.length === 0 && !eventsLoading && (
                <p className="muted">No events available.</p>
              )}

              {events.map((event) => (
                <article className="example-item" key={event.id}>
                  <h3>{event.title}</h3>
                  <p>{event.location}</p>
                  <p className="muted">
                    {event.currentVolunteers} / {event.maxVolunteers} volunteers registered
                  </p>
                  <p className="muted">
                    {event.availableSpots} spots available
                  </p>
                  <small>{new Date(event.eventDate).toLocaleString()}</small>

                  <button
                    type="button"
                    onClick={() => handleRegisterForEvent(event.id)}
                    disabled={event.availableSpots <= 0 || registeredEvents.includes(event.id)}
                  >
                    {registeredEvents.includes(event.id)
                      ? 'Registered'
                      : event.availableSpots <= 0
                        ? 'Event full'
                        : 'Register'}
                  </button>
                </article>
              ))}
            </div>
          </div>

          {auth.isAuthenticated && (
            <div className="panel">
              <div className="panel-header">
                <div>
                  <p className="eyebrow">GET /api/example</p>
                  <h2>Example documents</h2>
                </div>
                <button type="button" onClick={loadExamples} disabled={examplesLoading}>
                  {examplesLoading ? 'Loading' : 'Refresh examples'}
                </button>
              </div>

              {examplesError && <p className="error-text">{examplesError}</p>}

              <div className="example-list">
                {examples.length === 0 && !examplesLoading && (
                  <p className="muted">No example documents yet. Create one with the form.</p>
                )}

                {examples.map((example) => (
                  <article className="example-item" key={example.id}>
                    <h3>{example.title}</h3>
                    <p>{example.message}</p>
                    <small>{new Date(example.createdAt).toLocaleString()}</small>
                  </article>
                ))}
              </div>
            </div>
          )}

          {auth.isAuthenticated && (
            <form className="panel form-panel" onSubmit={handleSubmit}>
              <div>
                <p className="eyebrow">POST /api/example</p>
                <h2>Create an example</h2>
              </div>

              <label>
                Title
                <input
                  name="title"
                  value={form.title}
                  onChange={(event) => setForm({ ...form, title: event.target.value })}
                  maxLength={80}
                  required
                  placeholder="Connection test"
                />
              </label>

              <label>
                Message
                <textarea
                  name="message"
                  value={form.message}
                  onChange={(event) => setForm({ ...form, message: event.target.value })}
                  maxLength={500}
                  required
                  placeholder="The frontend can POST to the backend."
                />
              </label>

              <button type="submit">Save example</button>
              {formStatus && <p className="muted">{formStatus}</p>}
            </form>
          )}
        </section>
      </main>
    </GoogleOAuthProvider>
  );
}

export default App;
