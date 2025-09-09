package ru.yandex.practicum.filmorate.dal.mappers;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
// Данный маппер возвращает множество id друзей из таблицы friendship
public class FriendsIdRowMapper implements RowMapper<Long> {
    @Override
    public Long mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        // Множество resultSet возвращает таблицу с id друзей для пользователя или
        return resultSet.getLong("friend_id");
    }
}
