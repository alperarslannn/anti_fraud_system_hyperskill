package antifraud.api;

import antifraud.api.dto.AmountUiDto;
import antifraud.api.dto.ResultUiDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value="/api/antifraud/transaction")
public class TransactionUiController {

    @PostMapping
    public ResponseEntity<ResultUiDto> withDraw(@RequestBody AmountUiDto amountUiDto){
        if (amountUiDto.getAmount() == null || amountUiDto.getAmount() <= 0){return ResponseEntity.badRequest().build();}

        if (amountUiDto.getAmount() <= 200) {
            return ResponseEntity.ok(ResultUiDto.builder().result("ALLOWED").build());
        } else if (amountUiDto.getAmount() <= 1500) {
            return ResponseEntity.ok(ResultUiDto.builder().result("MANUAL_PROCESSING").build());
        } else {
            return ResponseEntity.ok(ResultUiDto.builder().result("PROHIBITED").build());
        }
    }
}
