package smartit_task.bank_service.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import smartit_task.bank_service.dto.AccountCreateRequest;
import smartit_task.bank_service.entity.Account;
import smartit_task.bank_service.entity.AccountStatus;
import smartit_task.bank_service.repository.AccountRepository;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;

    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }

    public Optional<Account> getAccountById(Long id) {
        return accountRepository.findById(id);
    }

    public Account createAccount(AccountCreateRequest req) {
        final String name = req.name().trim();
        final String iban = req.iban().trim();

        if (accountRepository.existsByName(name)) {
            throw new IllegalArgumentException("Account with name '" + name + "' already exists");
        }
        if (accountRepository.existsByIban(iban)) {
            throw new IllegalArgumentException("Account with IBAN '" + iban + "' already exists");
        }

        try {
            Account a = new Account();
            a.setName(req.name());
            a.setIban(req.iban());
            a.setAvailableAmount(req.initialAmount() == null ? BigDecimal.ZERO : req.initialAmount());
            a.setStatus(AccountStatus.ACTIVE);
            return accountRepository.save(a);
        } catch (DataIntegrityViolationException ex) {
            throw new IllegalArgumentException("Account with same name or IBAN already exists", ex);
        }
    }

    public List<Account> createAccounts(List<AccountCreateRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            throw new IllegalArgumentException("No accounts provided");
        }

        var names = new HashSet<String>();
        var ibans = new HashSet<String>();

        for (AccountCreateRequest r : requests) {
            if (r.name() == null || r.name().isBlank()) {
                throw new IllegalArgumentException("Name is required for all accounts");
            }
            if (r.iban() == null || r.iban().isBlank()) {
                throw new IllegalArgumentException("IBAN is required for all accounts");
            }
            if (!names.add(r.name())) {
                throw new IllegalArgumentException("Duplicate account name in request: " + r.name());
            }
            if (!ibans.add(r.iban())) {
                throw new IllegalArgumentException("Duplicate IBAN in request: " + r.iban());
            }
            if (r.initialAmount() != null && r.initialAmount().signum() < 0) {
                throw new IllegalArgumentException("Initial amount must be >= 0 for account: " + r.name());
            }
        }

        for (String name : names) {
            if (accountRepository.existsByName(name)) {
                throw new IllegalArgumentException("Account with name '" + name + "' already exists");
            }
        }
        for (String iban : ibans) {
            if (accountRepository.existsByIban(iban)) {
                throw new IllegalArgumentException("Account with IBAN '" + iban + "' already exists");
            }
        }

        var entities = requests.stream().map(r -> {
            var a = new Account();
            a.setName(r.name());
            a.setIban(r.iban());
            a.setStatus(AccountStatus.ACTIVE);
            a.setAvailableAmount(r.initialAmount() == null ? BigDecimal.ZERO : r.initialAmount());
            return a;
        }).toList();

        return accountRepository.saveAll(entities);
    }


    public Account updateAccount(Long id, Account accountDetails) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Account not found with id: " + id));

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

    public Account freezeAccount(Long id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Account not found with id: " + id));

        account.setStatus(AccountStatus.FROZEN);
        return accountRepository.save(account);
    }

    public Account unfreezeAccount(Long id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Account not found with id: " + id));

        account.setStatus(AccountStatus.ACTIVE);
        return accountRepository.save(account);
    }

    public void deleteAccount(Long id) {
        if (!accountRepository.existsById(id)) {
            throw new IllegalArgumentException("Account not found with id: " + id);
        }
        accountRepository.deleteById(id);
    }
}