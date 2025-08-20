package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.List;

public interface UserStorage {

    public User findById(int id);

    public List<User> getUsersFriends(int id);

    public List<User> commonFriends(int id, int otherId);

    public Collection<User> getAll();

    public User create(User user);

    public User update(User user);

    public List<User> addFriends(int id, int friendsId);

    public List<User> delFriends(int id, int friendsId);

    public void checkDuplicateEmail(User user);

    public User getUserById(int id);

}
