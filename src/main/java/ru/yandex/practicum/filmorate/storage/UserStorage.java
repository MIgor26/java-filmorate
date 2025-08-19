package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface UserStorage {

    public User findById(int id);

    public Set<Integer> getUsersFriends(int id);

    public Set<Integer> commonFriends(int id, int otherId);

    public Collection<User> getAll();

    public User create(User user);

    public User update(User user);

    public List<User> addFriends(int id, int friendsId);

    public List<User> delFriends(int id, int friendsId);

    public void checkDuplicateEmail(User user);

}
