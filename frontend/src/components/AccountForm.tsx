import React, { useState } from 'react';
import { AccountCreateRequest  } from '../types';
import { accountAPI } from '../services/api';

interface AccountFormProps {
  onAccountCreated: () => void;
}

const AccountForm: React.FC<AccountFormProps> = ({ onAccountCreated }) => {
  const [formData, setFormData] = useState<Omit<AccountCreateRequest , 'id' | 'createdOn' | 'modifiedOn'>>({
    name: '',
    iban: '',
    initialAmount: undefined
  });
  const [error, setError] = useState<string>('');
  const [success, setSuccess] = useState<string>('');
  const [loading, setLoading] = useState(false);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: name === 'initialAmount' ? parseFloat(value) || undefined : value
    }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError('');
    setSuccess('');

    try {
      await accountAPI.create(formData);
      setSuccess('Account created successfully!');
      setFormData({
        name: '',
        iban: '',
        initialAmount: 0
      });
      onAccountCreated();
    } catch (err: any) {
      const data = err?.response?.data;
      const msg =
        typeof data === 'string' ? data :
        data?.message ? data.message :
        data?.errors ? Object.values(data.errors).join(', ') :
        data?.error ? data.error :
        err?.message || 'Request failed';
      setError(msg);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="card">
      <h2>Create New Account</h2>
      {error && <div className="error">{error}</div>}
      {success && <div className="success">{success}</div>}
      
      <form onSubmit={handleSubmit}>
        <div className="form-group">
          <label htmlFor="name">Account Name:</label>
          <input
            type="text"
            id="name"
            name="name"
            value={formData.name}
            onChange={handleChange}
            required
          />
        </div>

        <div className="form-group">
          <label htmlFor="iban">IBAN:</label>
          <input
            type="text"
            id="iban"
            name="iban"
            value={formData.iban}
            onChange={handleChange}
            placeholder="e.g., GB29NWBK60161331926819"
            required
          />
        </div>

        <div className="form-group">
          <label htmlFor="initialAmount">Initial Amount:</label>
          <input
            type="number"
            id="initialAmount"
            name="initialAmount"
            value={formData.initialAmount}
            onChange={handleChange}
            min="0"
            step="0.01"
            required
          />
        </div>

        <button type="submit" className="button" disabled={loading}>
          {loading ? 'Creating...' : 'Create Account'}
        </button>
      </form>
    </div>
  );
};

export default AccountForm;
