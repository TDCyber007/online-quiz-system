package com.quizsystem.store;

import com.quizsystem.model.Role;
import com.quizsystem.model.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Stores user accounts in {@code data/users.txt}. */
public class UserStore {

    private final Storage storage = new Storage("users.txt");
    private final List<User> users = new ArrayList<>();

    public UserStore() {
        load();
    }

    private void load() {
        users.clear();
        for (String line : storage.readAll()) {
            String[] p = Storage.split(line);
            User user = new User();
            user.setId(Integer.parseInt(Storage.field(p, 0)));
            user.setUsername(Storage.field(p, 1));
            user.setFullName(Storage.field(p, 2));
            user.setEmail(Storage.field(p, 3));
            user.setPasswordHash(Storage.field(p, 4));
            user.setSalt(Storage.field(p, 5));
            user.setRole(Role.from(Storage.field(p, 6)));
            user.setActive(Boolean.parseBoolean(Storage.field(p, 7)));
            String createdAt = Storage.field(p, 8);
            user.setCreatedAt(createdAt.isEmpty() ? LocalDateTime.now() : LocalDateTime.parse(createdAt));
            users.add(user);
        }
    }

    private void save() {
        List<String> lines = new ArrayList<>();
        for (User user : users) {
            lines.add(Storage.join(
                    String.valueOf(user.getId()),
                    user.getUsername(),
                    user.getFullName(),
                    user.getEmail(),
                    user.getPasswordHash(),
                    user.getSalt(),
                    user.getRole().name(),
                    String.valueOf(user.isActive()),
                    user.getCreatedAt() == null ? "" : user.getCreatedAt().toString()));
        }
        storage.writeAll(lines);
    }

    /** Generates the next free user id. */
    public int nextId() {
        int max = 0;
        for (User user : users) {
            max = Math.max(max, user.getId());
        }
        return max + 1;
    }

    public void save(User user) {
        users.removeIf(u -> u.getId() == user.getId());
        users.add(user);
        save();
    }

    public Optional<User> findByUsername(String username) {
        for (User user : users) {
            if (user.getUsername().equalsIgnoreCase(username)) {
                return Optional.of(user);
            }
        }
        return Optional.empty();
    }

    public Optional<User> findById(int id) {
        for (User user : users) {
            if (user.getId() == id) {
                return Optional.of(user);
            }
        }
        return Optional.empty();
    }

    public boolean exists(String username) {
        return findByUsername(username).isPresent();
    }

    public List<User> findAll() {
        return new ArrayList<>(users);
    }

    public int count() {
        return users.size();
    }
}
