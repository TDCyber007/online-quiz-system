package com.quizsystem.service;

import com.quizsystem.model.Answer;
import com.quizsystem.model.Attempt;
import com.quizsystem.model.Question;
import com.quizsystem.model.Quiz;
import com.quizsystem.store.AttemptStore;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Evaluates a finished quiz and stores the attempt. */
public class AttemptService {

    private final AttemptStore attemptStore;
    private final QuizService quizService;

    public AttemptService(AttemptStore attemptStore, QuizService quizService) {
        this.attemptStore = attemptStore;
        this.quizService = quizService;
    }

    /**
     * Scores the given answers and saves the attempt.
     *
     * @param selections    question id to the index of the chosen option
     * @param autoSubmitted {@code true} when the time limit was reached
     */
    public Attempt submit(Quiz quiz, String username, Map<Integer, Integer> selections,
                          boolean autoSubmitted) {
        List<Question> questions = quizService.questionsOf(quiz.getId());
        if (questions.isEmpty()) {
            throw new ValidationException("This quiz has no questions yet.");
        }

        Attempt attempt = new Attempt();
        attempt.setId(attemptStore.nextId());
        attempt.setQuizId(quiz.getId());
        attempt.setUsername(username);
        attempt.setStartedAt(LocalDateTime.now());
        attempt.setFinishedAt(LocalDateTime.now());
        attempt.setAutoSubmitted(autoSubmitted);

        List<Answer> answers = new ArrayList<>();
        int score = 0;
        int total = 0;

        for (Question question : questions) {
            total += question.getPoints();
            Integer selected = selections == null ? null : selections.get(question.getId());
            int selectedIndex = selected == null ? -1 : selected;
            int correctIndex = question.getCorrectOptionIndex();
            boolean correct = selectedIndex == correctIndex && correctIndex >= 0;
            int awarded = correct ? question.getPoints() : 0;
            score += awarded;
            answers.add(new Answer(question.getId(), selectedIndex, correct, awarded));
        }

        attempt.setScore(score);
        attempt.setTotalMarks(total);
        attempt.setPassed(total > 0 && (score * 100.0 / total) >= quiz.getPassPercentage());
        attempt.setAnswers(answers);

        attemptStore.save(attempt);
        return attempt;
    }

    /** All attempts of one player. */
    public List<Attempt> attemptsOf(String username) {
        return attemptStore.findByUser(username);
    }

    /** All attempts of one quiz. */
    public List<Attempt> attemptsOfQuiz(int quizId) {
        return attemptStore.findByQuiz(quizId);
    }

    public Attempt requireAttempt(int attemptId) {
        return attemptStore.findById(attemptId)
                .orElseThrow(() -> new ValidationException("Attempt " + attemptId + " was not found."));
    }
}
