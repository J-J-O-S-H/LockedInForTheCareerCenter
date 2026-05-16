import { useState } from 'react';
import ErrorMessage from '../components/ErrorMessage';
import FormField from '../components/FormField';

const emptyLoginForm = {
  email: '',
  password: ''
};

function LoginPage({ status, onLogin, onNavigate }) {
  const [form, setForm] = useState(emptyLoginForm);

  async function handleSubmit(event) {
    event.preventDefault();
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
          <ErrorMessage message={status?.type === 'error' ? status.message : ''} />
          {status?.type === 'success' && <p className="muted">{status.message}</p>}
        </form>

        <button type="button" className="link-button" onClick={() => onNavigate('register')}>
          Create a new account
        </button>
        <button type="button" className="link-button" onClick={() => onNavigate('home')}>
          Back to landing
        </button>
      </div>
    </section>
  );
}

export default LoginPage;
