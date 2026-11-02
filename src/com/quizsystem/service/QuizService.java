package com.quizsystem.service;

import com.quizsystem.model.Difficulty;
import com.quizsystem.model.Option;
import com.quizsystem.model.Question;
import com.quizsystem.model.QuestionType;
import com.quizsystem.model.Quiz;
import com.quizsystem.store.QuestionStore;
import com.quizsystem.store.QuizStore;

import java.util.ArrayList;
import java.util.List;

/** Creating and reading quizzes and questions. */
public class QuizService {

    private final QuizStore quizStore;
    private final QuestionStore questionStore;

    public QuizService(QuizStore quizStore, QuestionStore questionStore) {
        this.quizStore = quizStore;
        this.questionStore = questionStore;
    }

    public List<Quiz> listAll() {
        return quizStore.findAll();
    }

    public List<Quiz> listActive() {
        return quizStore.findActive();
    }

    /** Finds a quiz and fails with a friendly message when it does not exist. */
    public Quiz requireQuiz(int quizId) {
        return quizStore.findById(quizId)
                .orElseThrow(() -> new ValidationException("Quiz " + quizId + " was not found."));
    }

    public List<Question> questionsOf(int quizId) {
        return questionStore.findByQuiz(quizId);
    }

    /** Total number of marks of a quiz. */
    public int totalMarks(int quizId) {
        int total = 0;
        for (Question question : questionStore.findByQuiz(quizId)) {
            total += question.getPoints();
        }
        return total;
    }

    /** Creates a new quiz and returns it. */
    public Quiz createQuiz(String title, String description, String category,
                           int timeLimitMinutes, int passPercentage, String createdBy) {
        title = title == null ? "" : title.trim();
        if (title.isEmpty()) {
            throw new ValidationException("Quiz title is required.");
        }
        if (timeLimitMinutes < 1) {
            throw new ValidationException("The time limit must be at least 1 minute.");
        }
        if (passPercentage < 0 || passPercentage > 100) {
            throw new ValidationException("The pass percentage must be between 0 and 100.");
        }

        Quiz quiz = new Quiz();
        quiz.setId(quizStore.nextId());
        quiz.setTitle(title);
        quiz.setDescription(description == null ? "" : description.trim());
        quiz.setCategory(category == null || category.isEmpty() ? "General" : category.trim());
        quiz.setTimeLimitMinutes(timeLimitMinutes);
        quiz.setPassPercentage(passPercentage);
        quiz.setActive(true);
        quiz.setCreatedBy(createdBy);
        quizStore.save(quiz);
        return quiz;
    }

    /** Updates the editable fields of a quiz. */
    public Quiz updateQuiz(Quiz quiz, String title, String description, String category,
                           int timeLimitMinutes, int passPercentage, boolean active) {
        if (title == null || title.trim().isEmpty()) {
            throw new ValidationException("Quiz title is required.");
        }
        if (timeLimitMinutes < 1) {
            throw new ValidationException("The time limit must be at least 1 minute.");
        }
        if (passPercentage < 0 || passPercentage > 100) {
            throw new ValidationException("The pass percentage must be between 0 and 100.");
        }
        quiz.setTitle(title.trim());
        quiz.setDescription(description == null ? "" : description.trim());
        quiz.setCategory(category == null || category.isEmpty() ? "General" : category.trim());
        quiz.setTimeLimitMinutes(timeLimitMinutes);
        quiz.setPassPercentage(passPercentage);
        quiz.setActive(active);
        quizStore.save(quiz);
        return quiz;
    }

    /** Deletes a quiz together with all of its questions. */
    public void deleteQuiz(int quizId) {
        for (Question question : questionStore.findByQuiz(quizId)) {
            questionStore.delete(question.getId());
        }
        quizStore.delete(quizId);
    }

    /**
     * Adds a multiple choice question to a quiz.
     *
     * @param optionTexts   the texts of the options, at least two
     * @param correctIndex  the zero based index of the correct option
     */
    public Question addMultipleChoice(int quizId, String text, List<String> optionTexts,
                                      int correctIndex, Difficulty difficulty, int points) {
        text = text == null ? "" : text.trim();
        if (text.isEmpty()) {
            throw new ValidationException("The question text is required.");
        }
        if (optionTexts == null || optionTexts.size() < 2) {
            throw new ValidationException("A question needs at least two options.");
        }
        if (optionTexts.size() > 4) {
            throw new ValidationException("A question can have at most four options.");
        }
        if (correctIndex < 0 || correctIndex >= optionTexts.size()) {
            throw new ValidationException("Please mark which option is correct.");
        }
        if (points < 1) {
            throw new ValidationException("The marks of a question must be at least 1.");
        }

        List<Option> options = new ArrayList<>();
        for (int i = 0; i < optionTexts.size(); i++) {
            String optionText = optionTexts.get(i) == null ? "" : optionTexts.get(i).trim();
            if (optionText.isEmpty()) {
                throw new ValidationException("Option " + (i + 1) + " must not be empty.");
            }
            options.add(new Option(optionText, i == correctIndex));
        }

        Question question = new Question();
        question.setId(questionStore.nextId());
        question.setQuizId(quizId);
        question.setText(text);
        question.setType(QuestionType.MULTIPLE_CHOICE);
        question.setDifficulty(difficulty == null ? Difficulty.MEDIUM : difficulty);
        question.setPoints(points);
        question.setOptions(options);
        questionStore.save(question);
        return question;
    }

    /** Adds a true/false question to a quiz. */
    public Question addTrueFalse(int quizId, String text, boolean answerIsTrue,
                                 Difficulty difficulty, int points) {
        text = text == null ? "" : text.trim();
        if (text.isEmpty()) {
            throw new ValidationException("The question text is required.");
        }
        if (points < 1) {
            throw new ValidationException("The marks of a question must be at least 1.");
        }
        List<Option> options = new ArrayList<>();
        options.add(new Option("True", answerIsTrue));
        options.add(new Option("False", !answerIsTrue));

        Question question = new Question();
        question.setId(questionStore.nextId());
        question.setQuizId(quizId);
        question.setText(text);
        question.setType(QuestionType.TRUE_FALSE);
        question.setDifficulty(difficulty == null ? Difficulty.MEDIUM : difficulty);
        question.setPoints(points);
        question.setOptions(options);
        questionStore.save(question);
        return question;
    }

    /** Removes one question. */
    public void deleteQuestion(int questionId) {
        questionStore.delete(questionId);
    }

    public Question requireQuestion(int questionId) {
        return questionStore.findById(questionId)
                .orElseThrow(() -> new ValidationException("Question " + questionId + " was not found."));
    }

    public int questionCount(int quizId) {
        return questionStore.countByQuiz(quizId);
    }
}
