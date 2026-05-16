import { useState } from 'react';
import ErrorMessage from '../components/ErrorMessage';
import FormField from '../components/FormField';

const emptyEventForm = {
  title: '',
  description: '',
  location: '',
  eventDateTime: '',
  maxVolunteers: '5',
  priority: 'MEDIUM'
};

function CreateEventPage({ user, status, error, onCreateEvent, onNavigate }) {
  const [form, setForm] = useState(emptyEventForm);
  const [localError, setLocalError] = useState('');

  async function handleSubmit(event) {
    event.preventDefault();
    setLocalError('');

    if (!form.eventDateTime) {
      setLocalError('Event date and time are required.');
      return;
    }

    if (Number(form.maxVolunteers) < 1) {
      setLocalError('Maximum volunteers must be greater than 0.');
      return;
    }

    const created = await onCreateEvent({
      ...form,
      eventDateTime: new Date(form.eventDateTime).toISOString(),
      maxVolunteers: Number(form.maxVolunteers)
    });

    if (created) {
      setForm(emptyEventForm);
    }
  }

  if (user?.role !== 'ADMIN') {
    return (
      <section className="panel empty-state">
        <h1>Admin access required</h1>
        <p className="muted">Event creation is limited to admin users.</p>
        <button type="button" onClick={() => onNavigate('home')}>Back to dashboard</button>
      </section>
    );
  }

  return (
    <section className="auth-page">
      <div className="panel form-panel">
        <div>
          <h1>Create event</h1>
          <p className="muted">New events appear in the public upcoming events list.</p>
        </div>

        <form onSubmit={handleSubmit}>
          <FormField
            label="Title"
            name="title"
            value={form.title}
            onChange={(event) => setForm({ ...form, title: event.target.value })}
            required
            placeholder="Career Fair"
          />

          <FormField label="Description" name="description">
            <textarea
              name="description"
              value={form.description}
              onChange={(event) => setForm({ ...form, description: event.target.value })}
              required
              placeholder="Describe the event"
            />
          </FormField>

          <FormField
            label="Location"
            name="location"
            value={form.location}
            onChange={(event) => setForm({ ...form, location: event.target.value })}
            required
            placeholder="University Union"
          />

          <div className="two-column-form">
            <FormField
              label="Date and time"
              type="datetime-local"
              name="eventDateTime"
              value={form.eventDateTime}
              onChange={(event) => setForm({ ...form, eventDateTime: event.target.value })}
              required
            />
            <FormField
              label="Maximum volunteers"
              type="number"
              name="maxVolunteers"
              min="1"
              value={form.maxVolunteers}
              onChange={(event) => setForm({ ...form, maxVolunteers: event.target.value })}
              required
            />
          </div>

          <FormField label="Priority" name="priority">
            <select
              name="priority"
              value={form.priority}
              onChange={(event) => setForm({ ...form, priority: event.target.value })}
              required
            >
              <option value="HIGH">High</option>
              <option value="MEDIUM">Medium</option>
              <option value="LOW">Low</option>
            </select>
          </FormField>

          <button type="submit">Create event</button>
          <button type="button" className="secondary-button" onClick={() => onNavigate('home')}>
            Back to dashboard
          </button>
          <ErrorMessage message={localError || error} />
          {status && <p className="status-text">{status}</p>}
        </form>
      </div>
    </section>
  );
}

export default CreateEventPage;
