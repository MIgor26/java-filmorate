package ru.yandex.practicum.filmorate.dal;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.List;

public interface UserStorage {

    public Collection<User> getAll();

    public User findById(Long id);

    public List<User> getUsersFriends(long id);

    public List<User> commonFriends(long id, long otherId);

    public User create(User user);

    public User update(User user);

    public void addFriends(long id, long friendsId);

    public void delFriends(long id, long friendsId);

    public void deleteAllFriends(Long userId);

    public boolean emilExists(User user);

    public boolean loginExists(User user);
}
