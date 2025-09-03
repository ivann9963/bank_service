import React, { useState } from 'react';
import AccountList from './components/AccountList';
import AccountForm from './components/AccountForm';
import TransferList from './components/TransferList';
import TransferForm from './components/TransferForm';

type ActiveView = 'accounts' | 'transfers' | 'create-account' | 'create-transfer';

const App: React.FC = () => {
  const [activeView, setActiveView] = useState<ActiveView>('accounts');
  const [refreshKey, setRefreshKey] = useState(0);

  const handleRefresh = () => {
    setRefreshKey(prev => prev + 1);
  };

  const renderContent = () => {
    switch (activeView) {
      case 'accounts':
        return <AccountList key={refreshKey} />;
      case 'transfers':
        return <TransferList key={refreshKey} />;
      case 'create-account':
        return (
          <AccountForm 
            onAccountCreated={() => {
              handleRefresh();
              setActiveView('accounts');
            }} 
          />
        );
      case 'create-transfer':
        return (
          <TransferForm 
            onTransferCreated={() => {
              handleRefresh();
              setActiveView('transfers');
            }} 
          />
        );
      default:
        return <AccountList key={refreshKey} />;
    }
  };

  return (
    <div>
      <nav className="navigation">
        <ul>
          <li>
            <a 
              href="#" 
              className={activeView === 'accounts' ? 'active' : ''}
              onClick={(e) => {
                e.preventDefault();
                setActiveView('accounts');
              }}
            >
              Accounts
            </a>
          </li>
          <li>
            <a 
              href="#" 
              className={activeView === 'transfers' ? 'active' : ''}
              onClick={(e) => {
                e.preventDefault();
                setActiveView('transfers');
              }}
            >
              Transfers
            </a>
          </li>
          <li>
            <a 
              href="#" 
              className={activeView === 'create-account' ? 'active' : ''}
              onClick={(e) => {
                e.preventDefault();
                setActiveView('create-account');
              }}
            >
              Create Account
            </a>
          </li>
          <li>
            <a 
              href="#" 
              className={activeView === 'create-transfer' ? 'active' : ''}
              onClick={(e) => {
                e.preventDefault();
                setActiveView('create-transfer');
              }}
            >
              Create Transfer
            </a>
          </li>
        </ul>
      </nav>

      <div className="container">
        <h1>Bank Management System</h1>
        {renderContent()}
      </div>
    </div>
  );
};

export default App;
