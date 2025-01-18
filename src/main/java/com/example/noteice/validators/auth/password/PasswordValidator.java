package com.example.noteice.validators.auth.password;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

public class PasswordValidator implements ConstraintValidator<PasswordValidatorInterface, String> {
    @Autowired
    private MessageSource messageSource;

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        context.disableDefaultConstraintViolation();
        if (value == null || value.isBlank()) {
            addConstraintViolation(context,
                    messageSource.getMessage("password.notBlank", null, LocaleContextHolder.getLocale()));
            return false;
        }
        if (value.length() < 8 || value.length() > 64) {
            String message = messageSource.getMessage("password.size", new Object[]{8, 64}, LocaleContextHolder.getLocale());
            addConstraintViolation(context, message);
            return false;
        }
        String pattern = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&#])[A-Za-z\\d@$!%*?&#]+$";
        if (!value.matches(pattern)) {
            addConstraintViolation(context,
                    messageSource.getMessage("password.pattern", null, LocaleContextHolder.getLocale()));
            return false;
        }
        return true;
    }

    private void addConstraintViolation(ConstraintValidatorContext context, String messageTemplate) {
        context.buildConstraintViolationWithTemplate(messageTemplate)
                .addConstraintViolation();
    }
}

