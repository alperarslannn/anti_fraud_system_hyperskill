package antifraud.domain.repository;

import antifraud.domain.UserAuthority;
import org.springframework.data.repository.CrudRepository;

public interface UserAuthorityRepository extends CrudRepository<UserAuthority, Long> {
    UserAuthority findByName(String name);
}
