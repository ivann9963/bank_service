import React, { useState, useEffect } from 'react';
import { AccountResponse  } from '../types';
import { accountAPI } from '../services/api';

const AccountList: React.FC = () => {
  const [accounts, setAccounts] = useState<AccountResponse []>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string>('');

  useEffect(() => {
    fetchAccounts();
  }, []);

  const fetchAccounts = async () => {
    try {
      setLoading(true);
      const response = await accountAPI.getAll();
      setAccounts(response.data);
      setError('');
    } catch (err) {
      setError('Failed to fetch accounts');
      console.error('Error fetching accounts:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleFreeze = async (id: number) => {
    try {
      await accountAPI.freeze(id);
      fetchAccounts();
    } catch (err) {
      setError('Failed to freeze account');
      console.error('Error freezing account:', err);
    }
  };

  const handleUnfreeze = async (id: number) => {
    try {
      await accountAPI.unfreeze(id);
      fetchAccounts();
    } catch (err) {
      setError('Failed to unfreeze account');
      console.error('Error unfreezing account:', err);
    }
  };

  if (loading) return <div>Loading accounts...</div>;

  return (
    <div className="card">
      <h2>Bank Accounts</h2>
      {error && <div className="error">{error}</div>}
      
      {accounts.length === 0 ? (
        <p>No accounts found. Create your first account!</p>
      ) : (
        <table className="table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Name</th>
              <th>IBAN</th>
              <th>Status</th>
              <th>Available Amount</th>
              <th>Created On</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {accounts.map((account) => (
              <tr key={account.id}>
                <td>{account.id}</td>
                <td>{account.name}</td>
                <td>{account.iban}</td>
                <td className={`status-${account.status.toLowerCase()}`}>
                  {account.status}
                </td>
                <td>${account.availableAmount.toFixed(2)}</td>
                <td>{account.createdOn ? new Date(account.createdOn).toLocaleDateString() : ''}</td>
                <td>
                  {account.status === 'ACTIVE' ? (
                    <button 
                      className="button danger" 
                      onClick={() => handleFreeze(account.id!)}
                    >
                      Freeze
                    </button>
                  ) : (
                    <button 
                      className="button success" 
                      onClick={() => handleUnfreeze(account.id!)}
                    >
                      Unfreeze
                    </button>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
};

export default AccountList;
