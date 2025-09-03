package smartit_task.bank_service.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import smartit_task.bank_service.entity.Account;
import smartit_task.bank_service.entity.AccountStatus;
import smartit_task.bank_service.entity.Transfer;
import smartit_task.bank_service.entity.TransferType;
import smartit_task.bank_service.repository.AccountRepository;
import smartit_task.bank_service.repository.TransferRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class TransferService {

    private final AccountRepository accountRepository;
    private final TransferRepository transferRepository;

    public List<Transfer> getAllTransfersByAccountId(Long accountId) {
        return transferRepository.findAllTransfersByAccountId(accountId);
    }

    public Optional<Transfer> getTransferById(Long id) {
        return transferRepository.findById(id);
    }

    public Transfer createTransfer(Long fromAccountId, Long toAccountId, BigDecimal amount, String idemKey) {
        if (fromAccountId.equals(toAccountId)) {
            throw new IllegalArgumentException("Cannot transfer to the same account");
        }

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Transfer amount must be positive");
        }

        if (idemKey != null && !idemKey.isBlank()) {
            var prior = transferRepository.findByAccountIdAndIdempotencyKeyAndType(fromAccountId, idemKey, TransferType.DEBIT);
            if (prior.isPresent()) return prior.get();
        }

        Long firstId  = fromAccountId < toAccountId ? fromAccountId : toAccountId;
        Long secondId = fromAccountId < toAccountId ? toAccountId   : fromAccountId;

        Account firstLocked = accountRepository.lockById(firstId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found with id: " + firstId));
        Account secondLocked = accountRepository.lockById(secondId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found with id: " + secondId));

        Account fromAccount = fromAccountId.equals(firstId) ? firstLocked : secondLocked;
        Account toAccount   = toAccountId.equals(secondId) ? secondLocked : firstLocked;


        if (fromAccount.getStatus() == AccountStatus.FROZEN) {
            throw new IllegalArgumentException("Source account is frozen");
        }
        if (toAccount.getStatus() == AccountStatus.FROZEN) {
            throw new IllegalArgumentException("Destination account is frozen");
        }
        if (fromAccount.getAvailableAmount().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient funds in source account");
        }

        Transfer debitTransfer = new Transfer(fromAccountId, toAccountId, TransferType.DEBIT, amount);
        Transfer creditTransfer = new Transfer(toAccountId, fromAccountId, TransferType.CREDIT, amount);

        if (idemKey != null && !idemKey.isBlank()) {
            debitTransfer.setIdempotencyKey(idemKey);
            creditTransfer.setIdempotencyKey(idemKey);
        }


        fromAccount.setAvailableAmount(fromAccount.getAvailableAmount().subtract(amount));
        toAccount.setAvailableAmount(toAccount.getAvailableAmount().add(amount));


        try {
            transferRepository.save(debitTransfer);
            transferRepository.save(creditTransfer);
            accountRepository.save(fromAccount);
            accountRepository.save(toAccount);
            return debitTransfer;
        } catch (DataIntegrityViolationException dup) {
            if (idemKey != null && !idemKey.isBlank()) {
                return transferRepository
                        .findByAccountIdAndIdempotencyKeyAndType(fromAccountId, idemKey, TransferType.DEBIT)
                        .orElseThrow(() -> dup);
            }
            throw dup;
        }
    }


    public Transfer createTransfer(Long fromAccountId, Long toAccountId, BigDecimal amount) {
        return createTransfer(fromAccountId, toAccountId, amount, null);
    }

    public List<Transfer> getAllTransfers() {
        return transferRepository.findAll();
    }
}