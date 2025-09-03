package smartit_task.bank_service.service;

import org.springframework.test.context.ActiveProfiles;
import smartit_task.bank_service.entity.Account;
import smartit_task.bank_service.entity.AccountStatus;
import smartit_task.bank_service.entity.Transfer;
import smartit_task.bank_service.repository.AccountRepository;
import smartit_task.bank_service.repository.TransferRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static smartit_task.bank_service.entity.TransferType.CREDIT;
import static smartit_task.bank_service.entity.TransferType.DEBIT;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class TransferServiceTest {

    @Mock AccountRepository accountRepository;
    @Mock TransferRepository transferRepository;

    @InjectMocks TransferService transferService;

    Long fromId, toId;
    Account from, to;

    @BeforeEach
    void setUp() {
        fromId = 1L;
        toId   = 2L;

        from = new Account();
        from.setId(fromId);
        from.setName("Alice");
        from.setIban("BG11TEST00000000000001");
        from.setStatus(AccountStatus.ACTIVE);
        from.setAvailableAmount(new BigDecimal("100.00"));

        to = new Account();
        to.setId(toId);
        to.setName("Bob");
        to.setIban("BG11TEST00000000000002");
        to.setStatus(AccountStatus.ACTIVE);
        to.setAvailableAmount(new BigDecimal("50.00"));

        lenient().when(accountRepository.lockById(fromId)).thenReturn(Optional.of(from));
        lenient().when(accountRepository.lockById(toId)).thenReturn(Optional.of(to));
    }

    @Test /** Happy path */
    void createsTransfer_movesMoney_andPersistsTwoRows() {
        transferService.createTransfer(fromId, toId, new BigDecimal("25.00"));

        assertThat(from.getAvailableAmount()).isEqualByComparingTo("75.00");
        assertThat(to.getAvailableAmount()).isEqualByComparingTo("75.00");

        verify(accountRepository).save(from);
        verify(accountRepository).save(to);

        ArgumentCaptor<Transfer> cap = ArgumentCaptor.forClass(Transfer.class);
        verify(transferRepository, times(2)).save(cap.capture());
        List<Transfer> saved = cap.getAllValues();

        assertThat(saved).hasSize(2);
        assertThat(saved).extracting(Transfer::getType)
                .containsExactlyInAnyOrder(DEBIT, CREDIT);
        assertThat(saved).allSatisfy(t ->
                assertThat(t.getAmount()).isEqualByComparingTo("25.00"));

        boolean hasDebitFromTo = saved.stream().anyMatch(t ->
                t.getType() == DEBIT &&
                        t.getAccountId().equals(fromId) &&
                        t.getBeneficiaryAccountId().equals(toId));

        boolean hasCreditToFrom = saved.stream().anyMatch(t ->
                t.getType() == CREDIT &&
                        t.getAccountId().equals(toId) &&
                        t.getBeneficiaryAccountId().equals(fromId));

        assertThat(hasDebitFromTo).isTrue();
        assertThat(hasCreditToFrom).isTrue();
    }

    @Test
    void fails_onSameAccount() {
        assertThatThrownBy(() ->
                transferService.createTransfer(fromId, fromId, new BigDecimal("10.00"))
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cannot transfer to the same account");

        verifyNoInteractions(transferRepository);
        verify(accountRepository, never()).save(any());
    }

    @Test
    void fails_onNonPositiveAmount() {
        assertThatThrownBy(() ->
                transferService.createTransfer(fromId, toId, new BigDecimal("0.00"))
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Transfer amount must be positive");

        verifyNoInteractions(transferRepository);
        verify(accountRepository, never()).save(any());
    }

    @Test
    void fails_onFrozenFrom() {
        from.setStatus(AccountStatus.FROZEN);

        assertThatThrownBy(() ->
                transferService.createTransfer(fromId, toId, new BigDecimal("10.00"))
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Source account is frozen");

        verifyNoInteractions(transferRepository);
        verify(accountRepository, never()).save(any());
    }

    @Test
    void fails_onFrozenTo() {
        to.setStatus(AccountStatus.FROZEN);

        assertThatThrownBy(() ->
                transferService.createTransfer(fromId, toId, new BigDecimal("10.00"))
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Destination account is frozen");

        verifyNoInteractions(transferRepository);
        verify(accountRepository, never()).save(any());
    }

    @Test
    void fails_onInsufficientFunds() {
        assertThatThrownBy(() ->
                transferService.createTransfer(fromId, toId, new BigDecimal("1000.00"))
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Insufficient funds");

        verifyNoInteractions(transferRepository);
        verify(accountRepository, never()).save(any());
    }

    @Test
    void fails_whenAccountNotFound_firstLock() {
        Long missing = 999L;
        when(accountRepository.lockById(missing)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                transferService.createTransfer(missing, toId, new BigDecimal("5.00"))
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Account not found with id: " + missing);

        verifyNoInteractions(transferRepository);
    }

    @Test
    void fails_whenAccountNotFound_secondLock() {
        when(accountRepository.lockById(toId)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                transferService.createTransfer(fromId, toId, new BigDecimal("5.00"))
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Account not found with id: " + toId);

        verifyNoInteractions(transferRepository);
    }

    // ---------- Idempotency ----------

    @Test
    void idempotencyKey_returnsPriorWithoutDoingWork() {
        String key = "IDEM-123";
        Transfer prior = new Transfer(fromId, toId, DEBIT, new BigDecimal("10.00"));
        prior.setId(42L);
        when(transferRepository.findByAccountIdAndIdempotencyKeyAndType(fromId, key, DEBIT))
                .thenReturn(Optional.of(prior));

        Transfer result = transferService.createTransfer(fromId, toId, new BigDecimal("10.00"), key);

        assertThat(result.getId()).isEqualTo(42L);

        verify(accountRepository, never()).lockById(any());
        verify(accountRepository, never()).save(any());
        verify(transferRepository, never()).save(any());
    }

    @Test
    void idempotencyKey_setsKeyOnSavedTransfers() {
        String key = "IDEM-XYZ";

        transferService.createTransfer(fromId, toId, new BigDecimal("5.00"), key);

        ArgumentCaptor<Transfer> cap = ArgumentCaptor.forClass(Transfer.class);
        verify(transferRepository, times(2)).save(cap.capture());
        var saved = cap.getAllValues();

        assertThat(saved).extracting(Transfer::getIdempotencyKey)
                .allMatch(key::equals);
    }

    @Test
    void idempotencyKey_handlesDuplicateSaveByLookingUpPrior() {
        Long fromId = 1L, toId = 2L;
        var from = new Account();
        from.setId(fromId);
        from.setStatus(AccountStatus.ACTIVE);
        from.setAvailableAmount(new BigDecimal("100"));
        var to = new Account();
        to.setId(toId);
        to.setStatus(AccountStatus.ACTIVE);
        to.setAvailableAmount(new BigDecimal("0"));

        when(accountRepository.lockById(fromId)).thenReturn(Optional.of(from));
        when(accountRepository.lockById(toId)).thenReturn(Optional.of(to));

        String idem = "abc-123";
        BigDecimal amount = new BigDecimal("10");

        when(transferRepository.save(argThat(t ->
                t.getType() == DEBIT && t.getAccountId().equals(fromId))))
                .thenThrow(new DataIntegrityViolationException("dup uk_transfers_idem"));

        Transfer prior = new Transfer(fromId, toId, DEBIT, amount);
        prior.setIdempotencyKey(idem);
        when(transferRepository.findByAccountIdAndIdempotencyKeyAndType(fromId, idem, DEBIT))
                .thenReturn(Optional.empty(), Optional.of(prior));

        var result = transferService.createTransfer(fromId, toId, amount, idem);

        assertThat(result).isSameAs(prior);
        verify(transferRepository, times(2))
                .findByAccountIdAndIdempotencyKeyAndType(fromId, idem, DEBIT);
    }

}
