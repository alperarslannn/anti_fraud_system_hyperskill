package antifraud.api.transaction.dto.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class IPValidator implements ConstraintValidator<ValidIP, String> {
    private static final String IP_REGEX =
            "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";
    private static final Pattern IP_PATTERN = Pattern.compile(IP_REGEX);

    @Override
    public void initialize(ValidIP constraintAnnotation) {
        // Initialization code if needed
    }

    @Override
    public boolean isValid(String ip, ConstraintValidatorContext context) {
        if (ip == null) {
            return false;
        }
        return IP_PATTERN.matcher(ip).matches();
    }

    public static boolean isIpValid(String ip) {
        if (ip == null) {
            return false;
        }
        return IP_PATTERN.matcher(ip).matches();
    }
}

