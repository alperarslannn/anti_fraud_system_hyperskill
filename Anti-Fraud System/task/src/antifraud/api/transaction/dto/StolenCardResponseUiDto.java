package antifraud.api.transaction.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StolenCardResponseUiDto {
    private Long id;
    private String number;
}
