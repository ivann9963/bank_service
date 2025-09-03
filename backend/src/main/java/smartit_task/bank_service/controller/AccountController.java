package smartit_task.bank_service.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import smartit_task.bank_service.dto.AccountCreateRequest;
import smartit_task.bank_service.dto.AccountResponse;
import smartit_task.bank_service.entity.Account;
import smartit_task.bank_service.mapper.AccountMapper;
import smartit_task.bank_service.service.AccountService;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    @Value("${app.allow-seed:false}")
    private boolean allowSeed;

    @Autowired
    private AccountService accountService;

    @GetMapping
    public List<AccountResponse> getAllAccounts() {
        return AccountMapper.toResponseList(accountService.getAllAccounts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccountResponse> getAccountById(@PathVariable Long id) {
        return accountService.getAccountById(id)
                .map(acc -> ResponseEntity.ok(AccountMapper.toResponse(acc)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createAccount(@Valid @RequestBody AccountCreateRequest req) {
        try {
            Account saved = accountService.createAccount(req);
            return ResponseEntity.ok(AccountMapper.toResponse(saved));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/batch")
    public ResponseEntity<?> createAccounts(@RequestBody List<@Valid AccountCreateRequest> requests) {
        try {
            var saved = requests.stream()
                    .map(accountService::createAccount)
                    .map(AccountMapper::toResponse)
                    .toList();
            return ResponseEntity.status(201).body(saved);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }


    @PutMapping("/{id}")
    public ResponseEntity<?> updateAccount(@PathVariable Long id, @Valid @RequestBody Account accountDetails) {
        try {
            Account updated = accountService.updateAccount(id, accountDetails);
            return ResponseEntity.ok(AccountMapper.toResponse(updated));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}/freeze")
    public ResponseEntity<?> freezeAccount(@PathVariable Long id) {
        try {
            Account account = accountService.freezeAccount(id);
            return ResponseEntity.ok(AccountMapper.toResponse(account));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}/unfreeze")
    public ResponseEntity<?> unfreezeAccount(@PathVariable Long id) {
        try {
            Account account = accountService.unfreezeAccount(id);
            return ResponseEntity.ok(AccountMapper.toResponse(account));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAccount(@PathVariable Long id) {
        try {
            accountService.deleteAccount(id);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/seed")
//    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> seedAccounts() {
        if (!allowSeed) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        var batch = List.of(
                new AccountCreateRequest("Alice", "BG80BNBG96611020345678", new BigDecimal("1000.00")),
                new AccountCreateRequest("Bob",   "BG10BNBG96611020345679", new BigDecimal("250.00")),
                new AccountCreateRequest("Carol", "BG29BNBG96611020345680", new BigDecimal("500.00"))
        );

        var saved = accountService.createAccounts(batch);
        return ResponseEntity.ok(AccountMapper.toResponseList(saved));
    }
}