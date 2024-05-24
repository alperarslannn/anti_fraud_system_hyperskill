package antifraud.api.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LockUiDto {
    @NotEmpty
    @NotBlank
    private String username;
    private Operation operation;

    public enum Operation {
        LOCK, UNLOCK
    }
}