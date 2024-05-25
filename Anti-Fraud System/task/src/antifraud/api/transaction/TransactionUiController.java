package antifraud.api.transaction;

import antifraud.api.transaction.dto.ResultUiDto;
import antifraud.api.transaction.dto.StatusUiDto;
import antifraud.api.transaction.dto.StolenCardResponseUiDto;
import antifraud.api.transaction.dto.StolenCardUiDto;
import antifraud.api.transaction.dto.SuspiciousIpResponseUiDto;
import antifraud.api.transaction.dto.SuspiciousIpUiDto;
import antifraud.api.transaction.dto.TransactionUiDto;
import antifraud.api.transaction.dto.validator.CheckSum;
import antifraud.api.transaction.dto.validator.IPValidator;
import antifraud.domain.StolenCard;
import antifraud.domain.SuspiciousIp;
import antifraud.domain.Transaction;
import antifraud.domain.repository.StolenCardRepository;
import antifraud.domain.repository.SuspiciousIpRepository;
import antifraud.domain.repository.TransactionRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(value="/api/antifraud")
@RequiredArgsConstructor
@Validated
public class TransactionUiController {
    private final SuspiciousIpRepository suspiciousIpRepository;
    private final StolenCardRepository stolenCardRepository;
    private final TransactionRepository transactionRepository;

    @PostMapping({"/transaction", "/transaction/"})
    public ResponseEntity<ResultUiDto> withDraw(@RequestBody TransactionUiDto transactionUiDto){
        if (transactionUiDto.getAmount() == null || transactionUiDto.getAmount() <= 0){return ResponseEntity.badRequest().build();}
        String result;
        String info = "";
        //todo simplify business logic and move to a service
        if (transactionUiDto.getAmount() <= 200) {
            result = "ALLOWED";
        } else if (transactionUiDto.getAmount() <= 1500) {
            if(!info.isBlank()) {info = info.concat(", ");}
            info = "amount";
            result = "MANUAL_PROCESSING";
        } else {
            if(!info.isBlank()) {info = info.concat(", ");}
            info = "amount";
            result = "PROHIBITED";
        }

        if (stolenCardRepository.findByNumber(transactionUiDto.getNumber()).isPresent()){
            if(!info.isBlank()) {info = info.concat(", ");}
            if(result.equals("MANUAL_PROCESSING")) info = "card-number";
            if(result.equals("PROHIBITED")) info = info.concat("card-number");
            result = "PROHIBITED";
        }

        if (suspiciousIpRepository.findByIp(transactionUiDto.getIp()).isPresent()){
            if(!info.isBlank()) {info = info.concat(", ");}
            if(result.equals("MANUAL_PROCESSING")) info = "ip";
            if(result.equals("PROHIBITED")) info = info.concat("ip");
            result = "PROHIBITED";
        }

        int ipCorrelationByRegionCount = isIpCorrelationByRegion(transactionUiDto.getRegion(), transactionUiDto.getDate());
        int ipCorrelationByIpCount = isIpCorrelationByIp(transactionUiDto.getIp(), transactionUiDto.getDate());
        if (ipCorrelationByIpCount >= 2) {
            if(!info.isBlank()) {info = info.concat(", ");}
            if(result.equals("MANUAL_PROCESSING") && ipCorrelationByIpCount == 2) info = info.concat("ip-correlation");
            else if(result.equals("MANUAL_PROCESSING")) info = "ip-correlation";
            else if(result.equals("PROHIBITED") && ipCorrelationByIpCount == 2) {}
            else if(result.equals("ALLOWED") && ipCorrelationByIpCount == 2) {
                info = "ip-correlation";
                result = "MANUAL_PROCESSING";
            }
            else if(result.equals("ALLOWED")) {
                info = "ip-correlation";
                result = "PROHIBITED";
            }
            else if(result.equals("PROHIBITED")) info = info.concat("ip-correlation");

            if(result.equals("MANUAL_PROCESSING") && ipCorrelationByIpCount == 2) result = "MANUAL_PROCESSING";
            else result = "PROHIBITED";
        }

        if (ipCorrelationByRegionCount >= 2) {
            if(!info.isBlank()) {info = info.concat(", ");}
            if(result.equals("MANUAL_PROCESSING") && ipCorrelationByRegionCount == 2) info = info.concat("region-correlation");
            else if(result.equals("MANUAL_PROCESSING")) info = "region-correlation";
            else if(result.equals("PROHIBITED") && ipCorrelationByRegionCount == 2) {}
            else if(result.equals("ALLOWED") && ipCorrelationByRegionCount == 2) {
                info = "region-correlation";
                result = "MANUAL_PROCESSING";
            }
            else if(result.equals("ALLOWED")) {
                info = "region-correlation";
                result = "PROHIBITED";
            }
            else if(result.equals("PROHIBITED")) info = info.concat("region-correlation");

            if(result.equals("MANUAL_PROCESSING") && ipCorrelationByRegionCount == 2) result = "MANUAL_PROCESSING";
            else result = "PROHIBITED";
        }

        if(info.isBlank()) {info = "none";}
        transactionRepository.save(Transaction.builder()
                .amount(transactionUiDto.getAmount())
                .ip(transactionUiDto.getIp())
                .number(transactionUiDto.getNumber())
                .region(transactionUiDto.getRegion())
                .date(transactionUiDto.getDate())
                .build());

        return ResponseEntity.ok(ResultUiDto.builder().result(result).info(info).build());
    }

    private int isIpCorrelationByRegion(Transaction.Region region, LocalDateTime date) {
        LocalDateTime oneHourAgoFromTheRecord = date.minusHours(1);
        List<Transaction.Region> regionList = transactionRepository.listOfDistinctRegionsInLastHour(oneHourAgoFromTheRecord, date);

        int regionCount = regionList.size();
        if (regionCount == 2 && !regionList.contains(region)) return 2;
        if (regionCount == 2 && regionList.contains(region)) return 1;
        if (regionCount == 3 && regionList.contains(region)) return 2;
        //if (regionCount >= 2 && !regionList.contains(region)) return regionCount;
        return regionCount;
    }

    private int isIpCorrelationByIp(String ip, LocalDateTime date) {
        LocalDateTime oneHourAgoFromTheRecord = date.minusHours(1);
        List<String> ipList = transactionRepository.listOfDistinctIpsInLastHour(oneHourAgoFromTheRecord, date);

        int ipCount = ipList.size();
        if (ipCount == 2 && !ipList.contains(ip)) return 2;
        if (ipCount == 2 && ipList.contains(ip)) return 1;
        if (ipCount == 3 && ipList.contains(ip)) return 2;
        //if (regionCount >= 2 && !regionList.contains(region)) return regionCount;
        return ipCount;
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
