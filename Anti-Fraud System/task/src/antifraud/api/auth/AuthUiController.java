package antifraud.api.auth;

import antifraud.api.auth.dto.LockUiDto;
import antifraud.api.auth.dto.NewRoleUiDto;
import antifraud.api.auth.dto.StatusUserUiDto;
import antifraud.api.auth.dto.UserResponseUiDto;
import antifraud.api.auth.dto.UserUiDto;
import antifraud.api.auth.security.CustomBCryptPasswordEncoder;
import antifraud.api.auth.security.Role;
import antifraud.domain.UserAccount;
import antifraud.domain.UserAuthority;
import antifraud.domain.repository.UserAccountRepository;
import antifraud.domain.repository.UserAuthorityRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping(value="/api/auth")
@RequiredArgsConstructor
@Validated
public class AuthUiController {
    private final UserAccountRepository userAccountRepository;
    private final UserAuthorityRepository userAuthorityRepository;
    private final CustomBCryptPasswordEncoder encoder;


    @PostMapping({"/user","/user/"})
    public ResponseEntity<UserResponseUiDto> registerUser(@Valid @RequestBody UserUiDto userUiDto){
        String salt = BCrypt.gensalt();
        String hashedPassword = hashPassword(userUiDto.getPassword(), salt);

        UserAccount userAccount = userAccountRepository.findByUsernameEqualsIgnoreCase(userUiDto.getUsername()).orElse(null);
        if(Objects.nonNull(userAccount)){
            return ResponseEntity.status(409).build();
        }

        if(userAccountRepository.findFirstByLocked(false).isEmpty()){
            userAccount = UserAccount.builder()
                    .name(userUiDto.getName())
                    .username(userUiDto.getUsername())
                    .password(hashedPassword)
                    .salt(salt)
                    .userAuthority(userAuthorityRepository.findByName(Role.getAuthorityNameByRole(Role.ADMINISTRATOR)))
                    .locked(false)
                    .build();
            UserAccount savedUserAccount = userAccountRepository.save(userAccount);

            return ResponseEntity.status(201).body(UserResponseUiDto.builder()
                    .id(savedUserAccount.getId())
                    .name(savedUserAccount.getName())
                    .username(savedUserAccount.getUsername())
                    .role(Role.findRoleByAuthorityName(savedUserAccount.getUserAuthority().getName()))
                    .build());
        }

        userAccount = UserAccount.builder()
                .name(userUiDto.getName())
                .username(userUiDto.getUsername())
                .password(hashedPassword)
                .salt(salt)
                .userAuthority(userAuthorityRepository.findByName(Role.getAuthorityNameByRole(Role.MERCHANT)))
                .locked(true)
                .build();
        UserAccount savedUserAccount = userAccountRepository.save(userAccount);

        return ResponseEntity.status(201).body(UserResponseUiDto.builder()
                .id(savedUserAccount.getId())
                .name(savedUserAccount.getName())
                .username(savedUserAccount.getUsername())
                .role(Role.findRoleByAuthorityName(savedUserAccount.getUserAuthority().getName()))
                .build());
    }

    @PutMapping({"/role", "/role/"})
    public ResponseEntity<UserResponseUiDto> changeUserRole(@Valid @RequestBody NewRoleUiDto newRoleUiDto){

        if(Role.ADMINISTRATOR.equals(newRoleUiDto.getRole())){
            return ResponseEntity.status(400).build();
        }
        UserAccount userAccount = userAccountRepository.findByUsernameEqualsIgnoreCase(newRoleUiDto.getUsername()).orElse(null);
        if(Objects.isNull(userAccount) || (Role.ADMINISTRATOR.equals(newRoleUiDto.getRole()))){
            return ResponseEntity.status(404).build();
        }
        if(userAccount.getUserAuthority().getName().equals(Role.getAuthorityNameByRole(newRoleUiDto.getRole()))){
            return ResponseEntity.status(409).build();
        }
        UserAuthority userAuthority = userAuthorityRepository.findByName(Role.getAuthorityNameByRole(newRoleUiDto.getRole()));
        userAccount.setUserAuthority(userAuthority);
        UserAccount savedUserAccount = userAccountRepository.save(userAccount);

        return ResponseEntity.status(200).body(UserResponseUiDto.builder()
                .id(savedUserAccount.getId())
                .name(savedUserAccount.getName())
                .username(savedUserAccount.getUsername())
                .role(Role.findRoleByAuthorityName(savedUserAccount.getUserAuthority().getName()))
                .build());
    }

    @PutMapping({"/access", "/access/"})
    public ResponseEntity<StatusUserUiDto> switchUserLock(@Valid @RequestBody LockUiDto lockUiDto){

        UserAccount userAccount = userAccountRepository.findByUsernameEqualsIgnoreCase(lockUiDto.getUsername()).orElse(null);
        if(Objects.isNull(userAccount)){
            return ResponseEntity.status(404).build();
        }
        if(userAccount.getUserAuthority().getName().equals(Role.getAuthorityNameByRole(Role.ADMINISTRATOR))){
            return ResponseEntity.status(400).build();
        }
        userAccount.setLocked(lockUiDto.getOperation().equals(LockUiDto.Operation.LOCK));
        userAccountRepository.save(userAccount);
        return ResponseEntity.ok(StatusUserUiDto.builder().status("User " + userAccount.getUsername() + " " + lockUiDto.getOperation().name().toLowerCase() + "ed!").build());
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
                    .role(Role.findRoleByAuthorityName(userAccount.getUserAuthority().getName()))
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
