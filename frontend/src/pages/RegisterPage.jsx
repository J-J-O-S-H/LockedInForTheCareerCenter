import { useState } from 'react';
import ErrorMessage from '../components/ErrorMessage';
import FormField from '../components/FormField';

const emptyRegistrationForm = {
  firstName: '',
  lastName: '',
  role: 'VOLUNTEER',
  email: '',
  password: ''
};

function RegisterPage({ status, onRegister, onNavigate }) {
  const [form, setForm] = useState(emptyRegistrationForm);
  const [localError, setLocalError] = useState('');

  async function handleSubmit(event) {
    event.preventDefault();
    setLocalError('');

    if (
      !form.firstName.trim()
      || !form.lastName.trim()
      || !form.role
      || !form.email.trim()
      || !form.password
    ) {
      setLocalError('Please complete all required fields.');
      return;
    }

    if (!form.email.includes('@')) {
      setLocalError('Please enter a valid email address.');
      return;
    }

    if (!/[^A-Za-z0-9]/.test(form.password)) {
      setLocalError('Password must include at least one special character.');
      return;
    }

    await onRegister(form);
  }

  return (
    <section className="auth-page">
      <div className="panel form-panel">
        <div>
          <h1>Create account</h1>
          <p className="muted">Choose the role that matches your Career Center access.</p>
        </div>

        <form onSubmit={handleSubmit}>
          <div className="two-column-form">
            <FormField
              label="First name"
              name="firstName"
              value={form.firstName}
              onChange={(event) => setForm({ ...form, firstName: event.target.value })}
              required
              placeholder="Jane"
            />
            <FormField
              label="Last name"
              name="lastName"
              value={form.lastName}
              onChange={(event) => setForm({ ...form, lastName: event.target.value })}
              required
              placeholder="Hornet"
            />
          </div>

          <FormField label="Role" name="role">
            <select
              name="role"
              value={form.role}
              onChange={(event) => setForm({ ...form, role: event.target.value })}
              required
            >
              <option value="STUDENT">Student</option>
              <option value="VOLUNTEER">Volunteer</option>
              <option value="EMPLOYER">Employer</option>
              <option value="ADMIN">Admin</option>
            </select>
          </FormField>

          <FormField
            label="Email"
            type="email"
            name="email"
            value={form.email}
            onChange={(event) => setForm({ ...form, email: event.target.value })}
            required
            placeholder="jane@example.com"
          />

          <FormField
            label="Password"
            type="password"
            name="password"
            value={form.password}
            onChange={(event) => setForm({ ...form, password: event.target.value })}
            required
            placeholder="Include a special character"
          />

          <button type="submit">Create account</button>
          <div className="form-message-slot">
            <ErrorMessage message={localError || (status?.type === 'error' ? status.message : '')} />
            {status?.type === 'success' && <p className="status-text">{status.message}</p>}
          </div>
        </form>

        <div className="form-links">
          <button type="button" className="link-button" onClick={() => onNavigate('login')}>
            Already have an account
          </button>
          <button type="button" className="link-button" onClick={() => onNavigate('home')}>
            Back to landing
          </button>
        </div>
      </div>
    </section>
  );
}

export default RegisterPage;
