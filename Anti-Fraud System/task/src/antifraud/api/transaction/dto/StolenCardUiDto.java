package antifraud.api.transaction.dto;

import antifraud.api.transaction.dto.validator.CheckSum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import org.hibernate.validator.constraints.Length;

@Getter
public class StolenCardUiDto {
    @NotEmpty
    @NotBlank
    @Length(min = 16, max = 16)
    @CheckSum
    private String number;
}
