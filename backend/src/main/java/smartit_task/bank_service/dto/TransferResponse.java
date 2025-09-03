package smartit_task.bank_service.dto;

import smartit_task.bank_service.entity.TransferType;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransferResponse(
        Long id,
        Long accountId,
        Long beneficiaryAccountId,
        TransferType type,
        BigDecimal amount,
        LocalDateTime createdOn,
        LocalDateTime modifiedOn
) {}
