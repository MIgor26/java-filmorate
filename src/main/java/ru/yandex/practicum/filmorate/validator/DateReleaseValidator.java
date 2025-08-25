package ru.yandex.practicum.filmorate.validator;

import java.time.LocalDate;

public class DateReleaseValidator {
    public static final LocalDate MIN_RELEASE_DATE = LocalDate.of(1895, 12, 28);

    public static boolean isValid(LocalDate date) {
        if (date.isBefore(MIN_RELEASE_DATE)) {
            return false;
        } else return true;
    }
}