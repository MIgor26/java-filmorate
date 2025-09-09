package ru.yandex.practicum.filmorate.dto;

import lombok.Data;

@Data
public class FriendsDto {
    private Long user_send_id;
    private Long user_accept_id;
}
