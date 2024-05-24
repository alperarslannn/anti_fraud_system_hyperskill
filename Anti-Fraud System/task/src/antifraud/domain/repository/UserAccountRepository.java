package antifraud.domain.repository;

import antifraud.domain.UserAccount;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface UserAccountRepository extends CrudRepository<UserAccount, Long> {

    @Override
    Optional<UserAccount> findById(Long id);
    Iterable<UserAccount> findAllByOrderByIdAsc();
    Optional<UserAccount> findByUsernameEqualsIgnoreCase(String email);
    Optional<UserAccount> findFirstByLocked(boolean locked);

}
