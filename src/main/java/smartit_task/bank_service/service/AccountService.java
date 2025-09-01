package smartit_task.bank_service.service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import smartit_task.bank_service.entity.Account;
import smartit_task.bank_service.entity.AccountStatus;
import smartit_task.bank_service.repository.AccountRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class AccountService {

    @Autowired
    private AccountRepository accountRepository;

    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }

    public Optional<Account> getAccountById(UUID id) {
        return accountRepository.findById(id);
    }

    public Account createAccount(Account account) {
        // Validate unique name and IBAN
        if (accountRepository.existsByName(account.getName())) {
            throw new IllegalArgumentException("Account with name '" + account.getName() + "' already exists");
        }
        if (accountRepository.existsByIban(account.getIban())) {
            throw new IllegalArgumentException("Account with IBAN '" + account.getIban() + "' already exists");
        }
        return accountRepository.save(account);
    }

    public Account updateAccount(UUID id, Account accountDetails) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Account not found with id: " + id));

        // Check for unique name and IBAN (excluding current account)
        if (!account.getName().equals(accountDetails.getName()) &&
                accountRepository.existsByName(accountDetails.getName())) {
            throw new IllegalArgumentException("Account with name '" + accountDetails.getName() + "' already exists");
        }
        if (!account.getIban().equals(accountDetails.getIban()) &&
                accountRepository.existsByIban(accountDetails.getIban())) {
            throw new IllegalArgumentException("Account with IBAN '" + accountDetails.getIban() + "' already exists");
        }

        account.setName(accountDetails.getName());
        account.setIban(accountDetails.getIban());
        account.setAvailableAmount(accountDetails.getAvailableAmount());

        return accountRepository.save(account);
    }

    public Account freezeAccount(UUID id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Account not found with id: " + id));

        account.setStatus(AccountStatus.FROZEN);
        return accountRepository.save(account);
    }

    public Account unfreezeAccount(UUID id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Account not found with id: " + id));

        account.setStatus(AccountStatus.ACTIVE);
        return accountRepository.save(account);
    }

    public void deleteAccount(UUID id) {
        if (!accountRepository.existsById(id)) {
            throw new IllegalArgumentException("Account not found with id: " + id);
        }
        accountRepository.deleteById(id);
    }
}