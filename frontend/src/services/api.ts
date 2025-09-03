// src/api/api.ts (or wherever your API file lives)
import axios from 'axios';
import {
  AccountResponse,
  AccountCreateRequest,
  AccountUpdateRequest,
  TransferResponse,
  TransferCreateRequest,
} from '../types';

const API_BASE_URL = 'http://localhost:8080/api';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: { 'Content-Type': 'application/json' },
  // auth: { username: 'admin', password: 'admin' }, // dev-only
});

// Account API
export const accountAPI = {
  getAll: () => api.get<AccountResponse[]>('/accounts'),
  getById: (id: number) => api.get<AccountResponse>(`/accounts/${id}`),

  create: (payload: AccountCreateRequest) =>
    api.post<AccountResponse>('/accounts', payload),

  // if you exposed POST /api/accounts/batch on the backend
  createBatch: (payloads: AccountCreateRequest[]) =>
    api.post<AccountResponse[]>('/accounts/batch', payloads),

  update: (id: number, payload: AccountUpdateRequest) =>
    api.put<AccountResponse>(`/accounts/${id}`, payload),

  freeze: (id: number) => api.put<AccountResponse>(`/accounts/${id}/freeze`),
  unfreeze: (id: number) => api.put<AccountResponse>(`/accounts/${id}/unfreeze`),

  delete: (id: number) => api.delete<void>(`/accounts/${id}`),
};

// Transfer API
export const transferAPI = {
  getByAccountId: (accountId: number) =>
    api.get<TransferResponse[]>(`/transfers/account/${accountId}`),

  getById: (id: number) => api.get<TransferResponse>(`/transfers/${id}`),

  create: (payload: TransferCreateRequest) =>
    api.post<TransferResponse>('/transfers', payload),

  // keep only if your backend supports GET /api/transfers
  getAll: () => api.get<TransferResponse[]>('/transfers'),
};
