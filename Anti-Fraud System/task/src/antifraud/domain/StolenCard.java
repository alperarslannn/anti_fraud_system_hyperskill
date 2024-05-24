package antifraud.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "stolen-card")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StolenCard {
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "suspicious_ip_seq")
    @SequenceGenerator(name = "suspicious_ip_seq", sequenceName = "SUSPICIOUS_IP_SEQ", allocationSize = 1)
    private Long id;
    @Column
    private String number;

}