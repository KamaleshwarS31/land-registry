const API_BASE = '/api';

function getHeaders(isFormData = false): HeadersInit {
  const token = localStorage.getItem('auth_token');
  const headers: Record<string, string> = {};
  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }
  if (!isFormData) {
    headers['Content-Type'] = 'application/json';
  }
  return headers;
}

export const api = {
  auth: {
    login: async (login: string, password: string) => {
      const res = await fetch(`${API_BASE}/auth/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ login, password }),
      });
      if (!res.ok) {
        const err = await res.json().catch(() => ({}));
        throw new Error(err.error || 'Login failed');
      }
      return res.json();
    },
    me: async () => {
      const res = await fetch(`${API_BASE}/auth/me`, { headers: getHeaders() });
      if (!res.ok) throw new Error('Not authenticated');
      return res.json();
    },
  },
  parcels: {
    list: async (query = '', status = '', page = 0, size = 10) => {
      const params = new URLSearchParams();
      if (query) params.append('query', query);
      if (status) params.append('status', status);
      params.append('page', page.toString());
      params.append('size', size.toString());
      const res = await fetch(`${API_BASE}/parcels?${params.toString()}`, { headers: getHeaders() });
      if (!res.ok) throw new Error('Failed to load parcels');
      return res.json();
    },
    get: async (id: string) => {
      const res = await fetch(`${API_BASE}/parcels/${id}`, { headers: getHeaders() });
      if (!res.ok) throw new Error('Parcel not found');
      return res.json();
    },
    myHoldings: async () => {
      const res = await fetch(`${API_BASE}/parcels/my-holdings`, { headers: getHeaders() });
      if (!res.ok) throw new Error('Failed to load holdings');
      return res.json();
    },
    getLineage: async (id: string) => {
      const res = await fetch(`${API_BASE}/parcels/${id}/lineage`, { headers: getHeaders() });
      if (!res.ok) throw new Error('Failed to load parcel lineage');
      return res.json();
    },
  },
  evidence: {
    upload: async (formData: FormData) => {
      const res = await fetch(`${API_BASE}/evidence/upload`, {
        method: 'POST',
        headers: getHeaders(true),
        body: formData,
      });
      if (!res.ok) throw new Error('Evidence upload failed');
      return res.json();
    },
    getForParcel: async (parcelId: string) => {
      const res = await fetch(`${API_BASE}/evidence/parcel/${parcelId}`, { headers: getHeaders() });
      if (!res.ok) throw new Error('Failed to load evidence');
      return res.json();
    },
    getUrl: async (id: string) => {
      const res = await fetch(`${API_BASE}/evidence/${id}/url`, { headers: getHeaders() });
      if (!res.ok) throw new Error('Failed to get signed URL');
      return res.json();
    },
  },
  mutations: {
    list: async (page = 0, size = 20) => {
      const res = await fetch(`${API_BASE}/mutations?page=${page}&size=${size}`, { headers: getHeaders() });
      if (!res.ok) throw new Error('Failed to load mutations');
      return res.json();
    },
    myRequests: async () => {
      const res = await fetch(`${API_BASE}/mutations/my-requests`, { headers: getHeaders() });
      if (!res.ok) throw new Error('Failed to load user mutation requests');
      return res.json();
    },
    get: async (id: string) => {
      const res = await fetch(`${API_BASE}/mutations/${id}`, { headers: getHeaders() });
      if (!res.ok) throw new Error('Failed to load mutation');
      return res.json();
    },
    submit: async (data: any) => {
      const res = await fetch(`${API_BASE}/mutations/submit`, {
        method: 'POST',
        headers: getHeaders(),
        body: JSON.stringify(data),
      });
      if (!res.ok) throw new Error('Failed to submit mutation');
      return res.json();
    },
    evaluate: async (id: string) => {
      const res = await fetch(`${API_BASE}/mutations/${id}/evaluate`, {
        method: 'POST',
        headers: getHeaders(),
      });
      if (!res.ok) throw new Error('Evaluation failed');
      return res.json();
    },
    review: async (id: string, action: string, reason: string) => {
      const res = await fetch(`${API_BASE}/mutations/${id}/review`, {
        method: 'POST',
        headers: getHeaders(),
        body: JSON.stringify({ action, reason }),
      });
      if (!res.ok) throw new Error('Review submission failed');
      return res.json();
    },
    commit: async (id: string) => {
      const res = await fetch(`${API_BASE}/mutations/${id}/commit`, {
        method: 'POST',
        headers: getHeaders(),
      });
      if (!res.ok) {
        const err = await res.json().catch(() => ({}));
        throw new Error(err.message || 'On-chain commit failed');
      }
      return res.json();
    },
  },
  certificates: {
    get: async (id: string) => {
      const res = await fetch(`${API_BASE}/certificates/${id}`, { headers: getHeaders() });
      if (!res.ok) throw new Error('Certificate not found');
      return res.json();
    },
    verify: async (id: string, payload?: string) => {
      const res = await fetch(`${API_BASE}/certificates/${id}/verify`, {
        method: 'POST',
        headers: getHeaders(),
        body: payload ? JSON.stringify({ payload }) : undefined,
      });
      if (!res.ok) throw new Error('Failed to verify certificate');
      return res.json();
    },
  },
  admin: {
    getHealth: async () => {
      const res = await fetch(`${API_BASE}/admin/system-health`, { headers: getHeaders() });
      if (!res.ok) throw new Error('Failed to load health status');
      return res.json();
    },
    resetDemo: async () => {
      const res = await fetch(`${API_BASE}/admin/demo-reset`, {
        method: 'POST',
        headers: getHeaders(),
      });
      if (!res.ok) throw new Error('Failed to reset demo dataset');
      return res.json();
    },
    getUsers: async () => {
      const res = await fetch(`${API_BASE}/admin/users`, { headers: getHeaders() });
      if (!res.ok) throw new Error('Failed to load users');
      return res.json();
    },
  },
};
