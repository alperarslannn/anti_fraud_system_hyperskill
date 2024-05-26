package antifraud.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "transaction")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "transaction_seq")
    @SequenceGenerator(name = "transaction_seq", sequenceName = "TRANSACTION_SEQ", allocationSize = 1)
    private Long id;
    @Column
    private Long amount;
    @Column
    private String ip;
    @Column
    private String number;
    @Column
    private Region region;
    @Column
    private LocalDateTime date;
    @Column
    private ValidityType result;
    @Column(nullable = false)
    private String feedback;

    public enum Region {
        EAP("East Asia and Pacific"),
        ECA("Europe and Central Asia"),
        HIC("High-Income countries"),
        LAC("Latin America and the Caribbean"),
        MENA("The Middle East and North Africa"),
        SA("South Asia"),
        SSA("Sub-Saharan Africa");

        private final String description;

        Region(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    public enum ValidityType {
        ALLOWED, MANUAL_PROCESSING, PROHIBITED
    }

    @PrePersist
    public void prePersist() {
        if (feedback == null) {
            feedback = "";
        }
    }
}
