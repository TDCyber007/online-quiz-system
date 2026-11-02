package com.quizsystem.ui;

import com.quizsystem.model.Attempt;
import com.quizsystem.model.Option;
import com.quizsystem.model.Question;
import com.quizsystem.model.Quiz;
import com.quizsystem.model.User;
import com.quizsystem.service.AttemptService;
import com.quizsystem.service.QuizService;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Runs one quiz for one player, including the countdown and the review step. */
public class QuizRunner {

    private final QuizService quizService;
    private final AttemptService attemptService;

    public QuizRunner(QuizService quizService, AttemptService attemptService) {
        this.quizService = quizService;
        this.attemptService = attemptService;
    }

    /** Plays a quiz and returns the saved attempt. */
    public Attempt run(Quiz quiz, User user) {
        List<Question> questions = quizService.questionsOf(quiz.getId());
        if (questions.isEmpty()) {
            Console.error("This quiz has no questions yet, so it cannot be started.");
            return null;
        }

        Map<Integer, Integer> selections = new LinkedHashMap<>();
        QuizTimer timer = new QuizTimer(quiz.getTimeLimitMinutes());
        timer.start();

        try {
            int index = 0;
            // Set when the player asked to change one answer in the review screen.
            // Then only that single question is asked again before the review returns.
            boolean jumpToReview = false;

            while (true) {
                boolean done = false;

                while (!done) {
                    if (timer.isExpired()) {
                        done = true;
                        break;
                    }
                    Question question = questions.get(index);
                    showQuestion(question, index, questions.size(), timer);

                    String choice = Console.readLine("Your answer (1-" + question.getOptions().size()
                            + ", 0=skip, b=previous, q=finish): ", timer.remainingMillis());
                    if (choice == null) {
                        // The timer ran out or the console was closed.
                        done = true;
                        break;
                    }
                    choice = choice.trim().toLowerCase();

                    if (choice.equals("q")) {
                        done = true;
                    } else if (choice.equals("b")) {
                        if (index > 0) {
                            index--;
                        } else {
                            Console.warn("This is already the first question.");
                        }
                    } else if (choice.equals("0") || choice.equals("s")) {
                        selections.remove(question.getId());
                        Console.info("Question skipped. You can change it before submitting.");
                        index++;
                    } else {
                        int optionIndex;
                        try {
                            optionIndex = Integer.parseInt(choice) - 1;
                        } catch (NumberFormatException e) {
                            Console.error("Please type a number between 1 and "
                                    + question.getOptions().size() + ", or 0, b or q.");
                            continue;
                        }
                        if (optionIndex < 0 || optionIndex >= question.getOptions().size()) {
                            Console.error("Please type a number between 1 and "
                                    + question.getOptions().size() + ", or 0, b or q.");
                            continue;
                        }
                        selections.put(question.getId(), optionIndex);
                        Console.success("Answer recorded: "
                                + question.getOptions().get(optionIndex).getText());
                        index++;
                    }

                    if (jumpToReview) {
                        done = true;
                    } else if (index >= questions.size()) {
                        done = true;
                    }
                }

                if (timer.isExpired()) {
                    return submit(quiz, user, selections, true);
                }

                int change = review(questions, selections, timer);
                if (change < 0) {
                    return submit(quiz, user, selections, timer.isExpired());
                }
                index = change - 1;
                jumpToReview = true;
            }
        } finally {
            timer.stop();
        }
    }

    private Attempt submit(Quiz quiz, User user, Map<Integer, Integer> selections, boolean autoSubmitted) {
        Attempt attempt = attemptService.submit(quiz, user.getUsername(), selections, autoSubmitted);
        showResult(quiz, attempt);
        return attempt;
    }

