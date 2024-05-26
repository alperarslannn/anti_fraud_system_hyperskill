package antifraud.api.transaction.dto;

import antifraud.domain.Transaction;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class FeedbackUiDto {
    @NotNull
    private Long transactionId;
    private Transaction.ValidityType feedback;
}
