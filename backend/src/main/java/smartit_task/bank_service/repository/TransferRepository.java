package smartit_task.bank_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import smartit_task.bank_service.entity.Transfer;
import smartit_task.bank_service.entity.TransferType;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransferRepository extends JpaRepository<Transfer, Long> {
    @Query("SELECT t FROM Transfer t WHERE t.accountId = :accountId OR t.beneficiaryAccountId = :accountId ORDER BY t.createdOn DESC")
    List<Transfer> findAllTransfersByAccountId(@Param("accountId") Long accountId);

    Optional<Transfer> findByAccountIdAndIdempotencyKeyAndType(
            Long accountId, String idempotencyKey, TransferType type);

}
