package smartit_task.bank_service.mapper;

import smartit_task.bank_service.dto.TransferResponse;
import smartit_task.bank_service.entity.Transfer;

import java.util.Collection;
import java.util.List;

public final class TransferMapper {

    private TransferMapper() {}

    public static TransferResponse toResponse(Transfer t) {
        if (t == null) return null;
        return new TransferResponse(
                t.getId(),
                t.getAccountId(),
                t.getBeneficiaryAccountId(),
                t.getType(),
                t.getAmount(),
                t.getCreatedOn(),
                t.getModifiedOn()
        );
    }

    public static List<TransferResponse> toResponseList(Collection<Transfer> transfers) {
        if (transfers == null || transfers.isEmpty()) return List.of();
        return transfers.stream().map(TransferMapper::toResponse).toList();
    }
}
