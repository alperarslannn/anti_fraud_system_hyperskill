package antifraud.api.auth.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StatusUserUiDto {
    private String username;
    private String status;
}
