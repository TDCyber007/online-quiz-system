package com.quizsystem.test;

import com.quizsystem.model.Attempt;
import com.quizsystem.model.Difficulty;
import com.quizsystem.model.Question;
import com.quizsystem.model.Quiz;
import com.quizsystem.model.Role;
import com.quizsystem.model.User;
import com.quizsystem.service.AttemptService;
import com.quizsystem.service.AuthService;
import com.quizsystem.service.QuizService;
import com.quizsystem.service.ResultService;
import com.quizsystem.service.ValidationException;
import com.quizsystem.store.AttemptStore;
import com.quizsystem.store.DataSeeder;
import com.quizsystem.store.QuestionStore;
import com.quizsystem.store.QuizStore;
import com.quizsystem.store.UserStore;
import com.quizsystem.util.PasswordUtil;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A small test suite that needs no external libraries.
 *
 * <p>Run it with {@code java -cp out com.quizsystem.test.SelfTest} or the
 * {@code test.bat} / {@code test.sh} scripts. The tests run against a temporary
 * data folder, so they never touch the data of the real application.</p>
 */
public final class SelfTest {

    private static int passed;
    private static int failed;

    private SelfTest() {
    }

    public static void main(String[] args) throws Exception {
        System.out.println("Online Quiz System - self test");
        System.out.println();

        testPasswordHashing();
        testRegistrationAndLogin();
        testRegistrationValidation();
        testQuizCreation();
        testQuestionCreation();
        testAttemptScoring();
        testLeaderboard();
        testDataSeeder();
        testPersistenceAcrossRestart();

        System.out.println();
        System.out.println("---------------------------------------------");
        System.out.println("Tests passed : " + passed);
        System.out.println("Tests failed : " + failed);
        System.out.println("---------------------------------------------");

        System.exit(failed == 0 ? 0 : 1);
    }

    /**
     * Starts a test with a fresh empty data folder so the tests never influence
     * each other and never touch the data of the real application.
     */
    private static void startTest(String name) throws Exception {
        Path folder = Files.createTempDirectory("quiz-selftest");
        System.setProperty("quiz.data.dir", folder.toString());
        System.out.println(name);
    }

    // ------------------------------------------------------------------ tests

    private static void testPasswordHashing() throws Exception {
        startTest("Password hashing");
        String salt = PasswordUtil.newSalt();
        String hash = PasswordUtil.hash("secret123", salt);
        check("hash is not the plain password", !hash.equals("secret123"));
        check("hash is stable", hash.equals(PasswordUtil.hash("secret123", salt)));
        check("a different salt gives a different hash",
                !hash.equals(PasswordUtil.hash("secret123", PasswordUtil.newSalt())));
        check("correct password matches", PasswordUtil.matches("secret123", salt, hash));
        check("wrong password does not match", !PasswordUtil.matches("secret", salt, hash));
        check("short passwords are rejected", !PasswordUtil.isStrongEnough("12345"));
        check("long enough passwords are accepted", PasswordUtil.isStrongEnough("123456"));
    }

    private static void testRegistrationAndLogin() throws Exception {
        startTest("Registration and login");
        UserStore userStore = new UserStore();
        AuthService authService = new AuthService(userStore);

        User user = authService.register("rahul", "Rahul Sharma", "rahul@example.com", "pass123");
        check("the user gets an id", user.getId() > 0);
        check("the new user is a player", user.getRole() == Role.PLAYER);
        check("the password is not stored in plain text",
                !user.getPasswordHash().contains("pass123"));

        User loggedIn = authService.login("rahul", "pass123");
        check("login with the correct password works", loggedIn != null);
        check("login is case insensitive for the username",
                authService.login("RAHUL", "pass123") != null);
        check("login with a wrong password fails", authService.login("rahul", "wrong") == null);
        check("login with an unknown user fails", authService.login("nobody", "pass123") == null);

        boolean duplicateRejected = false;
        try {
            authService.register("rahul", "Another Rahul", "other@example.com", "pass123");
        } catch (ValidationException e) {
            duplicateRejected = true;
        }
        check("a duplicate username is rejected", duplicateRejected);
    }

    private static void testRegistrationValidation() throws Exception {
        startTest("Registration validation");
        AuthService authService = new AuthService(new UserStore());

        check("a too short username is rejected",
                throwsValidation(() -> authService.register("ab", "Name", "a@b.com", "pass123")));
        check("a username with a space is rejected",
                throwsValidation(() -> authService.register("bad name", "Name", "a@b.com", "pass123")));
        check("an empty full name is rejected",
                throwsValidation(() -> authService.register("goodname", "", "a@b.com", "pass123")));
        check("an invalid email is rejected",
                throwsValidation(() -> authService.register("goodname", "Name", "nope", "pass123")));
        check("a too short password is rejected",
                throwsValidation(() -> authService.register("goodname", "Name", "a@b.com", "123")));
    }

