package me.stlee321.instatube.app.validator.handle;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class HandleValidator implements ConstraintValidator<Handle, String> {
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if(value == null) return false;
        if(value.isEmpty()) return false;
        if(value.length() > 15) return false;
        if(value.equals("me")) return false;
        if(value.equals("api")) return false;
        return value.matches("^[a-zA-Z0-9_]+$");
    }
}
