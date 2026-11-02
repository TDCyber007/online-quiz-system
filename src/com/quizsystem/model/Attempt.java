package com.quizsystem.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** One play of one quiz by one player. */
public class Attempt {

    private int id;
    private int quizId;
    private String username;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
    private int score;
    private int totalMarks;
    private boolean passed;
    private boolean autoSubmitted;
    private List<Answer> answers = new ArrayList<>();

    public Attempt() {
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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public LocalDateTime getFinishedAt() {
        return finishedAt;
    }

    public void setFinishedAt(LocalDateTime finishedAt) {
        this.finishedAt = finishedAt;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getTotalMarks() {
        return totalMarks;
    }

    public void setTotalMarks(int totalMarks) {
        this.totalMarks = totalMarks;
    }

    public boolean isPassed() {
        return passed;
    }

    public void setPassed(boolean passed) {
        this.passed = passed;
    }

    public boolean isAutoSubmitted() {
        return autoSubmitted;
    }

    public void setAutoSubmitted(boolean autoSubmitted) {
        this.autoSubmitted = autoSubmitted;
    }

    public List<Answer> getAnswers() {
        return answers;
    }

    public void setAnswers(List<Answer> answers) {
        this.answers = answers;
    }

    /** Percentage of marks obtained, rounded to two decimals. */
    public double getPercentage() {
        if (totalMarks <= 0) {
            return 0.0;
        }
        return Math.round((score * 10000.0) / totalMarks) / 100.0;
    }
}
