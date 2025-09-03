import React, { useState, useEffect } from 'react';
import { AccountResponse, TransferResponse  } from '../types';
import { transferAPI, accountAPI } from '../services/api';

interface TransferListProps {
  accountId?: number;
}

const TransferList: React.FC<TransferListProps> = ({ accountId }) => {
  const [transfers, setTransfers] = useState<TransferResponse[]>([]);
  const [accounts, setAccounts] = useState<AccountResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string>('');

  useEffect(() => {
    fetchTransfers();
    fetchAccounts();
  }, [accountId]);

  const fetchTransfers = async () => {
    try {
      setLoading(true);
      const response = accountId 
        ? await transferAPI.getByAccountId(accountId)
        : await transferAPI.getAll();
      setTransfers(response.data);
      setError('');
    } catch (err) {
      setError('Failed to fetch transfers');
      console.error('Error fetching transfers:', err);
    } finally {
      setLoading(false);
    }
  };

  const fetchAccounts = async () => {
    try {
      const response = await accountAPI.getAll();
      setAccounts(response.data);
    } catch (err) {
      console.error('Error fetching accounts:', err);
    }
  };

  const getAccountName = (accountId: number) => {
    const account = accounts.find(acc => acc.id === accountId);
    return account ? account.name : `Account ${accountId}`;
  };

  if (loading) return <div>Loading transfers...</div>;

  return (
    <div className="card">
      <h2>{accountId ? `Transfers for Account ${accountId}` : 'All Transfers'}</h2>
      {error && <div className="error">{error}</div>}
      
      {transfers.length === 0 ? (
        <p>No transfers found.</p>
      ) : (
        <table className="table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Type</th>
              <th>Account</th>
              <th>Beneficiary Account</th>
              <th>Amount</th>
              <th>Date</th>
            </tr>
          </thead>
          <tbody>
            {transfers.map((transfer) => (
              <tr key={transfer.id}>
                <td>{transfer.id}</td>
                <td className={`status-${transfer.type.toLowerCase()}`}>
                  {transfer.type}
                </td>
                <td>{getAccountName(transfer.accountId)}</td>
                <td>{getAccountName(transfer.beneficiaryAccountId)}</td>
                <td>${transfer.amount.toFixed(2)}</td>
                <td>{transfer.createdOn ? new Date(transfer.createdOn).toLocaleDateString() : ''}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
};

export default TransferList;
