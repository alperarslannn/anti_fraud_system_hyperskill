package antifraud.api.transaction.dto;

import antifraud.domain.Transaction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class TransactionResponseUiDto {
    private Long transactionId;
    private Long amount;
    private String ip;
    private String number;
    private Transaction.Region region;
    private LocalDateTime date;
    private Transaction.ValidityType result;
    private String feedback;
}
