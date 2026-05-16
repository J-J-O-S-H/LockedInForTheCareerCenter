import { useState } from 'react';
import ErrorMessage from '../components/ErrorMessage';
import FormField from '../components/FormField';

const emptyLoginForm = {
  email: '',
  password: ''
};

function LoginPage({ status, onLogin, onNavigate }) {
  const [form, setForm] = useState(emptyLoginForm);
  const [localError, setLocalError] = useState('');

  async function handleSubmit(event) {
    event.preventDefault();
    setLocalError('');

    if (!form.email.trim() || !form.password) {
      setLocalError('Please complete all required fields.');
      return;
    }

    if (!form.email.includes('@')) {
      setLocalError('Please enter a valid email address.');
      return;
    }

    await onLogin(form);
  }

  return (
    <section className="auth-page">
      <div className="panel form-panel">
        <div>
          <h1>Sign in</h1>
          <p className="muted">Use your email and password to open the dashboard.</p>
        </div>

        <form onSubmit={handleSubmit}>
          <FormField
            label="Email"
            type="email"
            name="email"
            value={form.email}
            onChange={(event) => setForm({ ...form, email: event.target.value })}
            required
            placeholder="returninguser@example.com"
          />

          <FormField
            label="Password"
            type="password"
            name="password"
            value={form.password}
            onChange={(event) => setForm({ ...form, password: event.target.value })}
            required
            placeholder="Enter your password"
          />

          <button type="submit">Sign in</button>
          <div className="form-message-slot">
            <ErrorMessage message={localError || (status?.type === 'error' ? status.message : '')} />
            {status?.type === 'success' && <p className="status-text">{status.message}</p>}
          </div>
        </form>

        <div className="form-links">
          <button type="button" className="link-button" onClick={() => onNavigate('register')}>
            Create a new account
          </button>
          <button type="button" className="link-button" onClick={() => onNavigate('home')}>
            Back to landing
          </button>
        </div>
      </div>
    </section>
  );
}

export default LoginPage;
