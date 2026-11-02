package com.quizsystem.ui;

import com.quizsystem.model.User;
import com.quizsystem.service.AuthService;
import com.quizsystem.service.ValidationException;

/** The login and registration screen. */
public class LoginScreen {

    private final AuthService authService;

    public LoginScreen(AuthService authService) {
        this.authService = authService;
    }

    /** Shows the menu until a user is logged in or the program is closed. */
    public User run() {
        while (true) {
            Console.clearScreen();
            Console.banner("Online Quiz System", "Test your knowledge, track your progress");

            Console.section("Main menu");
            Console.println("   1) Login");
            Console.println("   2) Register as a new player");
            Console.println("   3) Exit");
            Console.println();

            int choice = Console.readInt("Choose an option: ", 1, 3);
            switch (choice) {
                case 1: {
                    User user = login();
                    if (user != null) {
                        return user;
                    }
                    break;
                }
                case 2: {
                    register();
                    break;
                }
                default:
                    Console.info("Goodbye!");
                    return null;
            }
        }
    }

    private User login() {
        Console.section("Login");
        String username = Console.readLine("Username: ");
        if (username == null) {
            return null;
        }
        String password = Console.readLine("Password: ");
        if (password == null) {
            return null;
        }
        try {
            User user = authService.login(username.trim(), password);
            if (user == null) {
                Console.error("Wrong username or password. Please try again.");
                Console.pause();
                return null;
            }
            Console.success("Welcome back, " + user.getFullName() + "!");
            Console.pause();
            return user;
        } catch (ValidationException e) {
            Console.error(e.getMessage());
            Console.pause();
            return null;
        }
    }

    private void register() {
        Console.section("Create a player account");
        String username = Console.readLine("Username (3-20 chars, letters/digits/_): ");
        if (username == null) {
            return;
        }
        String fullName = Console.readLine("Full name: ");
        if (fullName == null) {
            return;
        }
        String email = Console.readLine("Email: ");
        if (email == null) {
            return;
        }
        String password = Console.readLine("Password (min 6 characters): ");
        if (password == null) {
            return;
        }
        String confirm = Console.readLine("Repeat password: ");
        if (confirm == null) {
            return;
        }
        if (!password.equals(confirm)) {
            Console.error("The two passwords do not match.");
            Console.pause();
            return;
        }
        try {
            User user = authService.register(username, fullName, email, password);
            Console.success("Account created. You can log in now as " + user.getUsername() + ".");
        } catch (ValidationException e) {
            Console.error(e.getMessage());
        }
        Console.pause();
    }
}
