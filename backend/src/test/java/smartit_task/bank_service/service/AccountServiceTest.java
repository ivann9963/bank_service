package smartit_task.bank_service.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import smartit_task.bank_service.dto.AccountCreateRequest;
import smartit_task.bank_service.entity.Account;
import smartit_task.bank_service.entity.AccountStatus;
import smartit_task.bank_service.repository.AccountRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class AccountServiceTest {

    @Mock AccountRepository accountRepository;
    @InjectMocks AccountService accountService;

    Account existing;

    @BeforeEach
    void setUp() {
        existing = new Account();
        existing.setId(1L);
        existing.setName("Alice");
        existing.setIban("BG11TEST00000000000001");
        existing.setStatus(AccountStatus.ACTIVE);
        existing.setAvailableAmount(new BigDecimal("100.00"));
    }

    @Test
    void getAllAccounts_delegatesToRepo() {
        when(accountRepository.findAll()).thenReturn(List.of(existing));
        assertThat(accountService.getAllAccounts()).containsExactly(existing);
    }

    @Test
    void getById_delegatesToRepo() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(existing));
        assertThat(accountService.getAccountById(1L)).containsSame(existing);
    }

    @Test
    void createAccount_success_defaultsZeroAndActive() {
        var req = new AccountCreateRequest("Bob", "BG22TEST00000000000002", null);

        when(accountRepository.existsByName("Bob")).thenReturn(false);
        when(accountRepository.existsByIban("BG22TEST00000000000002")).thenReturn(false);
        when(accountRepository.save(any(Account.class))).thenAnswer(inv -> {
            Account a = inv.getArgument(0);
            a.setId(2L);
            return a;
        });

        Account saved = accountService.createAccount(req);

        assertThat(saved.getId()).isEqualTo(2L);
        assertThat(saved.getName()).isEqualTo("Bob");
        assertThat(saved.getIban()).isEqualTo("BG22TEST00000000000002");
        assertThat(saved.getStatus()).isEqualTo(AccountStatus.ACTIVE);
        assertThat(saved.getAvailableAmount()).isEqualByComparingTo("0.00");
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    void createAccount_rejectsDuplicateName_precheck() {
        var req = new AccountCreateRequest("Alice", "BG22TEST00000000000002", BigDecimal.TEN);
        when(accountRepository.existsByName("Alice")).thenReturn(true);

        assertThatThrownBy(() -> accountService.createAccount(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Account with name 'Alice' already exists");

        verify(accountRepository, never()).save(any());
    }

    @Test
    void createAccount_rejectsDuplicateIban_precheck() {
        var req = new AccountCreateRequest("Bob", "BG11TEST00000000000001", BigDecimal.TEN);
        when(accountRepository.existsByName("Bob")).thenReturn(false);
        when(accountRepository.existsByIban("BG11TEST00000000000001")).thenReturn(true);

        assertThatThrownBy(() -> accountService.createAccount(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Account with IBAN 'BG11TEST00000000000001' already exists");

        verify(accountRepository, never()).save(any());
    }

    @Test
    void createAccount_conflictOnSave_mapsToNiceMessage() {
        var req = new AccountCreateRequest("Bob", "BG22TEST00000000000002", BigDecimal.ONE);
        when(accountRepository.existsByName("Bob")).thenReturn(false);
        when(accountRepository.existsByIban("BG22TEST00000000000002")).thenReturn(false);
        when(accountRepository.save(any(Account.class)))
                .thenThrow(new DataIntegrityViolationException("unique_violation"));

        assertThatThrownBy(() -> accountService.createAccount(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Account with same name or IBAN already exists");
    }

    @Test
    void createAccounts_success_mapsAll_defaultsZeroForNull() {
        var r1 = new AccountCreateRequest("Bob",  "BG22", null);
        var r2 = new AccountCreateRequest("Cara", "BG33", new BigDecimal("12.34"));

        when(accountRepository.existsByName(anyString())).thenReturn(false);
        when(accountRepository.existsByIban(anyString())).thenReturn(false);
        when(accountRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

        var saved = accountService.createAccounts(List.of(r1, r2));

        assertThat(saved).hasSize(2);
        assertThat(saved.get(0).getName()).isEqualTo("Bob");
        assertThat(saved.get(0).getAvailableAmount()).isEqualByComparingTo("0.00");
        assertThat(saved.get(1).getName()).isEqualTo("Cara");
        assertThat(saved.get(1).getAvailableAmount()).isEqualByComparingTo("12.34");
        assertThat(saved).allSatisfy(a -> assertThat(a.getStatus()).isEqualTo(AccountStatus.ACTIVE));
    }

    @Test
    void createAccounts_rejectsEmptyList() {
        assertThatThrownBy(() -> accountService.createAccounts(List.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("No accounts provided");
    }

    @Test
    void createAccounts_rejectsDuplicateNamesWithinRequest() {
        var r1 = new AccountCreateRequest("Bob", "BG22", BigDecimal.ONE);
        var r2 = new AccountCreateRequest("Bob", "BG33", BigDecimal.ONE);

        assertThatThrownBy(() -> accountService.createAccounts(List.of(r1, r2)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Duplicate account name in request: Bob");
    }

    @Test
    void createAccounts_rejectsDuplicateIbansWithinRequest() {
        var r1 = new AccountCreateRequest("Bob",  "BG22", BigDecimal.ONE);
        var r2 = new AccountCreateRequest("Cara", "BG22", BigDecimal.ONE);

        assertThatThrownBy(() -> accountService.createAccounts(List.of(r1, r2)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Duplicate IBAN in request: BG22");
    }

    @Test
    void createAccounts_rejectsNegativeInitial() {
        var r1 = new AccountCreateRequest("Bob", "BG22", new BigDecimal("-1"));

        assertThatThrownBy(() -> accountService.createAccounts(List.of(r1)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Initial amount must be >= 0 for account: Bob");
    }

    @Test
    void createAccounts_rejectsExistingNameInDb() {
        var r1 = new AccountCreateRequest("Bob",  "BG22", BigDecimal.ONE);
        var r2 = new AccountCreateRequest("Cara", "BG33", BigDecimal.ONE);

        when(accountRepository.existsByName(anyString()))
                .thenAnswer(inv -> "Bob".equals(inv.getArgument(0)));
        when(accountRepository.existsByIban(anyString())).thenReturn(false);

        assertThatThrownBy(() -> accountService.createAccounts(List.of(r1, r2)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Account with name 'Bob' already exists");
    }

    @Test
    void createAccounts_rejectsExistingIbanInDb() {
        var r1 = new AccountCreateRequest("Bob",  "BG22", BigDecimal.ONE);
        var r2 = new AccountCreateRequest("Cara", "BG33", BigDecimal.ONE);

        when(accountRepository.existsByName(anyString())).thenReturn(false);
        when(accountRepository.existsByIban(anyString()))
                .thenAnswer(inv -> "BG33".equals(inv.getArgument(0)));

        assertThatThrownBy(() -> accountService.createAccounts(List.of(r1, r2)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Account with IBAN 'BG33' already exists");
    }

    @Test
    void updateAccount_success_updatesFields_respectsUniqueness() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(accountRepository.existsByName("NewName")).thenReturn(false);
        when(accountRepository.existsByIban("NEWIBAN")).thenReturn(false);
        when(accountRepository.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));

        var patch = new Account();
        patch.setName("NewName");
        patch.setIban("NEWIBAN");
        patch.setAvailableAmount(new BigDecimal("55.55"));

        Account updated = accountService.updateAccount(1L, patch);

        assertThat(updated.getName()).isEqualTo("NewName");
        assertThat(updated.getIban()).isEqualTo("NEWIBAN");
        assertThat(updated.getAvailableAmount()).isEqualByComparingTo("55.55");
    }

    @Test
    void updateAccount_rejectsDuplicateName() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(accountRepository.existsByName("Taken")).thenReturn(true);

        var patch = new Account();
        patch.setName("Taken");
        patch.setIban(existing.getIban());
        patch.setAvailableAmount(existing.getAvailableAmount());

        assertThatThrownBy(() -> accountService.updateAccount(1L, patch))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Account with name 'Taken' already exists");
    }

    @Test
    void updateAccount_rejectsDuplicateIban() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(accountRepository.existsByIban("TAKEN")).thenReturn(true);

        var patch = new Account();
        patch.setName(existing.getName());
        patch.setIban("TAKEN");
        patch.setAvailableAmount(existing.getAvailableAmount());

        assertThatThrownBy(() -> accountService.updateAccount(1L, patch))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Account with IBAN 'TAKEN' already exists");
    }

    @Test
    void updateAccount_notFound() {
        when(accountRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.updateAccount(99L, new Account()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Account not found with id: 99");
    }

    @Test
    void freezeAccount_setsFrozen() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(accountRepository.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));

        Account a = accountService.freezeAccount(1L);
        assertThat(a.getStatus()).isEqualTo(AccountStatus.FROZEN);
    }

    @Test
    void unfreezeAccount_setsActive() {
        existing.setStatus(AccountStatus.FROZEN);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(accountRepository.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));

        Account a = accountService.unfreezeAccount(1L);
        assertThat(a.getStatus()).isEqualTo(AccountStatus.ACTIVE);
    }

    @Test
    void freeze_unfreeze_notFound() {
        when(accountRepository.findById(123L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.freezeAccount(123L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Account not found with id: 123");

        assertThatThrownBy(() -> accountService.unfreezeAccount(123L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Account not found with id: 123");
    }

    @Test
    void deleteAccount_success() {
        when(accountRepository.existsById(1L)).thenReturn(true);
        accountService.deleteAccount(1L);
        verify(accountRepository).deleteById(1L);
    }

    @Test
    void deleteAccount_notFound() {
        when(accountRepository.existsById(77L)).thenReturn(false);

        assertThatThrownBy(() -> accountService.deleteAccount(77L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Account not found with id: 77");

        verify(accountRepository, never()).deleteById(anyLong());
    }
}
