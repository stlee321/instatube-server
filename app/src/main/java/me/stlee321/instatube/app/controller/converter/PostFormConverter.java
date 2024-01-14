package me.stlee321.instatube.app.controller.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.stlee321.instatube.app.controller.dto.req.PostForm;
import org.springframework.core.convert.converter.Converter;

public class PostFormConverter implements Converter<String, PostForm> {

    private final ObjectMapper objectMapper;
    public PostFormConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
    @Override
    public PostForm convert(String source) {
        PostForm form;
        try {
            form = objectMapper.readValue(source, PostForm.class);
        }catch(Exception e) {
            form = null;
        }
        return form;
    }
}
