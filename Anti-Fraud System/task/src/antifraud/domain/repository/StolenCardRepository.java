package antifraud.domain.repository;

import antifraud.domain.StolenCard;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface StolenCardRepository extends CrudRepository<StolenCard, Long> {
    Optional<StolenCard> findByNumber(String cardNumber);
    Iterable<StolenCard> findAllByOrderByIdAsc();

}
