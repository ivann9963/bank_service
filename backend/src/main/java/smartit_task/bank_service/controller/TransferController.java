package smartit_task.bank_service.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import smartit_task.bank_service.dto.TransferCreateRequest;
import smartit_task.bank_service.entity.Transfer;
import smartit_task.bank_service.mapper.TransferMapper;
import smartit_task.bank_service.service.TransferService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/transfers")
public class TransferController {

    @Autowired
    private TransferService transferService;

    @GetMapping("/account/{accountId}")
    public List<Transfer> getTransfersByAccountId(@PathVariable Long accountId) {
        return transferService.getAllTransfersByAccountId(accountId);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Transfer> getTransferById(@PathVariable Long id) {
        return transferService.getTransferById(id)
                .map(transfer -> ResponseEntity.ok().body(transfer))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createTransfer(@Valid @RequestBody TransferCreateRequest req,
                                    @RequestHeader(value = "Idempotency-Key", required = false)
                                            String idempotencyKey) {
        try {
            var t = transferService.createTransfer(req.fromAccountId(),
                    req.toAccountId(),
                    req.amount(),
                    idempotencyKey);
            return ResponseEntity.status(201).body(TransferMapper.toResponse(t));
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        }
    }

    @GetMapping
    public List<Transfer> getAllTransfers() {
        return transferService.getAllTransfers();
    }
}
