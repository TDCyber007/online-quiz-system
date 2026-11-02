package com.quizsystem.store;

import com.quizsystem.model.Difficulty;
import com.quizsystem.model.Role;
import com.quizsystem.model.User;
import com.quizsystem.service.QuizService;
import com.quizsystem.util.PasswordUtil;

import java.time.LocalDateTime;
import java.util.Arrays;

/**
 * Creates the default administrator account and a small set of sample quizzes
 * the first time the application is started.
 */
public final class DataSeeder {

    private DataSeeder() {
    }

    /** Seeds sample content only when the data folder is still empty. */
    public static void seedIfEmpty(UserStore userStore, QuizStore quizStore,
                                   QuestionStore questionStore, QuizService quizService) {
        if (userStore.count() == 0) {
            createDefaultAdmin(userStore);
        }
        if (quizStore.count() == 0) {
            createSampleQuizzes(quizService);
        }
    }

    private static void createDefaultAdmin(UserStore userStore) {
        String salt = PasswordUtil.newSalt();
        User admin = new User();
        admin.setId(userStore.nextId());
        admin.setUsername("admin");
        admin.setFullName("System Administrator");
        admin.setEmail("admin@quizsystem.local");
        admin.setSalt(salt);
        admin.setPasswordHash(PasswordUtil.hash("admin123", salt));
        admin.setRole(Role.ADMIN);
        admin.setActive(true);
        admin.setCreatedAt(LocalDateTime.now());
        userStore.save(admin);
    }

    private static void createSampleQuizzes(QuizService quizService) {
        int javaQuiz = quizService.createQuiz("Java Fundamentals",
                "Core Java concepts: OOP, exceptions, collections and the JVM.",
                "Programming", 6, 50, "admin").getId();

        int practicesQuiz = quizService.createQuiz("Java Best Practices",
                "Code style, exception handling, memory and performance habits.",
                "Best Practices", 5, 60, "admin").getId();

        int databaseQuiz = quizService.createQuiz("Database Fundamentals",
                "Relational concepts, SQL basics and normalisation.",
                "Database", 8, 50, "admin").getId();

        addJavaFundamentals(quizService, javaQuiz);
        addBestPractices(quizService, practicesQuiz);
        addDatabaseBasics(quizService, databaseQuiz);
    }

    private static void addJavaFundamentals(QuizService quizService, int quizId) {
        quizService.addMultipleChoice(quizId,
                "Which of the following is not a core principle of Object Oriented Programming?",
                Arrays.asList("Encapsulation", "Inheritance", "Compilation", "Polymorphism"),
                2, Difficulty.EASY, 1);

        quizService.addMultipleChoice(quizId,
                "What is the parent class of all exceptions in Java?",
                Arrays.asList("Throwable", "Exception", "RuntimeException", "Error"),
                0, Difficulty.EASY, 1);

        quizService.addMultipleChoice(quizId,
                "Which collection preserves insertion order and allows duplicate elements?",
                Arrays.asList("HashSet", "TreeSet", "ArrayList", "PriorityQueue"),
                2, Difficulty.EASY, 1);

        quizService.addTrueFalse(quizId,
                "Java is a statically typed, compiled and object oriented language.",
                true, Difficulty.EASY, 1);

        quizService.addMultipleChoice(quizId,
                "Which keyword is used to inherit from a superclass in Java?",
                Arrays.asList("implements", "extends", "inherits", "superclass"),
                1, Difficulty.EASY, 1);
    }

    private static void addBestPractices(QuizService quizService, int quizId) {

        quizService.addMultipleChoice(quizId,
                "Which is the correct way to release resources that implement AutoCloseable?",
                Arrays.asList("Try-finally without finally", "Try-with-resources",
                        "System.exit()", "Manual garbage collection"),
                1, Difficulty.MEDIUM, 2);

        quizService.addMultipleChoice(quizId,
                "Why should String be treated as immutable in Java?",
                Arrays.asList("It uses less memory", "It is thread safe and safe to share",
                        "It can be finalised", "It is faster to concatenate"),
                1, Difficulty.MEDIUM, 2);

        quizService.addTrueFalse(quizId,
                "Declaring a variable as final prevents it from being reassigned.",
                true, Difficulty.EASY, 1);

        quizService.addMultipleChoice(quizId,
                "Which approach best avoids NullPointerException?",
                Arrays.asList("Use == instead of equals()", "Always call equals() on a possibly null reference",
                        "Check for null before dereferencing", "Use Integer instead of int"),
                2, Difficulty.MEDIUM, 2);
    }

    private static void addDatabaseBasics(QuizService quizService, int quizId) {

        quizService.addMultipleChoice(quizId,
                "Which SQL keyword removes rows from a table?",
                Arrays.asList("DROP", "DELETE", "TRUNCATE only", "REMOVE"),
                1, Difficulty.EASY, 1);

        quizService.addMultipleChoice(quizId,
                "In which normal form is every non key attribute fully dependent on the primary key?",
                Arrays.asList("1NF", "2NF", "3NF", "BCNF"),
                1, Difficulty.MEDIUM, 2);

        quizService.addTrueFalse(quizId,
                "A foreign key must always reference the primary key of another table.",
                false, Difficulty.MEDIUM, 2);

        quizService.addMultipleChoice(quizId,
                "Which join returns only the rows that exist in both tables?",
                Arrays.asList("Left outer join", "Right outer join", "Full outer join", "Inner join"),
                3, Difficulty.EASY, 1);
    }
}
