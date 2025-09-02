package smartit_task.bank_service.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "transfers")
@NoArgsConstructor
public class Transfer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long accountId;

    @Column(nullable = false)
    private Long beneficiaryAccountId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransferType type;

    @Positive(message = "Amount must be positive")
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
//    @JsonProperty(access = JsonProperty.Access.READ_ONLY) // making it impossible for the client
//    @Schema(accessMode = Schema.AccessMode.READ_ONLY)    // to provide the field
    private LocalDateTime createdOn;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime modifiedOn;

    public Transfer(Long accountId, Long beneficiaryAccountId, TransferType type, BigDecimal amount) {
        this.accountId = accountId;
        this.beneficiaryAccountId = beneficiaryAccountId;
        this.type = type;
        this.amount = amount;
    }
}
