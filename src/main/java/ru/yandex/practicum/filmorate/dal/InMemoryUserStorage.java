//package ru.yandex.practicum.filmorate.dal;
//
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.stereotype.Component;
//import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
//import ru.yandex.practicum.filmorate.exception.NotFoundException;
//import ru.yandex.practicum.filmorate.model.User;
//
//import java.util.*;
//
//@Slf4j
//@Component
//@Qualifier("memoryUserService")
//public class InMemoryUserStorage implements UserStorage {
//    private final Map<Long, User> users = new HashMap<>();
//
//    @Override
//    public User findById(long id) {
//        return getUserById(id);
//    }
//
//    @Override
//    public List<User> getUsersFriends(long id) {
//        User user = getUserById(id);
//        return getFriends(user);
//    }
//
//    @Override
//    public List<User> commonFriends(long id, long otherId) {
//        // Делаем новый список, чтобы при поиске общих друзей ссылка на друзей не изменилась
//        List<User> userFriends = new ArrayList<>(getFriends(getUserById(id)));
//        List<User> otherFriends = getFriends(getUserById(otherId));
//        userFriends.retainAll(otherFriends);
//        return userFriends;
//    }
//
//    @Override
//    public Collection<User> getAll() {
//        return users.values();
//    }
//
//    @Override
//    public User create(User user) {
//        // Добавляем id
//        user.setUser_id(getNextId());
//        // Если имя пользователя не задано или пустое, то задаём логин для отображения имени.
//        if (!validateName(user.getName())) {
//            user.setName(user.getLogin());
//            log.info("Имя пользователя не задано. Используем логин для отображения имени.");
//        }
//        // Добавляем нового пользователя в базу.
//        users.put(user.getUser_id(), user);
//        log.info("Пользователь {} успешно создан.", user);
//        return user;
//    }
//
//    @Override
//    public User update(User user) {
//        // Находим данного пользователя в базе.
//        User oldUser = getUserById(user.getUser_id());
//        // Если имейл отличается от старого имейла, то проверяем, есть ли пользователь в базе с таким имейлом.
//        if (user.getEmail() != null && !user.getEmail().equals(oldUser.getEmail())) {
//            checkDuplicateEmail(user);
//        }
//        // Обновляем поля, в случае удачной проверки.
//        oldUser.setEmail(user.getEmail());
//        log.info("Имаил пользователя обновлён.");
//        if (validateLogin(user.getLogin())) {
//            oldUser.setLogin(user.getLogin());
//            log.info("Логин пользователя обновлён.");
//        }
//        if (validateName(user.getName())) {
//            oldUser.setName(user.getName());
//            log.info("Имя пользователя обновлёно.");
//        }
//        oldUser.setBirthday(user.getBirthday());
//        log.info("Дата рождения пользователя обновлёна.");
//        log.info("Данные пользователя {} успешно обновлёны.", oldUser);
//        return oldUser;
//    }
//
//    @Override
//    public List<User> addFriends(long id, long friendsId) {
//        User user = getUserById(id);
//        User friend = getUserById(friendsId);
//        // Получение списков друзей пользователей
//        Set<Long> usersFriend = user.getFriends();
//        Set<Long> friendUsers = friend.getFriends();
//        // Добавление друг друга в друзья
//        usersFriend.add(friendsId);
//        friendUsers.add(id);
//        return usersFriend.stream()
//                .map(x -> getUserById(x))
//                .toList();
//    }
//
//    @Override
//    public List<User> delFriends(long id, long friendsId) {
//        User user = getUserById(id);
//        User friend = getUserById(friendsId);
//        // Получение списков друзей пользователей
//        Set<Long> usersFriend = user.getFriends();
//        Set<Long> friendUsers = friend.getFriends();
//        // Удаление друг друга из друзей
//        usersFriend.remove(friendsId);
//        friendUsers.remove(id);
//        return usersFriend.stream()
//                .map(x -> getUserById(x))
//                .toList();
//    }
//
//    // Публичный метод проверки дубликации имайлов. Публичный, так как используется в UserService
//    @Override
//    public void checkDuplicateEmail(User user) {
//        if (users.entrySet().stream()
//                .anyMatch(entry -> user.getEmail().equals(entry.getValue().getEmail()))) {
//            String errorMessage = String.format("Имайл %s уже используется", user.getEmail());
//            throw new DuplicatedDataException(errorMessage);
//        }
//    }
//
//    // Вспомогательный метод для генерации id нового пользователя.
//    private long getNextId() {
//        long currentMaxId = users.keySet()
//                .stream()
//                .mapToLong(id -> id)
//                .max()
//                .orElse(0);
//        return ++currentMaxId;
//    }
//
//    // Вспомогательный метод для валидации логина.
//    private boolean validateLogin(String login) {
//        return (login != null && !login.isBlank() && !login.contains(" "));
//    }
//
//    // Вспомогательный метод для валидации имени.
//    private boolean validateName(String name) {
//        return (name != null && !name.isBlank());
//    }
//
//    // Публичный метод поиска пользователя по id
//    public User getUserById(long id) {
//        if (!users.containsKey(id)) {
//            String errorMessage = String.format("Пользователь с id = %d не найден", id);
//            throw new NotFoundException(errorMessage);
//        }
//        return users.get(id);
//    }
//
//    //Вспомогательный метод получения списка друзей
//    private List<User> getFriends(User user) {
//        if (user.getFriends() == null) {
//            String errorMessage = String.format("У пользователя с id = %d друзья не найдены.", user.getUser_id());
//            throw new NotFoundException(errorMessage);
//        }
//        return user.getFriends().stream()
//                .map(this::getUserById)
//                .toList();
//    }
//}
