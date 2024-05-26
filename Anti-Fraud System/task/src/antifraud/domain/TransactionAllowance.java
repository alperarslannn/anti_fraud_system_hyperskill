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

import static antifraud.constant.TransactionAllowanceConstants.DEFAULT_ALLOWED_AMOUNT;
import static antifraud.constant.TransactionAllowanceConstants.DEFAULT_MANUAL_PROCESSING_AMOUNT;

@Entity
@Table(name = "transaction_allowance")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionAllowance {
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "transaction_seq")
    @SequenceGenerator(name = "transaction_seq", sequenceName = "TRANSACTION_SEQ", allocationSize = 1)
    private Long id;
    @Column(unique = true, nullable = false)
    private String number;
    @Column
    private Long maxAllowed;
    @Column
    private Long maxManualProcessing;

    @PrePersist
    public void prePersist() {
        if (maxAllowed == null) {
            maxAllowed = DEFAULT_ALLOWED_AMOUNT;
        }
        if (maxManualProcessing == null) {
            maxManualProcessing = DEFAULT_MANUAL_PROCESSING_AMOUNT;
        }
    }

    public void setNewLimit(Long valueFromTransaction, Transaction.ValidityType feedback, Transaction.ValidityType transactionValidity) {
        if(feedback.equals(Transaction.ValidityType.ALLOWED)){
            maxAllowed = (long) Math.ceil(0.8 * maxAllowed + 0.2 * valueFromTransaction);
            if(transactionValidity.equals(Transaction.ValidityType.PROHIBITED)){
                maxManualProcessing = (long) Math.ceil(0.8 * maxManualProcessing + 0.2 * valueFromTransaction);
            }
        }

        if(feedback.equals(Transaction.ValidityType.MANUAL_PROCESSING)){
            if(transactionValidity.equals(Transaction.ValidityType.ALLOWED)){
                maxAllowed = (long) Math.ceil(0.8 * maxAllowed - 0.2 * valueFromTransaction);
            }
            if(transactionValidity.equals(Transaction.ValidityType.PROHIBITED)){
                maxManualProcessing = (long) Math.ceil(0.8 * maxManualProcessing + 0.2 * valueFromTransaction);
            }
        }

        if(feedback.equals(Transaction.ValidityType.PROHIBITED)){
            maxManualProcessing = (long) Math.ceil(0.8 * maxManualProcessing - 0.2 * valueFromTransaction);
            if(transactionValidity.equals(Transaction.ValidityType.ALLOWED)){
                maxAllowed = (long) Math.ceil(0.8 * maxAllowed - 0.2 * valueFromTransaction);
            }
        }
    }

}