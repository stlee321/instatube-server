package me.stlee321.instatube.app.util;

import java.sql.Timestamp;
import java.time.LocalDateTime;

public class MicroTimestamp {
    public static Long fromLocalDateTime(LocalDateTime time) {
        Long millis = Timestamp.valueOf(time).getTime();
        Long micros = (time.getNano() % 1000000L) / 1000L;
        return millis * 1000L + micros;
    }
}
