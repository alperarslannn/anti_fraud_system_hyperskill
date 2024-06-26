package antifraud.api.transaction;

import antifraud.api.transaction.dto.FeedbackUiDto;
import antifraud.api.transaction.dto.ResultUiDto;
import antifraud.api.transaction.dto.StatusUiDto;
import antifraud.api.transaction.dto.StolenCardResponseUiDto;
import antifraud.api.transaction.dto.StolenCardUiDto;
import antifraud.api.transaction.dto.SuspiciousIpResponseUiDto;
import antifraud.api.transaction.dto.SuspiciousIpUiDto;
import antifraud.api.transaction.dto.TransactionResponseUiDto;
import antifraud.api.transaction.dto.TransactionUiDto;
import antifraud.api.transaction.dto.validator.CheckSum;
import antifraud.api.transaction.dto.validator.IPValidator;
import antifraud.domain.StolenCard;
import antifraud.domain.SuspiciousIp;
import antifraud.domain.Transaction;
import antifraud.domain.TransactionAllowance;
import antifraud.domain.repository.StolenCardRepository;
import antifraud.domain.repository.SuspiciousIpRepository;
import antifraud.domain.repository.TransactionAllowanceRepository;
import antifraud.domain.repository.TransactionRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static antifraud.constant.TransactionAllowanceConstants.DEFAULT_ALLOWED_AMOUNT;
import static antifraud.constant.TransactionAllowanceConstants.DEFAULT_MANUAL_PROCESSING_AMOUNT;

@RestController
@RequestMapping(value="/api/antifraud")
@RequiredArgsConstructor
@Validated
public class TransactionUiController {
    private final SuspiciousIpRepository suspiciousIpRepository;
    private final StolenCardRepository stolenCardRepository;
    private final TransactionRepository transactionRepository;
    private final TransactionAllowanceRepository transactionAllowanceRepository;

