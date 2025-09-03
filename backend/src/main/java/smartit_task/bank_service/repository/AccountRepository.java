package smartit_task.bank_service.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import smartit_task.bank_service.entity.Account;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long>{
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Account a where a.id = :id")
    Optional<Account> lockById(@Param("id") Long id);

    Optional<Account> findByName(String name);
    Optional<Account> findByIban(String iban);
    boolean existsByName(String name);
    boolean existsByIban(String iban);
}
