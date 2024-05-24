package antifraud.api.auth.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserResponseUiDto {
    private Long id;
    private String name;
    private String username;
}