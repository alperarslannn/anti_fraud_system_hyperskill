package antifraud.api.transaction.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StatusUiDto {
    private String status;
}
