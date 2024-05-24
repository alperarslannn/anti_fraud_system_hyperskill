package antifraud.api.transaction;

import antifraud.api.transaction.dto.AmountUiDto;
import antifraud.api.transaction.dto.ResultUiDto;
import antifraud.api.transaction.dto.StatusUiDto;
import antifraud.api.transaction.dto.StolenCardResponseUiDto;
import antifraud.api.transaction.dto.StolenCardUiDto;
import antifraud.api.transaction.dto.SuspiciousIpResponseUiDto;
import antifraud.api.transaction.dto.SuspiciousIpUiDto;
import antifraud.api.transaction.dto.validator.CheckSum;
import antifraud.api.transaction.dto.validator.IPValidator;
import antifraud.domain.StolenCard;
import antifraud.domain.SuspiciousIp;
import antifraud.domain.repository.StolenCardRepository;
import antifraud.domain.repository.SuspiciousIpRepository;
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

    @PostMapping({"/transaction", "/transaction/"})
    public ResponseEntity<ResultUiDto> withDraw(@RequestBody AmountUiDto amountUiDto){
        if (amountUiDto.getAmount() == null || amountUiDto.getAmount() <= 0){return ResponseEntity.badRequest().build();}
        String result;
        String info = "";
        if (amountUiDto.getAmount() <= 200) {
            result = "ALLOWED";
        } else if (amountUiDto.getAmount() <= 1500) {
            if(!info.isBlank()) {info = info.concat(", ");}
            info = "amount";
            result = "MANUAL_PROCESSING";
        } else {
            if(!info.isBlank()) {info = info.concat(", ");}
            info = "amount";
            result = "PROHIBITED";
        }

        if (stolenCardRepository.findByNumber(amountUiDto.getNumber()).isPresent()){
            if(!info.isBlank()) {info = info.concat(", ");}
            if(result.equals("MANUAL_PROCESSING")) info = "card-number";
            if(result.equals("PROHIBITED")) info = info.concat("card-number");
            result = "PROHIBITED";
        }

        if (suspiciousIpRepository.findByIp(amountUiDto.getIp()).isPresent()){
            if(!info.isBlank()) {info = info.concat(", ");}
            if(result.equals("MANUAL_PROCESSING")) info = "ip";
            if(result.equals("PROHIBITED")) info = info.concat("ip");
            result = "PROHIBITED";
        }
        if(info.isBlank()) {info = "none";}

        return ResponseEntity.ok(ResultUiDto.builder().result(result).info(info).build());
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
