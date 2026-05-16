import { useEffect, useRef, useState } from 'react';
import { apiClient } from './api/client';
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

const viewToPath = {
  home: '/',
  login: '/login',
  register: '/register',
  events: '/events',
  'create-event': '/events/create'
};

function getViewFromPath(pathname) {
  if (pathname === '/dashboard' || pathname === '/') {
    return 'home';
  }

  if (pathname === '/login') {
    return 'login';
  }

  if (pathname === '/register') {
    return 'register';
  }

  if (pathname === '/events/create') {
    return 'create-event';
  }

  if (pathname === '/events') {
    return 'events';
  }

  return 'home';
}

function App() {
  const [view, setView] = useState(() => getViewFromPath(window.location.pathname));
  const [events, setEvents] = useState([]);
  const [eventsLoading, setEventsLoading] = useState(false);
  const [eventsError, setEventsError] = useState('');
  const [registrations, setRegistrations] = useState([]);
  const [registrationsError, setRegistrationsError] = useState('');
  const [authStatus, setAuthStatus] = useState(null);
  const [eventFeedback, setEventFeedback] = useState(null);
  const [createStatus, setCreateStatus] = useState('');
  const [createError, setCreateError] = useState('');
  const [auth, setAuth] = useState(getSavedAuth);
  const authRef = useRef(auth);
  const eventFeedbackTimerRef = useRef(null);

  function updateView(nextView, { replace = false } = {}) {
    const nextPath = viewToPath[nextView] || '/';
    const currentPath = `${window.location.pathname}${window.location.search}${window.location.hash}`;

    if (currentPath !== nextPath) {
      if (replace) {
        window.history.replaceState(null, '', nextPath);
      } else {
        window.history.pushState(null, '', nextPath);
      }
    }

    setView(nextView);
  }

  function clearEventFeedback() {
    if (eventFeedbackTimerRef.current) {
      window.clearTimeout(eventFeedbackTimerRef.current);
      eventFeedbackTimerRef.current = null;
    }
    setEventFeedback(null);
  }

  function showEventFeedback(eventId, type, message, { autoClear = false } = {}) {
    if (eventFeedbackTimerRef.current) {
      window.clearTimeout(eventFeedbackTimerRef.current);
      eventFeedbackTimerRef.current = null;
    }

    setEventFeedback({ eventId, type, message });

    if (autoClear) {
      eventFeedbackTimerRef.current = window.setTimeout(() => {
        setEventFeedback((current) => (
          current?.eventId === eventId && current?.message === message ? null : current
        ));
        eventFeedbackTimerRef.current = null;
      }, 4500);
    }
  }

  function setAuthState(nextAuth) {
    authRef.current = nextAuth;
    setAuth(nextAuth);
  }

  function navigate(nextView, options = {}) {
    const normalizedView = nextView === 'dashboard' || nextView === 'landing' ? 'home' : nextView;
    clearEventFeedback();

    if (normalizedView === 'create-event' && !auth.user) {
      updateView('login', options);
      setAuthStatus({ type: 'error', message: 'Please sign in to continue.' });
      return;
    }

    if (normalizedView === 'create-event' && auth.user?.role !== 'ADMIN') {
      updateView('home', options);
      return;
    }

    updateView(normalizedView, options);
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

  async function refreshEvents() {
    clearEventFeedback();
    await loadEvents();
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
        if (authRef.current.token === token) {
          clearAuth('Your session expired. Please sign in again.');
        }
      } else {
        setRegistrationsError(error.message);
      }
    }
  }

  async function refreshCurrentUser(token) {
    try {
      const user = await apiClient.getCurrentUser(token);
      if (authRef.current.token === token) {
        setAuthState({ user, token });
      }
      await loadRegistrations(token);
    } catch {
      if (authRef.current.token === token) {
        clearAuth('Your session expired. Please sign in again.');
      }
    }
  }

  function clearAuth(message = '') {
    setAuthState({ user: null, token: null });
    setRegistrations([]);
    updateView('home');
    if (message) {
      setAuthStatus({ type: 'error', message });
    }
  }

  async function handleLogin(credentials) {
    setAuthStatus({ type: 'info', message: 'Signing in...' });

    try {
      const response = await apiClient.login(credentials);
      setAuthState({ user: response.user, token: response.token });
      setAuthStatus({ type: 'success', message: response.message });
      updateView('home');
      await Promise.all([loadEvents(), loadRegistrations(response.token)]);
    } catch (error) {
      setAuthState({ user: null, token: null });
      setAuthStatus({ type: 'error', message: error.message });
    }
  }

  async function handleRegister(account) {
    setAuthStatus({ type: 'info', message: 'Creating account...' });

    try {
      const response = await apiClient.registerUser(account);
      setAuthState({ user: response.user, token: response.token });
      setAuthStatus({ type: 'success', message: response.message });
      updateView('home');
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
      updateView('login');
      setAuthStatus({ type: 'error', message: 'Please sign in before registering for an event.' });
      return;
    }

    if (auth.user?.role !== 'VOLUNTEER' && auth.user?.role !== 'ADMIN') {
      showEventFeedback(event.id, 'error', 'Only volunteer and admin accounts can register for events.');
      return;
    }

    showEventFeedback(event.id, 'info', `Registering for ${event.title}...`);

    try {
      const registration = await apiClient.registerForEvent(event.id, auth.token);
      await Promise.all([loadEvents(), loadRegistrations(auth.token)]);
      showEventFeedback(event.id, 'success', `Registered for ${registration.event.title}.`, { autoClear: true });
    } catch (error) {
      showEventFeedback(event.id, 'error', error.message);
    }
  }

  async function handleWithdraw(event) {
    if (!auth.token) {
      updateView('login');
      setAuthStatus({ type: 'error', message: 'Please sign in before withdrawing from an event.' });
      return;
    }

    showEventFeedback(event.id, 'info', `Withdrawing from ${event.title}...`);

    try {
      const response = await apiClient.withdrawFromEvent(event.id, auth.token);
      await Promise.all([loadEvents(), loadRegistrations(auth.token)]);
      showEventFeedback(event.id, 'success', response.message, { autoClear: true });
    } catch (error) {
      showEventFeedback(event.id, 'error', error.message);
    }
  }

  async function handleDeleteEvent(event) {
    if (!auth.token) {
      updateView('login');
      setAuthStatus({ type: 'error', message: 'Please sign in before deleting an event.' });
      return;
    }

    if (auth.user?.role !== 'ADMIN') {
      showEventFeedback(event.id, 'error', 'Only admin accounts can delete events.');
      return;
    }

    showEventFeedback(event.id, 'info', `Deleting ${event.title}...`);

    try {
      const response = await apiClient.deleteEvent(event.id, auth.token);
      await Promise.all([loadEvents(), loadRegistrations(auth.token)]);
      showEventFeedback(event.id, 'success', response.message, { autoClear: true });
    } catch (error) {
      showEventFeedback(event.id, 'error', error.message);
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
    authRef.current = auth;
  }, [auth]);

  useEffect(() => () => {
    if (eventFeedbackTimerRef.current) {
      window.clearTimeout(eventFeedbackTimerRef.current);
    }
  }, []);

  useEffect(() => {
    const normalizedView = getViewFromPath(window.location.pathname);
    if (window.location.pathname === '/dashboard') {
      navigate('home', { replace: true });
    } else {
      setView(normalizedView);
    }

    function handlePopState() {
      clearEventFeedback();
      setView(getViewFromPath(window.location.pathname));
    }

    window.addEventListener('popstate', handlePopState);
    return () => window.removeEventListener('popstate', handlePopState);
  }, []);

  useEffect(() => {
    loadEvents();

    if (auth.token) {
      refreshCurrentUser(auth.token);
    }
  }, []);

  useEffect(() => {
    if (auth.user?.role !== 'ADMIN' && view === 'create-event') {
      updateView('home', { replace: true });
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
    error: eventsError || registrationsError,
    eventFeedback,
    onNavigate: navigate,
    onSignUp: handleSignUp,
    onWithdraw: handleWithdraw,
    onDelete: handleDeleteEvent,
    onLogout: handleLogout
  };

  function renderPage() {
    if (view === 'login') {
      return <LoginPage status={authStatus} onLogin={handleLogin} onNavigate={navigate} />;
    }

    if (view === 'register') {
      return <RegisterPage status={authStatus} onRegister={handleRegister} onNavigate={navigate} />;
    }

    if (view === 'events') {
      return (
        <EventListPage
          {...sharedPageProps}
          loading={eventsLoading}
          onRefresh={refreshEvents}
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

    if (view === 'home' && auth.user) {
      return <DashboardPage {...sharedPageProps} />;
    }

    return (
      <LandingPage
        onNavigate={navigate}
      />
    );
  }

  return (
    <main className="app-shell">
      {renderPage()}
    </main>
  );
}

export default App;