    /**
     * Shows the review screen.
     *
     * @return {@code -1} when the player wants to submit, otherwise the number
     *         of the question that should be changed
     */
    private int review(List<Question> questions, Map<Integer, Integer> selections, QuizTimer timer) {
        while (true) {
            Console.section("Review your answers");
            Console.println("  #   Question                                        Answer");
            Console.rule();
            for (int i = 0; i < questions.size(); i++) {
                Question question = questions.get(i);
                Integer selected = selections.get(question.getId());
                String answer = selected == null
                        ? Console.paint(Console.YELLOW, "not answered")
                        : question.getOptions().get(selected).getText();
                Console.println(String.format("  %-3d %-48s %s", i + 1,
                        shorten(question.getText(), 46), answer));
            }
            Console.rule();
            Console.info("Time left: " + timer.formatted());

            String line = Console.readLine("Press Enter to submit, or type a question number to change it: ",
                    timer.remainingMillis());
            if (line == null) {
                return -1;
            }
            line = line.trim();
            if (line.isEmpty()) {
                return -1;
            }
            try {
                int number = Integer.parseInt(line);
                if (number < 1 || number > questions.size()) {
                    Console.error("Please type a number between 1 and " + questions.size() + ".");
                    continue;
                }
                return number;
            } catch (NumberFormatException e) {
                Console.error("Please press Enter to submit or type a question number.");
            }
        }
    }

    private void showQuestion(Question question, int index, int total, QuizTimer timer) {
        Console.section("Question " + (index + 1) + " of " + total);
        Console.println("  " + question.getText());
        Console.println();
        List<Option> options = question.getOptions();
        for (int i = 0; i < options.size(); i++) {
            Console.println("   " + Console.paint(Console.BOLD, String.valueOf(i + 1))
                    + ") " + options.get(i).getText());
        }
        Console.println();
        Console.info("Difficulty: " + question.getDifficulty() + "   Marks: " + question.getPoints()
                + "   Time left: " + timer.formatted());
    }

    /** Prints the score of a finished attempt. */
    public void showResult(Quiz quiz, Attempt attempt) {
        Console.clearScreen();
        Console.banner("Quiz completed", quiz.getTitle());

        Console.section("Your result");
        Console.println("  Score          : " + attempt.getScore() + " / " + attempt.getTotalMarks());
        Console.println("  Percentage     : " + attempt.getPercentage() + " %");
        Console.println("  Pass mark      : " + quiz.getPassPercentage() + " %");
        Console.println("  Outcome        : "
                + (attempt.isPassed()
                ? Console.paint(Console.GREEN + Console.BOLD, "PASSED")
                : Console.paint(Console.RED + Console.BOLD, "NOT PASSED")));
        Console.println("  Submitted      : "
                + (attempt.isAutoSubmitted() ? "automatically (time up)" : "manually")
                + " at " + com.quizsystem.util.DateUtil.format(attempt.getFinishedAt()));
        Console.println();

        int correct = 0;
        for (com.quizsystem.model.Answer answer : attempt.getAnswers()) {
            if (answer.isCorrect()) {
                correct++;
            }
        }
        Console.println("  Correct answers: " + correct + " of " + attempt.getAnswers().size());
        Console.println();

        Console.section("Answer sheet");
        List<Question> questions = quizService.questionsOf(quiz.getId());
        for (com.quizsystem.model.Answer answer : attempt.getAnswers()) {
            Question question = null;
            for (Question candidate : questions) {
                if (candidate.getId() == answer.getQuestionId()) {
                    question = candidate;
                    break;
                }
            }
            if (question == null) {
                continue;
            }
            String chosen = answer.getSelectedOptionIndex() >= 0
                    && answer.getSelectedOptionIndex() < question.getOptions().size()
                    ? question.getOptions().get(answer.getSelectedOptionIndex()).getText()
                    : "not answered";
            String marker = answer.isCorrect()
                    ? Console.paint(Console.GREEN + Console.BOLD, "[CORRECT]")
                    : Console.paint(Console.RED + Console.BOLD, "[WRONG]  ");
            Console.println("  " + marker + " " + question.getText());
            Console.println("           Your answer : " + chosen);
            Console.println("           Correct     : " + question.getCorrectOptionText());
        }
    }

    private static String shorten(String text, int maxLength) {
        if (text == null) {
            return "";
        }
        String clean = text.replaceAll("\\s+", " ").trim();
        if (clean.length() <= maxLength) {
            return clean;
        }
        return clean.substring(0, maxLength - 3) + "...";
    }
}
