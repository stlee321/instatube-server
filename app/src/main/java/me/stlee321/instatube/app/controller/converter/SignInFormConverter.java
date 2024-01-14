package me.stlee321.instatube.app.controller.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.stlee321.instatube.app.controller.dto.req.SignInForm;
import org.springframework.core.convert.converter.Converter;

public class SignInFormConverter implements Converter<String, SignInForm> {

    private final ObjectMapper objectMapper;
    public SignInFormConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
    @Override
    public SignInForm convert(String source) {
        SignInForm form;
        try {
            form = objectMapper.readValue(source, SignInForm.class);
        }catch(Exception e) {
            form = null;
        }
        return form;
    }
}
