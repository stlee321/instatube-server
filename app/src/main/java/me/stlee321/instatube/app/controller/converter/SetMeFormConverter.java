package me.stlee321.instatube.app.controller.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.stlee321.instatube.app.controller.dto.req.SetMeForm;
import org.springframework.core.convert.converter.Converter;

public class SetMeFormConverter implements Converter<String, SetMeForm> {

    private final ObjectMapper objectMapper;
    public SetMeFormConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
    @Override
    public SetMeForm convert(String source) {
        SetMeForm form;
        try {
            form = objectMapper.readValue(source, SetMeForm.class);
        }catch(Exception e) {
            form = null;
        }
        return form;
    }
}
