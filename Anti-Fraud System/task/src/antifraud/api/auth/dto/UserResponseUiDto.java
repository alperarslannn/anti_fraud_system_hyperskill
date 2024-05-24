package antifraud.api.auth.dto;

import antifraud.api.auth.security.Role;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserResponseUiDto {
    private Long id;
    private String name;
    private String username;
    private Role role;
}