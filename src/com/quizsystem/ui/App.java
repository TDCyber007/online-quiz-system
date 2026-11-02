package com.quizsystem.ui;

import com.quizsystem.model.User;
import com.quizsystem.service.AdminService;
import com.quizsystem.service.AttemptService;
import com.quizsystem.service.AuthService;
import com.quizsystem.service.QuizService;
import com.quizsystem.service.ResultService;
import com.quizsystem.store.AttemptStore;
import com.quizsystem.store.DataSeeder;
import com.quizsystem.store.QuestionStore;
import com.quizsystem.store.QuizStore;
import com.quizsystem.store.UserStore;

/**
 * Wires the stores, the services and the screens together and runs the program.
 */
public class App {

    private final UserStore userStore = new UserStore();
    private final QuizStore quizStore = new QuizStore();
    private final QuestionStore questionStore = new QuestionStore();
    private final AttemptStore attemptStore = new AttemptStore();

    private final AuthService authService = new AuthService(userStore);
    private final QuizService quizService = new QuizService(quizStore, questionStore);
    private final AttemptService attemptService = new AttemptService(attemptStore, quizService);
    private final ResultService resultService = new ResultService(attemptStore, quizStore);
    private final AdminService adminService =
            new AdminService(userStore, quizStore, questionStore, attemptStore);

    private final LoginScreen loginScreen = new LoginScreen(authService);
    private final QuizRunner quizRunner = new QuizRunner(quizService, attemptService);
    private final PlayerMenu playerMenu =
            new PlayerMenu(quizService, attemptService, resultService, authService, quizRunner);
    private final AdminMenu adminMenu = new AdminMenu(quizService, adminService, resultService);

    /** Starts the program. */
    public void run() {
        DataSeeder.seedIfEmpty(userStore, quizStore, questionStore, quizService);

        while (true) {
            User user;
            try {
                user = loginScreen.run();
            } catch (Console.EndOfInputException e) {
                break;
            }
            if (user == null) {
                break;
            }
            try {
                if (user.isAdmin()) {
                    adminMenu.run(user);
                } else {
                    playerMenu.run(user);
                }
            } catch (Console.EndOfInputException e) {
                break;
            }
        }
        Console.println();
        Console.info("Thanks for using the Online Quiz System!");
    }
}
