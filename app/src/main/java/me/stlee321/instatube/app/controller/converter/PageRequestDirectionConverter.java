package me.stlee321.instatube.app.controller.converter;

import me.stlee321.instatube.app.util.PageRequestDirection;
import org.springframework.core.convert.converter.Converter;

public class PageRequestDirectionConverter implements Converter<String, PageRequestDirection> {
    @Override
    public PageRequestDirection convert(String direction) {
        if(direction.equals("after")) {
            return PageRequestDirection.AFTER;
        }else if(direction.equals("before")) {
            return PageRequestDirection.BEFORE;
        }
        return null;
    }
}
