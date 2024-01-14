package me.stlee321.instatube.app.util;

public enum PageRequestDirection {
    BEFORE("before"), AFTER("after");

    private final String direction;
    PageRequestDirection(String direction) {
        this.direction = direction;
    }

    public String getDirection() {
        return this.direction;
    }
}
