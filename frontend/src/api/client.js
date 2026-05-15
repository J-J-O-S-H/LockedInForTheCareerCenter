const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api';

async function request(path, options = {}) {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    headers: {
      'Content-Type': 'application/json',
      ...options.headers
    },
    ...options
  });

  const contentType = response.headers.get('content-type') || '';
  const hasJsonBody = contentType.includes('application/json');
  const body = hasJsonBody ? await response.json() : null;

  if (!response.ok) {
    const message = body?.message || body?.error || `Request failed with status ${response.status}`;
    throw new Error(message);
  }

  return body;
}

export const apiClient = {
  getHealth() {
    return request('/health');
  },
  getExamples() {
    return request('/example');
  },
  createExample(example) {
    return request('/example', {
      method: 'POST',
      body: JSON.stringify(example)
    });
  },
  login(credentials) {
    return request('/auth/login', {
      method: 'POST',
      body: JSON.stringify(credentials)
    });
  },
  loginWithGoogle(token) {
    return request('/auth/google', {
      method: 'POST',
      body: JSON.stringify({ token })
    });
  }
};
