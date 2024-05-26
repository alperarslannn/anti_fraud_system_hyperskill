package antifraud.api.transaction.dto;

import antifraud.domain.Transaction;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResultUiDto {
    private Transaction.ValidityType result;
    private String info;
}
