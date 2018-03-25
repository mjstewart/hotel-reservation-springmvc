package com.demo;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class TimeProvider {

    public LocalDateTime localDateTime() {
        return LocalDateTime.now();
    }

    public LocalDate localDate() {
        return LocalDate.now();
    }
}
