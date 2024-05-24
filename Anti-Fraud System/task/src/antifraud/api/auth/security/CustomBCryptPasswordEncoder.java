package antifraud.api.auth.security;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class CustomBCryptPasswordEncoder extends BCryptPasswordEncoder {

    public CustomBCryptPasswordEncoder() {
        super(14);
    }

    @Override
    public String encode(CharSequence rawPassword) {
        return super.encode(rawPassword);
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        if(Objects.nonNull(SecurityContextHolder.getContext()) && Objects.nonNull(SecurityContextHolder.getContext().getAuthentication())){
            return super.matches(rawPassword + ((CustomUserDetails)SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getSalt(), encodedPassword);
        }
        return false;
    }
}
