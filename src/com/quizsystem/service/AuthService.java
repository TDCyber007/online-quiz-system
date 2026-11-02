package com.quizsystem.service;

import com.quizsystem.model.Role;
import com.quizsystem.model.User;
import com.quizsystem.store.UserStore;
import com.quizsystem.util.PasswordUtil;

import java.time.LocalDateTime;
import java.util.regex.Pattern;

/** Registration, login and password rules. */
public class AuthService {

    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[A-Za-z0-9_]{3,20}$");
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private final UserStore userStore;

    public AuthService(UserStore userStore) {
        this.userStore = userStore;
    }

    /** Registers a new player account and returns the created user. */
    public User register(String username, String fullName, String email, String password) {
        username = username == null ? "" : username.trim();
        fullName = fullName == null ? "" : fullName.trim();
        email = email == null ? "" : email.trim();

        if (!USERNAME_PATTERN.matcher(username).matches()) {
            throw new ValidationException(
                    "Username must be 3-20 characters and may only contain letters, digits and '_'.");
        }
        if (fullName.isEmpty()) {
            throw new ValidationException("Full name is required.");
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new ValidationException("Please enter a valid email address.");
        }
        if (!PasswordUtil.isStrongEnough(password)) {
            throw new ValidationException("Password must be at least 6 characters long.");
        }
        if (userStore.exists(username)) {
            throw new ValidationException("The username '" + username + "' is already taken.");
        }

        String salt = PasswordUtil.newSalt();
        User user = new User();
        user.setId(userStore.nextId());
        user.setUsername(username);
        user.setFullName(fullName);
        user.setEmail(email);
        user.setSalt(salt);
        user.setPasswordHash(PasswordUtil.hash(password, salt));
        user.setRole(Role.PLAYER);
        user.setActive(true);
        user.setCreatedAt(LocalDateTime.now());

        userStore.save(user);
        return user;
    }

    /** Logs a user in. Returns {@code null} when the credentials do not match. */
    public User login(String username, String password) {
        if (username == null || password == null) {
            return null;
        }
        User user = userStore.findByUsername(username.trim()).orElse(null);
        if (user == null) {
            return null;
        }
        if (!user.isActive()) {
            throw new ValidationException("This account has been deactivated by the administrator.");
        }
        if (!PasswordUtil.matches(password, user.getSalt(), user.getPasswordHash())) {
            return null;
        }
        return user;
    }

    /** Changes the password of an account after checking the old one. */
    public void changePassword(User user, String oldPassword, String newPassword) {
        if (!PasswordUtil.matches(oldPassword, user.getSalt(), user.getPasswordHash())) {
            throw new ValidationException("The current password is not correct.");
        }
        if (!PasswordUtil.isStrongEnough(newPassword)) {
            throw new ValidationException("The new password must be at least 6 characters long.");
        }
        String salt = PasswordUtil.newSalt();
        user.setSalt(salt);
        user.setPasswordHash(PasswordUtil.hash(newPassword, salt));
        userStore.save(user);
    }
}
