package com.quizsystem.ui;

import com.quizsystem.model.Attempt;
import com.quizsystem.model.Quiz;
import com.quizsystem.model.User;
import com.quizsystem.service.AttemptService;
import com.quizsystem.service.AuthService;
import com.quizsystem.service.QuizService;
import com.quizsystem.service.ResultService;
import com.quizsystem.service.ValidationException;

import java.util.List;

/** The menu a logged in player sees. */
public class PlayerMenu {

    private final QuizService quizService;
    private final AttemptService attemptService;
    private final ResultService resultService;
    private final AuthService authService;
    private final QuizRunner quizRunner;

    public PlayerMenu(QuizService quizService, AttemptService attemptService,
                      ResultService resultService, AuthService authService,
                      QuizRunner quizRunner) {
        this.quizService = quizService;
        this.attemptService = attemptService;
        this.resultService = resultService;
        this.authService = authService;
        this.quizRunner = quizRunner;
    }

    /** Runs the player menu until the player logs out. */
    public void run(User user) {
        while (true) {
            Console.clearScreen();
            Console.banner("Player menu", "Signed in as " + user.getUsername()
                    + " (" + user.getFullName() + ")");

            ResultService.PlayerStats stats = resultService.statsOf(user.getUsername());

            Console.section("Overview");
            Console.println("   Quizzes taken : " + stats.getAttempts());
            Console.println("   Quizzes passed: " + stats.getPassed());
            Console.println("   Best score    : " + stats.getBestPercentage() + " %");
            Console.println("   Average score : " + stats.getAveragePercentage() + " %");
            Console.println();

            Console.section("Menu");
            Console.println("   1) Take a quiz");
            Console.println("   2) My results");
            Console.println("   3) Leaderboard");
            Console.println("   4) My statistics");
            Console.println("   5) Change my password");
            Console.println("   0) Logout");
            Console.println();

            int choice = Console.readInt("Choose an option: ", 0, 5);
            switch (choice) {
                case 1:
                    takeQuiz(user);
                    break;
                case 2:
                    showResults(user);
                    break;
                case 3:
                    showLeaderboard();
                    break;
                case 4:
                    showStatistics(user);
                    break;
                case 5:
                    changePassword(user);
                    break;
                default:
                    Console.info("You have been logged out.");
                    Console.pause();
                    return;
            }
        }
    }

    private void takeQuiz(User user) {
        Console.section("Available quizzes");
        List<Quiz> quizzes = quizService.listActive();
        if (quizzes.isEmpty()) {
            Console.warn("There are no quizzes available right now.");
            Console.pause();
            return;
        }
        for (int i = 0; i < quizzes.size(); i++) {
            Quiz quiz = quizzes.get(i);
            Console.println("   " + (i + 1) + ") " + quiz.getTitle()
                    + "  " + Console.paint(Console.DIM, "[" + quiz.getCategory()
                    + ", " + quiz.getTimeLimitMinutes() + " min, "
                    + quizService.questionCount(quiz.getId()) + " questions]"));
        }
        Console.println();

        int choice = Console.readInt("Which quiz do you want to play? (0 = back) ", 0, quizzes.size());
        if (choice == 0) {
            return;
        }
        Quiz quiz = quizzes.get(choice - 1);

        Console.clearScreen();
        Console.banner(quiz.getTitle(), quiz.getDescription());
        Console.info("Time limit  : " + quiz.getTimeLimitMinutes() + " minutes");
        Console.info("Questions   : " + quizService.questionCount(quiz.getId()));
        Console.info("Total marks : " + quizService.totalMarks(quiz.getId()));
        Console.info("Pass mark   : " + quiz.getPassPercentage() + " %");
        Console.println();
        Console.info("Inside a question you can type 0 to skip, b to go back and q to finish early.");
        Console.println();

        if (!Console.confirm("Start the quiz now?")) {
            return;
        }
        quizRunner.run(quiz, user);
        Console.pause();
    }

