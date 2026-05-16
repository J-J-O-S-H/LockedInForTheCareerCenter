const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api';

async function request(path, options = {}) {
  const { authToken, ...fetchOptions } = options;

  const response = await fetch(`${API_BASE_URL}${path}`, {
    headers: {
      'Content-Type': 'application/json',
      ...(authToken ? { Authorization: `Bearer ${authToken}` } : {}),
      ...fetchOptions.headers
    },
    ...fetchOptions
  });

  const contentType = response.headers.get('content-type') || '';
  const hasJsonBody = contentType.includes('application/json');
  const body = hasJsonBody ? await response.json() : null;

  if (!response.ok) {
    const fieldErrors = body?.fieldErrors || {};
    const message = friendlyErrorMessage(body, fieldErrors, response.status);
    const error = new Error(message);
    error.status = response.status;
    error.fieldErrors = fieldErrors;
    throw error;
  }

  return body;
}

function friendlyErrorMessage(body, fieldErrors, status) {
  if (body?.message && !body.message.toLowerCase().includes('request body')) {
    return body.message;
  }

  if (Object.values(fieldErrors).some((message) => message?.includes('required'))) {
    return 'Please complete all required fields.';
  }

  if (fieldErrors.email) {
    return fieldErrors.email;
  }

  if (fieldErrors.password) {
    return fieldErrors.password;
  }

  const firstFieldMessage = Object.values(fieldErrors)[0];
  if (firstFieldMessage) {
    return firstFieldMessage;
  }

  if (status === 401) {
    return 'Your session expired. Please sign in again.';
  }

  return body?.error || `Request failed with status ${status}`;
}

export const apiClient = {
  getHealth() {
    return request('/health');
  },
  getEvents() {
    return request('/events');
  },
  createEvent(event, token) {
    return request('/events', {
      method: 'POST',
      authToken: token,
      body: JSON.stringify(event)
    });
  },
  deleteEvent(eventId, token) {
    return request(`/events/${eventId}`, {
      method: 'DELETE',
      authToken: token
    });
  },
  registerForEvent(eventId, token) {
    return request(`/events/${eventId}/registrations`, {
      method: 'POST',
      authToken: token
    });
  },
  withdrawFromEvent(eventId, token) {
    return request(`/events/${eventId}/registrations/me`, {
      method: 'DELETE',
      authToken: token
    });
  },
  getMyRegistrations(token) {
    return request('/users/me/registrations', {
      authToken: token
    });
  },
  registerUser(account) {
    return request('/auth/register', {
      method: 'POST',
      body: JSON.stringify(account)
    });
  },
  login(credentials) {
    return request('/auth/login', {
      method: 'POST',
      body: JSON.stringify(credentials)
    });
  },
  getCurrentUser(token) {
    return request('/auth/me', {
      authToken: token
    });
  }
};
