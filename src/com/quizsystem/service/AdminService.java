package com.quizsystem.service;

import com.quizsystem.model.Role;
import com.quizsystem.model.User;
import com.quizsystem.store.AttemptStore;
import com.quizsystem.store.QuestionStore;
import com.quizsystem.store.QuizStore;
import com.quizsystem.store.UserStore;
import com.quizsystem.util.PasswordUtil;

import java.util.List;

/** Everything the administration menu needs. */
public class AdminService {

    private final UserStore userStore;
    private final QuizStore quizStore;
    private final QuestionStore questionStore;
    private final AttemptStore attemptStore;

    public AdminService(UserStore userStore, QuizStore quizStore,
                        QuestionStore questionStore, AttemptStore attemptStore) {
        this.userStore = userStore;
        this.quizStore = quizStore;
        this.questionStore = questionStore;
        this.attemptStore = attemptStore;
    }

    /** Numbers shown on the admin dashboard. */
    public String[] dashboard() {
        return new String[]{
                String.valueOf(userStore.count()),
                String.valueOf(quizStore.count()),
                String.valueOf(questionStore.count()),
                String.valueOf(attemptStore.count())
        };
    }

    public List<User> listUsers() {
        return userStore.findAll();
    }

    public List<com.quizsystem.model.Attempt> listAttempts() {
        return attemptStore.findAll();
    }

    /** Activates or deactivates a user account. */
    public void setUserActive(User user, boolean active) {
        user.setActive(active);
        userStore.save(user);
    }

    /** Changes the role of a user account. */
    public void setUserRole(User user, Role role) {
        user.setRole(role);
        userStore.save(user);
    }

    /** Resets the password of an account to a generated one. */
    public String resetPassword(User user) {
        String newPassword = "quiz" + (1000 + (int) (Math.random() * 9000));
        String salt = PasswordUtil.newSalt();
        user.setSalt(salt);
        user.setPasswordHash(PasswordUtil.hash(newPassword, salt));
        userStore.save(user);
        return newPassword;
    }
}