    private static void testQuizCreation() throws Exception {
        startTest("Quiz creation");
        QuizStore quizStore = new QuizStore();
        QuestionStore questionStore = new QuestionStore();
        QuizService quizService = new QuizService(quizStore, questionStore);

        Quiz quiz = quizService.createQuiz("Java Basics", "A short quiz", "Programming",
                10, 50, "admin");
        check("the quiz gets an id", quiz.getId() > 0);
        check("the quiz starts as active", quiz.isActive());
        check("the title is stored", quiz.getTitle().equals("Java Basics"));
        check("an empty title is rejected",
                throwsValidation(() -> quizService.createQuiz("  ", "d", "c", 5, 50, "admin")));
        check("a time limit below one minute is rejected",
                throwsValidation(() -> quizService.createQuiz("Title", "d", "c", 0, 50, "admin")));
        check("a pass mark above 100 is rejected",
                throwsValidation(() -> quizService.createQuiz("Title", "d", "c", 5, 101, "admin")));

        quizService.updateQuiz(quiz, "Java Basics v2", "Updated", "Programming", 15, 60, false);
        check("the title is updated", quiz.getTitle().equals("Java Basics v2"));
        check("the quiz can be deactivated", !quiz.isActive());
        check("a deactivated quiz is not listed for players",
                quizService.listActive().stream().noneMatch(q -> q.getId() == quiz.getId()));
    }

    private static void testQuestionCreation() throws Exception {
        startTest("Question creation");
        QuizStore quizStore = new QuizStore();
        QuestionStore questionStore = new QuestionStore();
        QuizService quizService = new QuizService(quizStore, questionStore);
        Quiz quiz = quizService.createQuiz("Questions", "d", "c", 5, 50, "admin");

        Question mcq = quizService.addMultipleChoice(quiz.getId(), "What is 2 + 2?",
                Arrays.asList("3", "4", "5"), 1, Difficulty.EASY, 2);
        check("the multiple choice question gets four or fewer options", mcq.getOptions().size() == 3);
        check("the correct option is stored",
                mcq.getCorrectOptionIndex() == 1 && mcq.getCorrectOptionText().equals("4"));

        Question trueFalse = quizService.addTrueFalse(quiz.getId(), "Java is a programming language.",
                true, Difficulty.EASY, 1);
        check("a true false question has two options", trueFalse.getOptions().size() == 2);
        check("the correct answer of a true false question is True",
                trueFalse.getCorrectOptionText().equals("True"));

        check("a question needs at least two options",
                throwsValidation(() -> quizService.addMultipleChoice(quiz.getId(), "text",
                        Arrays.asList("only one"), 0, Difficulty.EASY, 1)));
        check("the correct option has to be inside the option list",
                throwsValidation(() -> quizService.addMultipleChoice(quiz.getId(), "text",
                        Arrays.asList("a", "b"), 5, Difficulty.EASY, 1)));
        check("the marks of a question have to be positive",
                throwsValidation(() -> quizService.addMultipleChoice(quiz.getId(), "text",
                        Arrays.asList("a", "b"), 0, Difficulty.EASY, 0)));

        check("the total marks are the sum of all questions",
                quizService.totalMarks(quiz.getId()) == 3);
        check("the question count is correct", quizService.questionCount(quiz.getId()) == 2);
    }

    private static void testAttemptScoring() throws Exception {
        startTest("Attempt scoring");
        QuizStore quizStore = new QuizStore();
        QuestionStore questionStore = new QuestionStore();
        AttemptStore attemptStore = new AttemptStore();
        QuizService quizService = new QuizService(quizStore, questionStore);
        AttemptService attemptService = new AttemptService(attemptStore, quizService);

        Quiz quiz = quizService.createQuiz("Scoring", "d", "c", 5, 50, "admin");
        Question first = quizService.addMultipleChoice(quiz.getId(), "Q1",
                Arrays.asList("wrong", "right"), 1, Difficulty.EASY, 2);
        Question second = quizService.addMultipleChoice(quiz.getId(), "Q2",
                Arrays.asList("wrong", "right"), 1, Difficulty.EASY, 3);

        Map<Integer, Integer> selections = new LinkedHashMap<>();
        selections.put(first.getId(), 1);
        selections.put(second.getId(), 0);

        Attempt attempt = attemptService.submit(quiz, "rahul", selections, false);
        check("the score counts the correct answers", attempt.getScore() == 2);
        check("the total marks count every question", attempt.getTotalMarks() == 5);
        check("the percentage is correct", attempt.getPercentage() == 40.0);
        check("a score below the pass mark is not passed", !attempt.isPassed());
        check("both answers are stored", attempt.getAnswers().size() == 2);
        check("the wrong answer is marked as wrong", !attempt.getAnswers().get(1).isCorrect());

        Map<Integer, Integer> allCorrect = new LinkedHashMap<>();
        allCorrect.put(first.getId(), 1);
        allCorrect.put(second.getId(), 1);
        Attempt perfect = attemptService.submit(quiz, "rahul", allCorrect, true);
        check("a perfect score is 100 %", perfect.getPercentage() == 100.0);
        check("a perfect score passes", perfect.isPassed());
        check("an automatic submission is flagged", perfect.isAutoSubmitted());

        Attempt empty = attemptService.submit(quiz, "rahul", new LinkedHashMap<>(), true);
        check("an empty quiz scores zero", empty.getScore() == 0);
        check("an empty quiz has no percentage", empty.getPercentage() == 0.0);
    }

