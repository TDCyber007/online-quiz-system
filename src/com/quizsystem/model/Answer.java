package com.quizsystem.model;

/** The answer a player gave to one question during an attempt. */
public class Answer {

    private int questionId;
    private int selectedOptionIndex;
    private boolean correct;
    private int pointsAwarded;

    public Answer() {
    }

    public Answer(int questionId, int selectedOptionIndex, boolean correct, int pointsAwarded) {
        this.questionId = questionId;
        this.selectedOptionIndex = selectedOptionIndex;
        this.correct = correct;
        this.pointsAwarded = pointsAwarded;
    }

    public int getQuestionId() {
        return questionId;
    }

    public void setQuestionId(int questionId) {
        this.questionId = questionId;
    }

    public int getSelectedOptionIndex() {
        return selectedOptionIndex;
    }

    public void setSelectedOptionIndex(int selectedOptionIndex) {
        this.selectedOptionIndex = selectedOptionIndex;
    }

    public boolean isCorrect() {
        return correct;
    }

    public void setCorrect(boolean correct) {
        this.correct = correct;
    }

    public int getPointsAwarded() {
        return pointsAwarded;
    }

    public void setPointsAwarded(int pointsAwarded) {
        this.pointsAwarded = pointsAwarded;
    }
}
