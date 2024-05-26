package antifraud.domain.repository;

import antifraud.domain.TransactionAllowance;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface TransactionAllowanceRepository extends CrudRepository<TransactionAllowance, Long> {
    Optional<TransactionAllowance> findTransactionAllowanceByNumber(String number);
}
