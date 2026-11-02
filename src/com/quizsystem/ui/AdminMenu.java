package com.quizsystem.ui;

import com.quizsystem.model.Attempt;
import com.quizsystem.model.Difficulty;
import com.quizsystem.model.Question;
import com.quizsystem.model.Quiz;
import com.quizsystem.model.Role;
import com.quizsystem.model.User;
import com.quizsystem.service.AdminService;
import com.quizsystem.service.QuizService;
import com.quizsystem.service.ResultService;
import com.quizsystem.service.ValidationException;

import java.util.ArrayList;
import java.util.List;

/** The administration console. */
public class AdminMenu {

    private final QuizService quizService;
    private final AdminService adminService;
    private final ResultService resultService;

    public AdminMenu(QuizService quizService, AdminService adminService,
                     ResultService resultService) {
        this.quizService = quizService;
        this.adminService = adminService;
        this.resultService = resultService;
    }

    /** Runs the admin menu until the administrator logs out. */
    public void run(User admin) {
        while (true) {
            Console.clearScreen();
            Console.banner("Administration console", "Signed in as " + admin.getUsername());

            Console.section("Menu");
            Console.println("   1) Dashboard");
            Console.println("   2) Manage quizzes");
            Console.println("   3) Manage questions");
            Console.println("   4) Manage users");
            Console.println("   5) All attempts");
            Console.println("   0) Logout");
            Console.println();

            int choice = Console.readInt("Choose an option: ", 0, 5);
            switch (choice) {
                case 1:
                    showDashboard();
                    break;
                case 2:
                    manageQuizzes(admin);
                    break;
                case 3:
                    manageQuestions();
                    break;
                case 4:
                    manageUsers();
                    break;
                case 5:
                    showAllAttempts();
                    break;
                default:
                    Console.info("You have been logged out.");
                    Console.pause();
                    return;
            }
        }
    }

    private void showDashboard() {
        String[] stats = adminService.dashboard();
        Console.section("Dashboard");
        Console.println("   Registered users : " + stats[0]);
        Console.println("   Quizzes          : " + stats[1]);
        Console.println("   Questions        : " + stats[2]);
        Console.println("   Attempts played  : " + stats[3]);
        Console.pause();
    }

    // ---------------------------------------------------------------- quizzes

    private void manageQuizzes(User admin) {
        while (true) {
            Console.clearScreen();
            Console.section("Manage quizzes");
            List<Quiz> quizzes = quizService.listAll();
            if (quizzes.isEmpty()) {
                Console.warn("There are no quizzes yet.");
            }
            for (int i = 0; i < quizzes.size(); i++) {
                Quiz quiz = quizzes.get(i);
                Console.println("   " + (i + 1) + ") " + quiz.getTitle()
                        + Console.paint(Console.DIM, "  [" + quiz.getCategory()
                        + ", " + quiz.getTimeLimitMinutes() + " min, "
                        + quizService.questionCount(quiz.getId()) + " questions"
                        + (quiz.isActive() ? "" : ", INACTIVE") + "]"));
            }
            Console.println();
            Console.println("   C) Create a new quiz");
            Console.println("   0) Back");
            Console.println();

            String choice = Console.readLine("Choose an option: ").trim().toLowerCase();
            if (choice.equals("0")) {
                return;
            }
            if (choice.equals("c")) {
                createQuiz(admin);
                continue;
            }
            int index = parseIndex(choice, quizzes.size());
            if (index < 0) {
                continue;
            }
            quizSubMenu(admin, quizzes.get(index));
        }
    }

