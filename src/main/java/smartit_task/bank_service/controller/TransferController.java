package smartit_task.bank_service.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import smartit_task.bank_service.entity.Transfer;
import smartit_task.bank_service.service.TransferService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/transfers")
@CrossOrigin(origins = "http://localhost:3000")
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
    public ResponseEntity<?> createTransfer(@RequestBody Map<String, Object> transferRequest) {
        try {
            Long fromAccountId = Long.valueOf(transferRequest.get("fromAccountId").toString());
            Long toAccountId = Long.valueOf(transferRequest.get("toAccountId").toString());
            BigDecimal amount = new BigDecimal(transferRequest.get("amount").toString());

            Transfer transfer = transferService.createTransfer(fromAccountId, toAccountId, amount);
            return ResponseEntity.ok(transfer);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Invalid transfer request: " + e.getMessage());
        }
    }

    @GetMapping
    public List<Transfer> getAllTransfers() {
        return transferService.getAllTransfers();
    }
}