    @PostMapping({"/transaction", "/transaction/"})
    @Transactional
    public ResponseEntity<ResultUiDto> withDraw(@RequestBody @Valid TransactionUiDto transactionUiDto){
        Long allowedLimit = DEFAULT_ALLOWED_AMOUNT;
        Long manualProcessingLimit = DEFAULT_MANUAL_PROCESSING_AMOUNT;

        Optional<TransactionAllowance> transactionAllowanceOptional = transactionAllowanceRepository.findTransactionAllowanceByNumber(transactionUiDto.getNumber());
        TransactionAllowance transactionAllowance = null;
        if(transactionAllowanceOptional.isPresent()){
            transactionAllowance = transactionAllowanceOptional.get();
            allowedLimit = transactionAllowance.getMaxAllowed();
            manualProcessingLimit = transactionAllowance.getMaxManualProcessing();
        }

        Transaction.ValidityType result;
        String info = "";
        //todo simplify business logic and move to a service
        if (transactionUiDto.getAmount() <= allowedLimit) {
            result = Transaction.ValidityType.ALLOWED;
        } else if (transactionUiDto.getAmount() <= manualProcessingLimit) {
            if(!info.isBlank()) {info = info.concat(", ");}
            info = "amount";
            result = Transaction.ValidityType.MANUAL_PROCESSING;
        } else {
            if(!info.isBlank()) {info = info.concat(", ");}
            info = "amount";
            result = Transaction.ValidityType.PROHIBITED;
        }

        if (stolenCardRepository.findByNumber(transactionUiDto.getNumber()).isPresent()){
            if(!info.isBlank()) {info = info.concat(", ");}
            if(result.equals(Transaction.ValidityType.MANUAL_PROCESSING)) info = "card-number";
            if(result.equals(Transaction.ValidityType.PROHIBITED)) info = info.concat("card-number");
            result = Transaction.ValidityType.PROHIBITED;
        }

        if (suspiciousIpRepository.findByIp(transactionUiDto.getIp()).isPresent()){
            if(!info.isBlank()) {info = info.concat(", ");}
            if(result.equals(Transaction.ValidityType.MANUAL_PROCESSING)) info = "ip";
            if(result.equals(Transaction.ValidityType.PROHIBITED)) info = info.concat("ip");
            result = Transaction.ValidityType.PROHIBITED;
        }

        int ipCorrelationByRegionCount = isIpCorrelationByRegion(transactionUiDto.getRegion(), transactionUiDto.getDate());
        int ipCorrelationByIpCount = isIpCorrelationByIp(transactionUiDto.getIp(), transactionUiDto.getDate());
        if (ipCorrelationByIpCount >= 2) {
            if(!info.isBlank()) {info = info.concat(", ");}
            if(result.equals(Transaction.ValidityType.MANUAL_PROCESSING) && ipCorrelationByIpCount == 2) info = info.concat("ip-correlation");
            else if(result.equals(Transaction.ValidityType.MANUAL_PROCESSING)) info = "ip-correlation";
            else if(result.equals(Transaction.ValidityType.PROHIBITED) && ipCorrelationByIpCount == 2) {}
            else if(result.equals(Transaction.ValidityType.ALLOWED) && ipCorrelationByIpCount == 2) {
                info = "ip-correlation";
                result = Transaction.ValidityType.MANUAL_PROCESSING;
            }
            else if(result.equals(Transaction.ValidityType.ALLOWED)) {
                info = "ip-correlation";
                result = Transaction.ValidityType.PROHIBITED;
            }
            else if(result.equals(Transaction.ValidityType.PROHIBITED)) info = info.concat("ip-correlation");

            if(result.equals(Transaction.ValidityType.MANUAL_PROCESSING) && ipCorrelationByIpCount == 2) result = Transaction.ValidityType.MANUAL_PROCESSING;
            else result = Transaction.ValidityType.PROHIBITED;
        }

        if (ipCorrelationByRegionCount >= 2) {
            if(!info.isBlank()) {info = info.concat(", ");}
            if(result.equals(Transaction.ValidityType.MANUAL_PROCESSING) && ipCorrelationByRegionCount == 2) info = info.concat("region-correlation");
            else if(result.equals(Transaction.ValidityType.MANUAL_PROCESSING)) info = "region-correlation";
            else if(result.equals(Transaction.ValidityType.PROHIBITED) && ipCorrelationByRegionCount == 2) {}
            else if(result.equals(Transaction.ValidityType.ALLOWED) && ipCorrelationByRegionCount == 2) {
                info = "region-correlation";
                result = Transaction.ValidityType.MANUAL_PROCESSING;
            }
            else if(result.equals(Transaction.ValidityType.ALLOWED)) {
                info = "region-correlation";
                result = Transaction.ValidityType.PROHIBITED;
            }
            else info = info.concat("region-correlation");

            if(result.equals(Transaction.ValidityType.MANUAL_PROCESSING) && ipCorrelationByRegionCount == 2) result = Transaction.ValidityType.MANUAL_PROCESSING;
            else result = Transaction.ValidityType.PROHIBITED;
        }

        if(info.isBlank()) {info = "none";}
        transactionRepository.save(Transaction.builder()
                .amount(transactionUiDto.getAmount())
                .ip(transactionUiDto.getIp())
                .number(transactionUiDto.getNumber())
                .region(transactionUiDto.getRegion())
                .date(transactionUiDto.getDate())
                .result(result)
                .build());
        if(transactionAllowance == null) transactionAllowanceRepository.save(TransactionAllowance.builder().number(transactionUiDto.getNumber()).build());

        return ResponseEntity.ok(ResultUiDto.builder().result(result).info(info).build());
    }

    private int isIpCorrelationByRegion(Transaction.Region region, LocalDateTime date) {
        LocalDateTime oneHourAgoFromTheRecord = date.minusHours(1);
        List<Transaction.Region> regionList = transactionRepository.listOfDistinctRegionsInLastHour(oneHourAgoFromTheRecord, date);

        int regionCount = regionList.size();
        if (regionCount == 2 && !regionList.contains(region)) return 2;
        if (regionCount == 2 && regionList.contains(region)) return 1;
        if (regionCount == 3 && regionList.contains(region)) return 2;
        return regionCount;
    }

