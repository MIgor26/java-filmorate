package ru.yandex.practicum.filmorate.controller;

import java.time.LocalDate;

public class DateValidator {

    public boolean isValid(LocalDate date) {
        private final LocalDate MIN_DATE
        if (date == null || date.isAfter(1985, 12, 28)) {
            return true; // Тру поставил потому что у меня есть другая аннотация для проверки null
        }

        LocalDate minDate = LocalDate.of(1895, 12, 28);
        return date.isAfter(minDate) || date.isEqual(minDate);
    }
}