    private void showResults(User user) {
        Console.section("My results");
        List<Attempt> attempts = attemptService.attemptsOf(user.getUsername());
        if (attempts.isEmpty()) {
            Console.warn("You have not played any quiz yet.");
            Console.pause();
            return;
        }
        Console.println("  Date                 Quiz                          Score      Result");
        Console.rule();
        for (Attempt attempt : attempts) {
            String outcome = attempt.isPassed()
                    ? Console.paint(Console.GREEN, "PASSED")
                    : Console.paint(Console.RED, "FAILED");
            Console.println(String.format("  %-20s %-28s %3d/%-3d  %s",
                    com.quizsystem.util.DateUtil.formatDate(attempt.getStartedAt()),
                    shorten(resultService.quizTitle(attempt.getQuizId()), 28),
                    attempt.getScore(), attempt.getTotalMarks(), outcome));
        }
        Console.rule();
        Console.pause();
    }

    private void showLeaderboard() {
        Console.section("Leaderboard");
        List<ResultService.LeaderboardEntry> board = resultService.globalLeaderboard();
        if (board.isEmpty()) {
            Console.warn("Nobody has played a quiz yet.");
            Console.pause();
            return;
        }
        Console.println("  Rank  Player            Score      Percentage");
        Console.rule();
        for (ResultService.LeaderboardEntry entry : board) {
            Console.println(String.format("  %-5d %-18s %3d/%-6d %6.2f %%",
                    entry.getRank(), entry.getUsername(),
                    entry.getScore(), entry.getTotalMarks(), entry.getPercentage()));
        }
        Console.rule();
        Console.pause();
    }

    private void showStatistics(User user) {
        ResultService.PlayerStats stats = resultService.statsOf(user.getUsername());
        Console.section("My statistics");
        if (stats.getAttempts() == 0) {
            Console.warn("Play a quiz first to see your statistics.");
            Console.pause();
            return;
        }
        Console.println("   Quizzes played  : " + stats.getAttempts());
        Console.println("   Quizzes passed  : " + stats.getPassed());
        Console.println("   Pass rate       : "
                + Math.round(stats.getPassed() * 10000.0 / stats.getAttempts()) / 100.0 + " %");
        Console.println("   Total score     : " + stats.getTotalScore());
        Console.println("   Average score   : " + stats.getAveragePercentage() + " %");
        Console.println("   Best score      : " + stats.getBestPercentage() + " %");
        Console.println();
        printBar(stats.getBestPercentage(), "Best   ");
        printBar(stats.getAveragePercentage(), "Average");
        Console.pause();
    }

    private void printBar(double percentage, String label) {
        int filled = (int) Math.round(percentage / 5.0);
        StringBuilder bar = new StringBuilder();
        for (int i = 0; i < 20; i++) {
            bar.append(i < filled ? Console.BAR_FULL : Console.BAR_EMPTY);
        }
        Console.println("   " + label + " [" + Console.paint(Console.CYAN, bar.toString())
                + "] " + percentage + " %");
    }

    private void changePassword(User user) {
        Console.section("Change password");
        String oldPassword = Console.readLine("Current password: ");
        if (oldPassword == null) {
            return;
        }
        String newPassword = Console.readLine("New password (min 6 characters): ");
        if (newPassword == null) {
            return;
        }
        String confirm = Console.readLine("Repeat new password: ");
        if (confirm == null) {
            return;
        }
        if (!newPassword.equals(confirm)) {
            Console.error("The two passwords do not match.");
            Console.pause();
            return;
        }
        try {
            authService.changePassword(user, oldPassword, newPassword);
            Console.success("Your password was changed.");
        } catch (ValidationException e) {
            Console.error(e.getMessage());
        }
        Console.pause();
    }

    private static String shorten(String text, int maxLength) {
        if (text == null) {
            return "";
        }
        String clean = text.replaceAll("\\s+", " ").trim();
        return clean.length() <= maxLength ? clean : clean.substring(0, maxLength - 3) + "...";
    }
}
