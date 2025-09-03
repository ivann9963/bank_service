import React, { useState, useEffect } from 'react';
import { AccountResponse, TransferCreateRequest  } from '../types';
import { transferAPI, accountAPI } from '../services/api';

interface TransferFormProps {
  onTransferCreated: () => void;
}

const TransferForm: React.FC<TransferFormProps> = ({ onTransferCreated }) => {
  const [accounts, setAccounts] = useState<AccountResponse[]>([]);
  const [formData, setFormData] = useState<TransferCreateRequest >({
    fromAccountId: 0,
    toAccountId: 0,
    amount: 0
  });
  const [error, setError] = useState<string>('');
  const [success, setSuccess] = useState<string>('');
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    fetchAccounts();
  }, []);

  const fetchAccounts = async () => {
    try {
      const response = await accountAPI.getAll();
      setAccounts(response.data.filter(acc => acc.status === 'ACTIVE'));
    } catch (err) {
      setError('Failed to fetch accounts');
      console.error('Error fetching accounts:', err);
    }
  };

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: name === 'amount' ? parseFloat(value) || 0 : parseInt(value) || 0
    }));
  };

  const toMessage = (err: any): string => {
    const data = err?.response?.data;
    if (!data) return err?.message || 'Request failed';
    if (typeof data === 'string') return data;
    if (typeof data?.message === 'string') return data.message;
    try { return JSON.stringify(data); } catch { return 'Request failed'; }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError('');
    setSuccess('');

    if (formData.fromAccountId === formData.toAccountId) {
      setError('Source and destination accounts must be different');
      setLoading(false);
      return;
    }

    if (formData.amount <= 0) {
      setError('Transfer amount must be positive');
      setLoading(false);
      return;
    }


    try {
      await transferAPI.create(formData);
      setSuccess('Transfer created successfully!');
      setFormData({
        fromAccountId: 0,
        toAccountId: 0,
        amount: 0
      });
      onTransferCreated();
    } catch (err: any) {
      setError(toMessage(err));
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="card">
      <h2>Create Transfer</h2>
      {error && <div className="error">{error}</div>}
      {success && <div className="success">{success}</div>}
      
      <form onSubmit={handleSubmit}>
        <div className="form-group">
          <label htmlFor="fromAccountId">From Account:</label>
          <select
            id="fromAccountId"
            name="fromAccountId"
            value={formData.fromAccountId}
            onChange={handleChange}
            required
          >
            <option value={0}>Select source account</option>
            {accounts.map((account) => (
              <option key={account.id} value={account.id}>
                {account.name} - ${account.availableAmount.toFixed(2)}
              </option>
            ))}
          </select>
        </div>

        <div className="form-group">
          <label htmlFor="toAccountId">To Account:</label>
          <select
            id="toAccountId"
            name="toAccountId"
            value={formData.toAccountId}
            onChange={handleChange}
            required
          >
            <option value={0}>Select destination account</option>
            {accounts.map((account) => (
              <option key={account.id} value={account.id}>
                {account.name} ({account.iban})
              </option>
            ))}
          </select>
        </div>

        <div className="form-group">
          <label htmlFor="amount">Amount:</label>
          <input
            type="number"
            id="amount"
            name="amount"
            value={formData.amount}
            onChange={handleChange}
            min="0.01"
            step="0.01"
            required
          />
        </div>

        <button type="submit" className="button" disabled={loading}>
          {loading ? 'Processing...' : 'Create Transfer'}
        </button>
      </form>
    </div>
  );
};

export default TransferForm;
