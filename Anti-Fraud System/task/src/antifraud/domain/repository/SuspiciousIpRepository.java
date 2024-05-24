package antifraud.domain.repository;

import antifraud.domain.SuspiciousIp;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface SuspiciousIpRepository extends CrudRepository<SuspiciousIp, Integer> {
    Optional<SuspiciousIp> findByIp(String ip);
    Iterable<SuspiciousIp> findAllByOrderByIdAsc();
}