    private void quizSubMenu(User admin, Quiz quiz) {
        while (true) {
            Console.clearScreen();
            Console.section("Quiz: " + quiz.getTitle());
            Console.println("   Category     : " + quiz.getCategory());
            Console.println("   Description  : " + quiz.getDescription());
            Console.println("   Time limit   : " + quiz.getTimeLimitMinutes() + " minutes");
            Console.println("   Pass mark    : " + quiz.getPassPercentage() + " %");
            Console.println("   Questions    : " + quizService.questionCount(quiz.getId()));
            Console.println("   Status       : " + (quiz.isActive() ? "ACTIVE" : "INACTIVE"));
            Console.println("   Created by   : " + quiz.getCreatedBy());
            Console.println();
            Console.println("   1) Edit details");
            Console.println("   2) Add a question");
            Console.println("   3) Show questions");
            Console.println("   4) Activate / deactivate");
            Console.println("   5) Delete quiz");
            Console.println("   0) Back");
            Console.println();

            int choice = Console.readInt("Choose an option: ", 0, 5);
            switch (choice) {
                case 1:
                    editQuiz(quiz);
                    break;
                case 2:
                    addQuestion(quiz);
                    break;
                case 3:
                    showQuestions(quiz);
                    break;
                case 4:
                    quiz.setActive(!quiz.isActive());
                    quizService.updateQuiz(quiz, quiz.getTitle(), quiz.getDescription(),
                            quiz.getCategory(), quiz.getTimeLimitMinutes(),
                            quiz.getPassPercentage(), quiz.isActive());
                    Console.success("The quiz is now " + (quiz.isActive() ? "ACTIVE" : "INACTIVE") + ".");
                    Console.pause();
                    break;
                case 5:
                    if (Console.confirm("Delete this quiz and all of its questions?")) {
                        quizService.deleteQuiz(quiz.getId());
                        Console.success("The quiz was deleted.");
                        Console.pause();
                        return;
                    }
                    break;
                default:
                    return;
            }
        }
    }

    private void createQuiz(User admin) {
        Console.section("Create a new quiz");
        String title = Console.readLine("Title: ");
        if (title == null) {
            return;
        }
        String description = Console.readLine("Description: ");
        if (description == null) {
            return;
        }
        String category = Console.readLine("Category (for example Programming): ");
        if (category == null) {
            return;
        }
        int timeLimit = Console.readInt("Time limit in minutes (1-180): ", 1, 180);
        int passMark = Console.readInt("Pass percentage (0-100): ", 0, 100);
        try {
            Quiz quiz = quizService.createQuiz(title, description, category, timeLimit, passMark,
                    admin.getUsername());
            Console.success("Quiz '" + quiz.getTitle() + "' was created. Now add some questions.");
            addQuestion(quiz);
        } catch (ValidationException e) {
            Console.error(e.getMessage());
            Console.pause();
        }
    }

    private void editQuiz(Quiz quiz) {
        Console.section("Edit quiz");
        String title = readWithDefault("Title", quiz.getTitle());
        if (title == null) {
            return;
        }
        String description = readWithDefault("Description", quiz.getDescription());
        if (description == null) {
            return;
        }
        String category = readWithDefault("Category", quiz.getCategory());
        if (category == null) {
            return;
        }
        int timeLimit = readIntWithDefault("Time limit in minutes", quiz.getTimeLimitMinutes(), 1, 180);
        int passMark = readIntWithDefault("Pass percentage", quiz.getPassPercentage(), 0, 100);
        try {
            quizService.updateQuiz(quiz, title, description, category, timeLimit, passMark,
                    quiz.isActive());
            Console.success("The quiz was updated.");
        } catch (ValidationException e) {
            Console.error(e.getMessage());
        }
        Console.pause();
    }

    // -------------------------------------------------------------- questions

    private void manageQuestions() {
        List<Quiz> quizzes = quizService.listAll();
        if (quizzes.isEmpty()) {
            Console.warn("Create a quiz first, then you can add questions to it.");
            Console.pause();
            return;
        }
        Console.section("Choose a quiz");
        for (int i = 0; i < quizzes.size(); i++) {
            Quiz quiz = quizzes.get(i);
            Console.println("   " + (i + 1) + ") " + quiz.getTitle()
                    + Console.paint(Console.DIM, "  [" + quizService.questionCount(quiz.getId())
                    + " questions]"));
        }
        Console.println();
        int choice = Console.readInt("Which quiz? (0 = back) ", 0, quizzes.size());
        if (choice == 0) {
            return;
        }
        Quiz quiz = quizzes.get(choice - 1);
        while (true) {
            Console.clearScreen();
            Console.section("Questions of: " + quiz.getTitle());
            showQuestions(quiz);
            Console.println("   A) Add a multiple choice question");
            Console.println("   T) Add a true / false question");
            Console.println("   0) Back");
            Console.println();
            String choiceText = Console.readLine("Choose an option: ").trim().toLowerCase();
            if (choiceText.equals("0")) {
                return;
            }
            if (choiceText.equals("a")) {
                addMultipleChoice(quiz);
            } else if (choiceText.equals("t")) {
                addTrueFalse(quiz);
            }
        }
    }

