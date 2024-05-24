package antifraud.api.transaction.dto.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CheckSumValidator implements ConstraintValidator<CheckSum, String> {
    @Override
    public void initialize(CheckSum constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(String cardNumber, ConstraintValidatorContext context) {
        int nDigits = cardNumber.length();
        int sum = 0;
        boolean isSecond = false;

        for (int i = nDigits - 1; i >= 0; i--) {
            int d = cardNumber.charAt(i) - '0';

            if (isSecond) {
                d = d * 2;
            }

            sum += d / 10;
            sum += d % 10;

            isSecond = !isSecond;
        }
        return (sum % 10 == 0);
    }
}
