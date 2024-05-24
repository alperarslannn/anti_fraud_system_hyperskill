package antifraud.api.transaction.dto;

import antifraud.api.transaction.dto.validator.ValidIP;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import org.hibernate.validator.constraints.Length;

@Getter
public class AmountUiDto {
    private Long amount;
    @ValidIP
    private String ip;
    @NotEmpty
    @NotBlank
    @Length(min = 16, max = 16)
    private String number;
}