    private static void testLeaderboard() throws Exception {
        startTest("Leaderboard and statistics");
        QuizStore quizStore = new QuizStore();
        QuestionStore questionStore = new QuestionStore();
        AttemptStore attemptStore = new AttemptStore();
        QuizService quizService = new QuizService(quizStore, questionStore);
        AttemptService attemptService = new AttemptService(attemptStore, quizService);
        ResultService resultService = new ResultService(attemptStore, quizStore);

        Quiz quiz = quizService.createQuiz("Board", "d", "c", 5, 50, "admin");
        Question question = quizService.addMultipleChoice(quiz.getId(), "Q1",
                Arrays.asList("wrong", "right"), 1, Difficulty.EASY, 1);

        Map<Integer, Integer> correct = new LinkedHashMap<>();
        correct.put(question.getId(), 1);
        Map<Integer, Integer> wrong = new LinkedHashMap<>();
        wrong.put(question.getId(), 0);

        attemptService.submit(quiz, "low", wrong, false);
        attemptService.submit(quiz, "high", correct, false);
        attemptService.submit(quiz, "low", correct, false);

        List<ResultService.LeaderboardEntry> board = resultService.leaderboardForQuiz(quiz.getId());
        check("the leaderboard has one row per player", board.size() == 2);
        check("the best player comes first", board.get(0).getUsername().equals("high"));
        check("the ranks are numbered from one", board.get(0).getRank() == 1);

        ResultService.PlayerStats stats = resultService.statsOf("low");
        check("the statistics count every attempt", stats.getAttempts() == 2);
        check("the statistics count the passed quizzes", stats.getPassed() == 1);
        check("the best percentage is correct", stats.getBestPercentage() == 100.0);
        check("the average percentage is correct", stats.getAveragePercentage() == 50.0);
    }

    private static void testDataSeeder() throws Exception {
        startTest("Sample data");
        UserStore userStore = new UserStore();
        QuizStore quizStore = new QuizStore();
        QuestionStore questionStore = new QuestionStore();
        QuizService quizService = new QuizService(quizStore, questionStore);

        DataSeeder.seedIfEmpty(userStore, quizStore, questionStore, quizService);
        check("the default administrator is created", userStore.count() == 1);
        check("the administrator has the admin role",
                userStore.findByUsername("admin").map(User::isAdmin).orElse(false));
        check("the default administrator can log in",
                new AuthService(userStore).login("admin", "admin123") != null);
        check("three sample quizzes are created", quizStore.count() == 3);
        check("the sample quizzes have questions", questionStore.count() > 0);

        DataSeeder.seedIfEmpty(userStore, quizStore, questionStore, quizService);
        check("seeding twice does not create duplicates", quizStore.count() == 3
                && userStore.count() == 1);
    }

    private static void testPersistenceAcrossRestart() throws Exception {
        startTest("Data persistence");
        UserStore userStore = new UserStore();
        QuizStore quizStore = new QuizStore();
        QuestionStore questionStore = new QuestionStore();
        AttemptStore attemptStore = new AttemptStore();
        QuizService quizService = new QuizService(quizStore, questionStore);
        AuthService authService = new AuthService(userStore);

        authService.register("persist", "Persist User", "p@example.com", "pass123");
        Quiz quiz = quizService.createQuiz("Persisted", "d", "c", 5, 50, "admin");
        quizService.addMultipleChoice(quiz.getId(), "Q1",
                Arrays.asList("a", "b"), 0, Difficulty.EASY, 1);

        // Create new store objects, which read the files again.
        UserStore reloadedUsers = new UserStore();
        QuizStore reloadedQuizzes = new QuizStore();
        QuestionStore reloadedQuestions = new QuestionStore();

        check("the user survives a restart", reloadedUsers.exists("persist"));
        check("the quiz survives a restart", reloadedQuizzes.count() == 1);
        check("the question survives a restart", reloadedQuestions.count() == 1);
        check("the question keeps its correct option",
                reloadedQuestions.findByQuiz(quiz.getId()).get(0).getCorrectOptionText().equals("a"));
    }

    // ---------------------------------------------------------------- helpers

    private static boolean throwsValidation(Runnable action) {
        try {
            action.run();
            return false;
        } catch (ValidationException e) {
            return true;
        }
    }

    private static void check(String name, boolean condition) {
        if (condition) {
            passed++;
            System.out.println("  [OK]   " + name);
        } else {
            failed++;
            System.out.println("  [FAIL] " + name);
        }
    }
}
