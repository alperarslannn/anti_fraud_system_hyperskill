package antifraud.bootstrap;

import antifraud.api.auth.security.Role;
import antifraud.domain.UserAuthority;
import antifraud.domain.repository.UserAuthorityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class DataLoader implements CommandLineRunner {

    private final UserAuthorityRepository authorityRepository;

    @Autowired
    public DataLoader(UserAuthorityRepository authorityRepository) {
        this.authorityRepository = authorityRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        createRoles();
    }

    private void createRoles() {
        if(Objects.isNull(authorityRepository.findByName(Role.getAuthorityNameByRole(Role.ADMINISTRATOR)))){
            authorityRepository.save(new UserAuthority(Role.ADMINISTRATOR));
            authorityRepository.save(new UserAuthority(Role.MERCHANT));
            authorityRepository.save(new UserAuthority(Role.SUPPORT));
        }
    }
}

