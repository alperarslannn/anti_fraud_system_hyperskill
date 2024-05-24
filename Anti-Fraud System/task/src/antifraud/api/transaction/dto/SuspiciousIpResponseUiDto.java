package antifraud.api.transaction.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class SuspiciousIpResponseUiDto {
    private Long id;
    private String ip;
}