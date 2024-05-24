package antifraud.api.transaction.dto;

import antifraud.api.transaction.dto.validator.ValidIP;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;

@Getter
public class SuspiciousIpUiDto {
    @NotEmpty
    @NotBlank
    @ValidIP
    private String ip;
}