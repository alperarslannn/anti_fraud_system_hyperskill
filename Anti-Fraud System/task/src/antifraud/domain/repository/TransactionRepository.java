package antifraud.domain.repository;

import antifraud.domain.Transaction;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface TransactionRepository extends CrudRepository<Transaction, Long> {
    @Query("SELECT DISTINCT t.region FROM Transaction t WHERE t.date >= :date and t.date < :givenDate")
    List<Transaction.Region> listOfDistinctRegionsInLastHour(@Param("date") LocalDateTime date, @Param("givenDate") LocalDateTime givenDate);

    @Query("SELECT DISTINCT t.ip FROM Transaction t WHERE t.date >= :date and t.date < :givenDate")
    List<String> listOfDistinctIpsInLastHour(@Param("date") LocalDateTime date, @Param("givenDate") LocalDateTime givenDate);

    @Query("SELECT t FROM Transaction t WHERE t.date >= :date and t.date < :givenDate")
    List<Transaction> listOfTransactionsInLastHour(@Param("date") LocalDateTime date, @Param("givenDate") LocalDateTime givenDate);

    List<Transaction> findAllByOrderByIdAsc();

    List<Transaction> findAllByNumber(String number);
}
