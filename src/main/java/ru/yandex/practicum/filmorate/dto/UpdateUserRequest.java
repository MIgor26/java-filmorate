package ru.yandex.practicum.filmorate.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateUserRequest {
    private String email;
    private String login;
    private String name;
    private LocalDate birthday;

    public boolean hasEmail() {
        return ! (email == null || email.isBlank());
    }

    public boolean hasLogin() {
        return ! (login == null || login.isBlank() || login.contains(" ")); // !! Если содержится пробел считаем, что его нет.
        // Тогда нет возврата сообщения клиенту о необходимости введения логина без пробела
    }

    public boolean hasName() {
        return ! (name == null || name.isBlank());
    }

    public boolean hasBirthday() {
        return ! (birthday == null || birthday.isAfter(LocalDate.now())); // !! Если ДР в будущем, считаем что его нет
    }
}
