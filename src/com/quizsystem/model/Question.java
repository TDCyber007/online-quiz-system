package com.quizsystem.model;

import java.util.ArrayList;
import java.util.List;

/** A question belonging to a quiz. */
public class Question {

    private int id;
    private int quizId;
    private String text;
    private QuestionType type;
    private Difficulty difficulty;
    private int points;
    private List<Option> options = new ArrayList<>();

    public Question() {
    }

    public Question(int id, int quizId, String text, QuestionType type,
                    Difficulty difficulty, int points, List<Option> options) {
        this.id = id;
        this.quizId = quizId;
        this.text = text;
        this.type = type;
        this.difficulty = difficulty;
        this.points = points;
        this.options = options;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getQuizId() {
        return quizId;
    }

    public void setQuizId(int quizId) {
        this.quizId = quizId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public QuestionType getType() {
        return type;
    }

    public void setType(QuestionType type) {
        this.type = type;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public List<Option> getOptions() {
        return options;
    }

    public void setOptions(List<Option> options) {
        this.options = options;
    }

    /** Index of the option marked as correct, or -1 when nothing is marked. */
    public int getCorrectOptionIndex() {
        for (int i = 0; i < options.size(); i++) {
            if (options.get(i).isCorrect()) {
                return i;
            }
        }
        return -1;
    }

    /** Text of the correct option, or an empty string when nothing is marked. */
    public String getCorrectOptionText() {
        int index = getCorrectOptionIndex();
        return index < 0 ? "" : options.get(index).getText();
    }
}
