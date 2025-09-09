package ru.yandex.practicum.filmorate.dal;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.List;

public interface UserStorage {

    public User findById(long id);

    public List<User> getUsersFriends(long id);

    public List<User> commonFriends(long id, long otherId);

    public Collection<User> getAll();

    public User create(User user);

    public User update(User user);

    public List<User> addFriends(long id, long friendsId);

    public List<User> delFriends(long id, long friendsId);

    public void checkDuplicateEmail(User user);

    public User getUserById(long id);

}