    private void showQuestions(Quiz quiz) {
        List<Question> questions = quizService.questionsOf(quiz.getId());
        if (questions.isEmpty()) {
            Console.warn("This quiz has no questions yet.");
            return;
        }
        Console.println();
        for (int i = 0; i < questions.size(); i++) {
            Question question = questions.get(i);
            Console.println("   " + (i + 1) + ") " + question.getText());
            Console.println("      " + Console.paint(Console.DIM, question.getDifficulty()
                    + " | " + question.getPoints() + " marks | type: " + question.getType()
                    + " | correct: " + question.getCorrectOptionText()));
        }
    }

    private void addQuestion(Quiz quiz) {
        Console.section("Add a question");
        Console.println("   1) Multiple choice");
        Console.println("   2) True / false");
        Console.println("   0) Back");
        Console.println();
        int choice = Console.readInt("Choose an option: ", 0, 2);
        if (choice == 1) {
            addMultipleChoice(quiz);
        } else if (choice == 2) {
            addTrueFalse(quiz);
        }
    }

    private void addMultipleChoice(Quiz quiz) {
        Console.section("New multiple choice question");
        String text = Console.readLine("Question text: ");
        if (text == null) {
            return;
        }
        int optionCount = Console.readInt("How many options (2-4)? ", 2, 4);

        List<String> options = new ArrayList<>();
        for (int i = 0; i < optionCount; i++) {
            String option = Console.readLine("Option " + (i + 1) + ": ");
            if (option == null) {
                return;
            }
            options.add(option);
        }
        int correct = Console.readInt("Which option is correct (1-" + optionCount + ")? ", 1, optionCount);
        Difficulty difficulty = readDifficulty();
        int points = Console.readInt("Marks for this question (1-10): ", 1, 10);

        try {
            quizService.addMultipleChoice(quiz.getId(), text, options, correct - 1, difficulty, points);
            Console.success("The question was added.");
        } catch (ValidationException e) {
            Console.error(e.getMessage());
        }
        Console.pause();
    }

    private void addTrueFalse(Quiz quiz) {
        Console.section("New true / false question");
        String text = Console.readLine("Question text: ");
        if (text == null) {
            return;
        }
        int answer = Console.readInt("Is the statement true? (1 = yes, 2 = no): ", 1, 2);
        Difficulty difficulty = readDifficulty();
        int points = Console.readInt("Marks for this question (1-10): ", 1, 10);

        try {
            quizService.addTrueFalse(quiz.getId(), text, answer == 1, difficulty, points);
            Console.success("The question was added.");
        } catch (ValidationException e) {
            Console.error(e.getMessage());
        }
        Console.pause();
    }

    private Difficulty readDifficulty() {
        Console.println("   1) Easy   2) Medium   3) Hard");
        int choice = Console.readInt("Difficulty: ", 1, 3);
        switch (choice) {
            case 1:
                return Difficulty.EASY;
            case 3:
                return Difficulty.HARD;
            default:
                return Difficulty.MEDIUM;
        }
    }

    // ------------------------------------------------------------------ users

    private void manageUsers() {
        while (true) {
            Console.clearScreen();
            Console.section("Users");
            List<User> users = adminService.listUsers();
            for (int i = 0; i < users.size(); i++) {
                User user = users.get(i);
                Console.println("   " + (i + 1) + ") " + user.getUsername()
                        + Console.paint(Console.DIM, "  [" + user.getRole()
                        + (user.isActive() ? "" : ", deactivated") + "]")
                        + "  " + user.getFullName());
            }
            Console.println();
            Console.println("   0) Back");
            Console.println();

            int choice = Console.readInt("Choose a user: ", 0, users.size());
            if (choice == 0) {
                return;
            }
            userSubMenu(users.get(choice - 1));
        }
    }

