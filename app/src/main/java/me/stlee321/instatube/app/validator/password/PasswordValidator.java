package me.stlee321.instatube.app.validator.password;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordValidator implements ConstraintValidator<Password, String> {
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if(value == null) return false;
        if(value.isBlank()) return false;
        if(value.length() < 5) return false;
        if(value.contains(" ")) return false;
        if(value.contains("\t")) return false;
        if(value.contains("\n")) return false;
        return true;
    }
}
