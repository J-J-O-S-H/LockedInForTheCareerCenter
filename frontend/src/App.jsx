import { useEffect, useMemo, useState } from 'react';
import { apiClient } from './api/client';
import Header from './components/Header';
import CreateEventPage from './pages/CreateEventPage';
import DashboardPage from './pages/DashboardPage';
import EventListPage from './pages/EventListPage';
import LandingPage from './pages/LandingPage';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';

function getSavedAuth() {
  try {
    const saved = window.localStorage.getItem('lockedin-auth');
    return saved ? JSON.parse(saved) : { user: null, token: null };
  } catch {
    return { user: null, token: null };
  }
}

function App() {
  const [view, setView] = useState('landing');
  const [health, setHealth] = useState(null);
  const [healthError, setHealthError] = useState('');
  const [healthLoading, setHealthLoading] = useState(true);
  const [events, setEvents] = useState([]);
  const [eventsLoading, setEventsLoading] = useState(false);
  const [eventsError, setEventsError] = useState('');
  const [registrations, setRegistrations] = useState([]);
  const [registrationsError, setRegistrationsError] = useState('');
  const [authStatus, setAuthStatus] = useState(null);
  const [actionStatus, setActionStatus] = useState('');
  const [actionError, setActionError] = useState('');
  const [createStatus, setCreateStatus] = useState('');
  const [createError, setCreateError] = useState('');
  const [auth, setAuth] = useState(getSavedAuth);

  const registeredEventIds = useMemo(
    () => new Set(registrations.map((registration) => registration.eventId)),
    [registrations]
  );

  function navigate(nextView) {
    if ((nextView === 'dashboard' || nextView === 'create-event') && !auth.user) {
      setView('login');
      setAuthStatus({ type: 'error', message: 'Please sign in to continue.' });
      return;
    }

    if (nextView === 'create-event' && auth.user?.role !== 'ADMIN') {
      setView('dashboard');
      setActionError('Only admin accounts can create events.');
      return;
    }

    setView(nextView);
  }

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

  async function loadRegistrations(token = auth.token) {
    if (!token) {
      setRegistrations([]);
      setRegistrationsError('');
      return;
    }

    try {
      const data = await apiClient.getMyRegistrations(token);
      setRegistrations(data);
      setRegistrationsError('');
    } catch (error) {
      if (error.status === 401) {
        clearAuth('Your session has expired. Please sign in again.');
      } else {
        setRegistrationsError(error.message);
      }
    }
  }

  async function refreshCurrentUser(token) {
    try {
      const user = await apiClient.getCurrentUser(token);
      setAuth({ user, token });
      await loadRegistrations(token);
    } catch {
      clearAuth('Your session has expired. Please sign in again.');
    }
  }

  function clearAuth(message = '') {
    setAuth({ user: null, token: null });
    setRegistrations([]);
    setView('login');
    if (message) {
      setAuthStatus({ type: 'error', message });
    }
  }

  async function handleLogin(credentials) {
    setAuthStatus({ type: 'info', message: 'Signing in...' });

    try {
      const response = await apiClient.login(credentials);
      setAuth({ user: response.user, token: response.token });
      setAuthStatus({ type: 'success', message: response.message });
      setView('dashboard');
      await Promise.all([loadEvents(), loadRegistrations(response.token)]);
    } catch (error) {
      setAuth({ user: null, token: null });
      setAuthStatus({ type: 'error', message: error.message });
    }
  }

  async function handleRegister(account) {
    setAuthStatus({ type: 'info', message: 'Creating account...' });

    try {
      const response = await apiClient.registerUser(account);
      setAuth({ user: response.user, token: response.token });
      setAuthStatus({ type: 'success', message: response.message });
      setView('dashboard');
      await Promise.all([loadEvents(), loadRegistrations(response.token)]);
    } catch (error) {
      setAuthStatus({ type: 'error', message: error.message });
    }
  }

  function handleLogout() {
    clearAuth('You have been signed out.');
  }

  async function handleSignUp(event) {
    if (!auth.token) {
      setActionError('Please sign in before registering for an event.');
      setView('login');
      return;
    }

    if (auth.user?.role !== 'VOLUNTEER') {
      setActionError('Only volunteer accounts can register for events.');
      return;
    }

    setActionError('');
    setActionStatus(`Registering for ${event.title}...`);

    try {
      const registration = await apiClient.registerForEvent(event.id, auth.token);
      setActionStatus(`Registered for ${registration.event.title}.`);
      await Promise.all([loadEvents(), loadRegistrations(auth.token)]);
    } catch (error) {
      setActionStatus('');
      setActionError(error.message);
    }
  }

  async function handleWithdraw(event) {
    if (!auth.token) {
      setActionError('Please sign in before withdrawing from an event.');
      setView('login');
      return;
    }

    setActionError('');
    setActionStatus(`Withdrawing from ${event.title}...`);

    try {
      const response = await apiClient.withdrawFromEvent(event.id, auth.token);
      setActionStatus(response.message);
      await Promise.all([loadEvents(), loadRegistrations(auth.token)]);
    } catch (error) {
      setActionStatus('');
      setActionError(error.message);
    }
  }

  async function handleDeleteEvent(event) {
    if (!auth.token) {
      setActionError('Please sign in before deleting an event.');
      setView('login');
      return;
    }

    if (auth.user?.role !== 'ADMIN') {
      setActionError('Only admin accounts can delete events.');
      return;
    }

    setActionError('');
    setActionStatus(`Deleting ${event.title}...`);

    try {
      const response = await apiClient.deleteEvent(event.id, auth.token);
      setActionStatus(response.message);
      await Promise.all([loadEvents(), loadRegistrations(auth.token)]);
    } catch (error) {
      setActionStatus('');
      setActionError(error.message);
    }
  }

  async function handleCreateEvent(event) {
    if (auth.user?.role !== 'ADMIN') {
      setCreateError('Only admin accounts can create events.');
      return false;
    }

    setCreateError('');
    setCreateStatus('Creating event...');

    try {
      const createdEvent = await apiClient.createEvent(event, auth.token);
      setCreateStatus(`${createdEvent.title} created.`);
      await loadEvents();
      return true;
    } catch (error) {
      setCreateStatus('');
      setCreateError(error.message);
      return false;
    }
  }

  useEffect(() => {
    loadHealth();
    loadEvents();

    if (auth.token) {
      refreshCurrentUser(auth.token);
    }
  }, []);

  useEffect(() => {
    if (!auth.user && (view === 'dashboard' || view === 'create-event')) {
      setView('login');
    }

    if (auth.user?.role !== 'ADMIN' && view === 'create-event') {
      setView('dashboard');
    }
  }, [auth.user, view]);

  useEffect(() => {
    if (auth.user && auth.token) {
      window.localStorage.setItem('lockedin-auth', JSON.stringify(auth));
    } else {
      window.localStorage.removeItem('lockedin-auth');
    }
  }, [auth]);

  const sharedPageProps = {
    user: auth.user,
    events,
    registrations,
    eventsLoading,
    error: eventsError || registrationsError || actionError,
    status: actionStatus,
    onNavigate: navigate,
    onSignUp: handleSignUp,
    onWithdraw: handleWithdraw,
    onDelete: handleDeleteEvent
  };

  function renderPage() {
    if (view === 'login') {
      return <LoginPage status={authStatus} onLogin={handleLogin} onNavigate={navigate} />;
    }

    if (view === 'register') {
      return <RegisterPage status={authStatus} onRegister={handleRegister} onNavigate={navigate} />;
    }

    if (view === 'dashboard') {
      return <DashboardPage {...sharedPageProps} />;
    }

    if (view === 'events') {
      return (
        <EventListPage
          {...sharedPageProps}
          loading={eventsLoading}
          onRefresh={loadEvents}
        />
      );
    }

    if (view === 'create-event') {
      return (
        <CreateEventPage
          user={auth.user}
          status={createStatus}
          error={createError}
          onCreateEvent={handleCreateEvent}
          onNavigate={navigate}
        />
      );
    }

    return (
      <LandingPage
        health={health}
        healthLoading={healthLoading}
        healthError={healthError}
        onCheckBackend={loadHealth}
        onNavigate={navigate}
      />
    );
  }

  return (
    <>
      <Header
        user={auth.user}
        currentView={view}
        onNavigate={navigate}
        onLogout={handleLogout}
      />
      <main className="app-shell">
        {renderPage()}
      </main>
    </>
  );
}

export default App;