    private void userSubMenu(User user) {
        while (true) {
            Console.clearScreen();
            Console.section("User: " + user.getUsername());
            Console.println("   Full name : " + user.getFullName());
            Console.println("   Email     : " + user.getEmail());
            Console.println("   Role      : " + user.getRole());
            Console.println("   Status    : " + (user.isActive() ? "ACTIVE" : "DEACTIVATED"));
            Console.println("   Created   : " + user.getCreatedAt());
            Console.println();
            Console.println("   1) Activate / deactivate");
            Console.println("   2) Change role");
            Console.println("   3) Reset password");
            Console.println("   0) Back");
            Console.println();

            int choice = Console.readInt("Choose an option: ", 0, 3);
            switch (choice) {
                case 1:
                    adminService.setUserActive(user, !user.isActive());
                    Console.success("The account is now "
                            + (user.isActive() ? "ACTIVE" : "DEACTIVATED") + ".");
                    Console.pause();
                    break;
                case 2: {
                    Role role = user.getRole() == Role.ADMIN ? Role.PLAYER : Role.ADMIN;
                    adminService.setUserRole(user, role);
                    Console.success("The role was changed to " + role + ".");
                    Console.pause();
                    break;
                }
                case 3: {
                    String password = adminService.resetPassword(user);
                    Console.success("The password of " + user.getUsername()
                            + " was reset to: " + password);
                    Console.pause();
                    break;
                }
                default:
                    return;
            }
        }
    }

    // --------------------------------------------------------------- attempts

    private void showAllAttempts() {
        Console.section("All attempts");
        List<Attempt> attempts = adminService.listAttempts();
        if (attempts.isEmpty()) {
            Console.warn("No quiz has been played yet.");
            Console.pause();
            return;
        }
        Console.println("  Date                 Player            Quiz                          Score");
        Console.rule();
        for (Attempt attempt : attempts) {
            Console.println(String.format("  %-20s %-17s %-28s %3d/%-3d",
                    com.quizsystem.util.DateUtil.formatDate(attempt.getStartedAt()),
                    attempt.getUsername(),
                    shorten(resultService.quizTitle(attempt.getQuizId()), 28),
                    attempt.getScore(), attempt.getTotalMarks()));
        }
        Console.rule();
        Console.pause();
    }

    // ---------------------------------------------------------------- helpers

    private static int parseIndex(String choice, int size) {
        try {
            int index = Integer.parseInt(choice) - 1;
            if (index < 0 || index >= size) {
                Console.error("Please choose a number between 1 and " + size + ".");
                return -1;
            }
            return index;
        } catch (NumberFormatException e) {
            Console.error("Please type a number, c or 0.");
            return -1;
        }
    }

    private static String readWithDefault(String label, String current) {
        String value = Console.readLine(label + " [" + current + "]: ");
        if (value == null) {
            return null;
        }
        value = value.trim();
        return value.isEmpty() ? current : value;
    }

    private static int readIntWithDefault(String label, int current, int min, int max) {
        String value = Console.readLine(label + " [" + current + "]: ");
        if (value == null) {
            return current;
        }
        value = value.trim();
        if (value.isEmpty()) {
            return current;
        }
        try {
            int parsed = Integer.parseInt(value);
            if (parsed < min || parsed > max) {
                Console.error("The value must be between " + min + " and " + max + ".");
                return current;
            }
            return parsed;
        } catch (NumberFormatException e) {
            Console.error("'" + value + "' is not a number, the old value is kept.");
            return current;
        }
    }

    private static String shorten(String text, int maxLength) {
        if (text == null) {
            return "";
        }
        String clean = text.replaceAll("\\s+", " ").trim();
        return clean.length() <= maxLength ? clean : clean.substring(0, maxLength - 3) + "...";
    }
}
