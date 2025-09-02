package smartit_task.bank_service.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
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

    public Transfer createTransfer(Long fromAccountId, Long toAccountId, BigDecimal amount) {
        // Validate accounts exist
        Account fromAccount = accountRepository.findById(fromAccountId)
                .orElseThrow(() -> new IllegalArgumentException("Source account not found with id: " + fromAccountId));

        Account toAccount = accountRepository.findById(toAccountId)
                .orElseThrow(() -> new IllegalArgumentException("Destination account not found with id: " + toAccountId));

        // Validate accounts are active
        if (fromAccount.getStatus() == AccountStatus.FROZEN) {
            throw new IllegalArgumentException("Source account is frozen");
        }
        if (toAccount.getStatus() == AccountStatus.FROZEN) {
            throw new IllegalArgumentException("Destination account is frozen");
        }

        // Validate sufficient funds
        if (fromAccount.getAvailableAmount().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient funds in source account");
        }

        // Validate amount is positive
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Transfer amount must be positive");
        }

        if (fromAccountId.equals(toAccountId)) {
            throw new IllegalArgumentException("Cannot transfer to the same account");
        }

        // Create debit transfer for source account
        Transfer debitTransfer = new Transfer(fromAccountId, toAccountId, TransferType.DEBIT, amount);

        // Create credit transfer for destination account
        Transfer creditTransfer = new Transfer(toAccountId, fromAccountId, TransferType.CREDIT, amount);

        // Update account balances
        fromAccount.setAvailableAmount(fromAccount.getAvailableAmount().subtract(amount));
        toAccount.setAvailableAmount(toAccount.getAvailableAmount().add(amount));

        // Save transfers and updated accounts
        transferRepository.save(debitTransfer);
        transferRepository.save(creditTransfer);
        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);

        return debitTransfer; // Return the debit transfer as the main transfer record
    }

    public List<Transfer> getAllTransfers() {
        return transferRepository.findAll();
    }
}