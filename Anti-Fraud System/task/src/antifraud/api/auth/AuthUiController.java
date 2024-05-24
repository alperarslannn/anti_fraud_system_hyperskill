package antifraud.api.auth;

import antifraud.api.auth.dto.StatusUserUiDto;
import antifraud.api.auth.dto.UserResponseUiDto;
import antifraud.api.auth.dto.UserUiDto;
import antifraud.api.auth.security.CustomBCryptPasswordEncoder;
import antifraud.domain.UserAccount;
import antifraud.domain.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping(value="/api/auth")
@RequiredArgsConstructor
@Validated
public class AuthUiController {
    private final UserAccountRepository userAccountRepository;
    private final CustomBCryptPasswordEncoder encoder;


    @PostMapping("/user")
    public ResponseEntity<UserResponseUiDto> registerUser(@Valid @RequestBody UserUiDto userUiDto){
        String salt = BCrypt.gensalt();
        String hashedPassword = hashPassword(userUiDto.getPassword(), salt);

        UserAccount userAccount = userAccountRepository.findByUsernameEqualsIgnoreCase(userUiDto.getUsername()).orElse(null);
        if(Objects.nonNull(userAccount)){
            return ResponseEntity.status(409).build();
        }

        userAccount = UserAccount.builder()
                .name(userUiDto.getName())
                .username(userUiDto.getUsername())
                .password(hashedPassword)
                .salt(salt)
                .build();
        UserAccount savedUserAccount = userAccountRepository.save(userAccount);
        return ResponseEntity.status(201).body(UserResponseUiDto.builder()
                .id(savedUserAccount.getId())
                .name(savedUserAccount.getName())
                .username(savedUserAccount.getUsername())
                .build());
    }

    @GetMapping("/list")
    public ResponseEntity<List<UserResponseUiDto>> getAllUsers(){
        Iterable<UserAccount> userAccountList = userAccountRepository.findAllByOrderByIdAsc();
        List<UserResponseUiDto> userResponseUiDtoList = new ArrayList<>();
        for (UserAccount userAccount : userAccountList) {
            UserResponseUiDto userResponseUiDto = UserResponseUiDto.builder()
                    .id(userAccount.getId())
                    .name(userAccount.getName())
                    .username(userAccount.getUsername())
                    .build();
            userResponseUiDtoList.add(userResponseUiDto);
        }
        return ResponseEntity.ok(userResponseUiDtoList);
    }

    @DeleteMapping("/user/{username}")
    public ResponseEntity<StatusUserUiDto> registerUser(@PathVariable String username){
        UserAccount userAccount = userAccountRepository.findByUsernameEqualsIgnoreCase(username).orElse(null);
        if(Objects.isNull(userAccount)){
            return ResponseEntity.notFound().build();
        }

        userAccountRepository.delete(userAccount);
        return ResponseEntity.ok(StatusUserUiDto.builder().username(username).status("Deleted successfully!").build());
    }

    private String hashPassword(String password, String salt) {
        return encoder.encode(password + salt);
    }
}
