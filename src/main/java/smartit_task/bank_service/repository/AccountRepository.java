package smartit_task.bank_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import smartit_task.bank_service.entity.Account;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long>{
    Optional<Account> findByName(String name);
    Optional<Account> findByIban(String iban);
    boolean existsByName(String name);
    boolean existsByIban(String iban);
}
