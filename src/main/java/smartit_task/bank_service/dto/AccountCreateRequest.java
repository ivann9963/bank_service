package smartit_task.bank_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

public record AccountCreateRequest(
        @NotBlank(message = "Account name is required")
        String name,

        @Pattern(regexp = "^[A-Z]{2}[0-9]{2}[A-Z0-9]{1,30}$", message = "Invalid IBAN format")
        String iban,

        @PositiveOrZero(message = "Initial amount must be >= 0")
        BigDecimal initialAmount
) {}