    private int isIpCorrelationByIp(String ip, LocalDateTime date) {
        LocalDateTime oneHourAgoFromTheRecord = date.minusHours(1);
        List<String> ipList = transactionRepository.listOfDistinctIpsInLastHour(oneHourAgoFromTheRecord, date);

        int ipCount = ipList.size();
        if (ipCount == 2 && !ipList.contains(ip)) return 2;
        if (ipCount == 2 && ipList.contains(ip)) return 1;
        if (ipCount == 3 && ipList.contains(ip)) return 2;
        return ipCount;
    }

    @PutMapping({"/transaction", "/transaction/"})
    @Transactional
    public ResponseEntity<TransactionResponseUiDto> feedback(@Valid @RequestBody FeedbackUiDto feedbackUiDto){
        Optional<Transaction> transactionOptional = transactionRepository.findById(feedbackUiDto.getTransactionId());
        if(transactionOptional.isEmpty()) {return ResponseEntity.status(404).build();}
        Transaction transaction = transactionOptional.get();
        TransactionAllowance transactionAllowance = transactionAllowanceRepository.findTransactionAllowanceByNumber(transaction.getNumber())
                .orElseThrow(IllegalStateException::new);

        if (feedbackUiDto.getFeedback().equals(transaction.getResult())) {return ResponseEntity.status(422).build();}
        if (!transaction.getFeedback().isBlank()) {return ResponseEntity.status(409).build();}

        transaction.setFeedback(feedbackUiDto.getFeedback().name());
        Transaction savedTransaction = transactionRepository.save(transaction);

        transactionAllowance.setNewLimit(transaction.getAmount(), feedbackUiDto.getFeedback(), transaction.getResult());
        transactionAllowanceRepository.save(transactionAllowance);

        return ResponseEntity.ok(TransactionResponseUiDto.builder()
                .transactionId(savedTransaction.getId())
                .amount(savedTransaction.getAmount())
                .ip(savedTransaction.getIp())
                .number(savedTransaction.getNumber())
                .region(savedTransaction.getRegion())
                .date(savedTransaction.getDate())
                .result(savedTransaction.getResult())
                .feedback(savedTransaction.getFeedback())
                .build());
    }

    @GetMapping({"/history", "/history/"})
    public ResponseEntity<List<TransactionResponseUiDto>> getTransactionHistory(){
        List<Transaction> transactionList = transactionRepository.findAllByOrderByIdAsc();
        List<TransactionResponseUiDto> transactionResponseUiDtoList = transactionList.stream().map(transaction -> TransactionResponseUiDto.builder()
                .transactionId(transaction.getId())
                .amount(transaction.getAmount())
                .ip(transaction.getIp())
                .number(transaction.getNumber())
                .region(transaction.getRegion())
                .date(transaction.getDate())
                .result(transaction.getResult())
                .feedback(transaction.getFeedback())
                .build()).toList();
        return ResponseEntity.ok(transactionResponseUiDtoList);
    }

    @GetMapping("/history/{number}")
    public ResponseEntity<List<TransactionResponseUiDto>> getTransactionHistoryForCard(@PathVariable @Valid @CheckSum String number){
        List<Transaction> transactionList = transactionRepository.findAllByNumber(number);
        if (transactionList.isEmpty()) {return ResponseEntity.status(404).build();}

        List<TransactionResponseUiDto> transactionResponseUiDtoList = transactionList.stream().map(transaction -> TransactionResponseUiDto.builder()
                .transactionId(transaction.getId())
                .amount(transaction.getAmount())
                .ip(transaction.getIp())
                .number(transaction.getNumber())
                .region(transaction.getRegion())
                .date(transaction.getDate())
                .result(transaction.getResult())
                .feedback(transaction.getFeedback())
                .build()).toList();
        return ResponseEntity.ok(transactionResponseUiDtoList);
    }


