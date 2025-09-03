package smartit_task.bank_service.service;

import org.springframework.test.context.ActiveProfiles;
import smartit_task.bank_service.entity.Account;
import smartit_task.bank_service.entity.AccountStatus;
import smartit_task.bank_service.repository.AccountRepository;
import smartit_task.bank_service.repository.TransferRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Import(TransferService.class)
class TransferServiceConcurrencyIT {

    @Autowired AccountRepository accountRepository;
    @Autowired TransferService transferService;

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void concurrent_transfers_are_serialized_by_row_locks() throws Exception {
        // Arrange accounts
        Account from = new Account();
        from.setName("From");
        from.setIban("BG00FROM00000000000001");
        from.setStatus(AccountStatus.ACTIVE);
        from.setAvailableAmount(new BigDecimal("100.00"));
        from = accountRepository.save(from);

        Account to = new Account();
        to.setName("To");
        to.setIban("BG00TO00000000000002");
        to.setStatus(AccountStatus.ACTIVE);
        to.setAvailableAmount(new BigDecimal("0.00"));
        to = accountRepository.save(to);

        final Long FROM_ID = from.getId();
        final Long TO_ID = to.getId();
        final BigDecimal AMOUNT = new BigDecimal("15.00");

        int threads = 10;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<Boolean>> futures = new ArrayList<>();

        for (int i = 0; i < threads; i++) {
            futures.add(pool.submit(() -> {
                start.await();
                try {
                    transferService.createTransfer(FROM_ID, TO_ID, AMOUNT);
                    return true;
                } catch (IllegalArgumentException ex) {
                    return false;
                }
            }));
        }

        start.countDown();

        int success = 0, fail = 0;
        for (Future<Boolean> f : futures) {
            if (f.get(5, TimeUnit.SECONDS)) success++; else fail++;
        }
        pool.shutdownNow();

        Account fromR = accountRepository.findById(FROM_ID).orElseThrow();
        Account toR   = accountRepository.findById(TO_ID).orElseThrow();


        /** We start from 100 and move 15 each time -> therefore we would have:
         * 6 successful transfers
         * 4 Insufficient funds exceptions
         * */
        assertThat(success).isEqualTo(6);
        assertThat(fail).isEqualTo(4);
        assertThat(fromR.getAvailableAmount()).isEqualByComparingTo("10.00");
        assertThat(toR.getAvailableAmount()).isEqualByComparingTo("90.00");
    }
}