    @PostMapping({"/suspicious-ip", "/suspicious-ip/"})
    public ResponseEntity<SuspiciousIpResponseUiDto> addSuspiciousIP(@Valid @RequestBody SuspiciousIpUiDto suspiciousIpUiDto){
        if (suspiciousIpRepository.findByIp(suspiciousIpUiDto.getIp()).isPresent()) return ResponseEntity.status(409).build();
        SuspiciousIp savedSuspiciousIp = suspiciousIpRepository.save(SuspiciousIp.builder().ip(suspiciousIpUiDto.getIp()).build());
        return ResponseEntity.ok(SuspiciousIpResponseUiDto.builder().id(savedSuspiciousIp.getId()).ip(suspiciousIpUiDto.getIp()).build());
    }

    @DeleteMapping("/suspicious-ip/{ip}")
    public ResponseEntity<StatusUiDto> removeSuspiciousIP(@PathVariable String ip){
        if (!IPValidator.isIpValid(ip)) return ResponseEntity.status(400).build();
        Optional<SuspiciousIp> suspiciousIp = suspiciousIpRepository.findByIp(ip);
        if (suspiciousIp.isEmpty()) return ResponseEntity.status(404).build();
        suspiciousIpRepository.delete(suspiciousIp.get());
        return ResponseEntity.ok(StatusUiDto.builder().status("IP " + ip + " successfully removed!").build());
    }

    @GetMapping({"/suspicious-ip", "/suspicious-ip/"})
    public ResponseEntity<List<SuspiciousIpResponseUiDto>> getAllSuspiciousIP(){
        Iterable<SuspiciousIp> suspiciousIpList = suspiciousIpRepository.findAllByOrderByIdAsc();

        List<SuspiciousIpResponseUiDto> suspiciousIpResponseUiDtoList = new ArrayList<>();
        suspiciousIpList.forEach(suspiciousIp -> {
            SuspiciousIpResponseUiDto suspiciousIpResponseUiDto = SuspiciousIpResponseUiDto.builder().id(suspiciousIp.getId()).ip(suspiciousIp.getIp()).build();
            suspiciousIpResponseUiDtoList.add(suspiciousIpResponseUiDto);
        });
        return ResponseEntity.ok(suspiciousIpResponseUiDtoList);
    }

    @PostMapping({"/stolencard", "/stolencard/"})
    public ResponseEntity<StolenCardResponseUiDto> addStolenCard(@Valid @RequestBody StolenCardUiDto stolenCardUiDto){
        if (stolenCardRepository.findByNumber(stolenCardUiDto.getNumber()).isPresent()) return ResponseEntity.status(409).build();
        StolenCard stolenCard = stolenCardRepository.save(StolenCard.builder().number(stolenCardUiDto.getNumber()).build());
        return ResponseEntity.ok(StolenCardResponseUiDto.builder().id(stolenCard.getId()).number(stolenCardUiDto.getNumber()).build());
    }

    @DeleteMapping("/stolencard/{number}")
    public ResponseEntity<StatusUiDto> removeStolenCard(@PathVariable @Valid @CheckSum String number){
        Optional<StolenCard> stolenCard = stolenCardRepository.findByNumber(number);
        if (stolenCard.isEmpty()) return ResponseEntity.status(404).build();
        stolenCardRepository.delete(stolenCard.get());
        return ResponseEntity.ok(StatusUiDto.builder().status("Card " + number + " successfully removed!").build());
    }

    @GetMapping({"/stolencard", "/stolencard/"})
    public ResponseEntity<List<StolenCardResponseUiDto>> getAllStolenCard(){
        Iterable<StolenCard> stolenCardList = stolenCardRepository.findAllByOrderByIdAsc();

        List<StolenCardResponseUiDto> suspiciousIpResponseUiDtoList = new ArrayList<>();
        stolenCardList.forEach(stolenCard -> {
            StolenCardResponseUiDto stolenCardResponseUiDto = StolenCardResponseUiDto.builder().id(stolenCard.getId()).number(stolenCard.getNumber()).build();
            suspiciousIpResponseUiDtoList.add(stolenCardResponseUiDto);
        });
        return ResponseEntity.ok(suspiciousIpResponseUiDtoList);
    }
}
