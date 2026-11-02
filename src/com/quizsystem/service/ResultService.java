package com.quizsystem.service;

import com.quizsystem.model.Attempt;
import com.quizsystem.model.Quiz;
import com.quizsystem.store.AttemptStore;
import com.quizsystem.store.QuizStore;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Leaderboards and per player statistics. */
public class ResultService {

    private final AttemptStore attemptStore;
    private final QuizStore quizStore;

    public ResultService(AttemptStore attemptStore, QuizStore quizStore) {
        this.attemptStore = attemptStore;
        this.quizStore = quizStore;
    }

    /** One row of a leaderboard. */
    public static class LeaderboardEntry {
        private final int rank;
        private final String username;
        private final String fullName;
        private final int score;
        private final int totalMarks;
        private final double percentage;

        LeaderboardEntry(int rank, String username, String fullName,
                         int score, int totalMarks, double percentage) {
            this.rank = rank;
            this.username = username;
            this.fullName = fullName;
            this.score = score;
            this.totalMarks = totalMarks;
            this.percentage = percentage;
        }

        public int getRank() {
            return rank;
        }

        public String getUsername() {
            return username;
        }

        public String getFullName() {
            return fullName;
        }

        public int getScore() {
            return score;
        }

        public int getTotalMarks() {
            return totalMarks;
        }

        public double getPercentage() {
            return percentage;
        }
    }

    /** Summary of how one player is doing. */
    public static class PlayerStats {
        private int attempts;
        private int passed;
        private double averagePercentage;
        private double bestPercentage;
        private int totalScore;

        public int getAttempts() {
            return attempts;
        }

        public int getPassed() {
            return passed;
        }

        public double getAveragePercentage() {
            return averagePercentage;
        }

        public double getBestPercentage() {
            return bestPercentage;
        }

        public int getTotalScore() {
            return totalScore;
        }
    }

    /**
     * Best attempt of every player for one quiz, highest score first.
     */
    public List<LeaderboardEntry> leaderboardForQuiz(int quizId) {
        Map<String, Attempt> bestPerUser = new LinkedHashMap<>();
        for (Attempt attempt : attemptStore.findByQuiz(quizId)) {
            Attempt current = bestPerUser.get(attempt.getUsername());
            if (current == null || attempt.getPercentage() > current.getPercentage()) {
                bestPerUser.put(attempt.getUsername(), attempt);
            }
        }
        return toEntries(bestPerUser.values());
    }

    /** Best attempt of every player over all quizzes, highest percentage first. */
    public List<LeaderboardEntry> globalLeaderboard() {
        Map<String, Attempt> bestPerUser = new LinkedHashMap<>();
        for (Attempt attempt : attemptStore.findAll()) {
            Attempt current = bestPerUser.get(attempt.getUsername());
            if (current == null || attempt.getPercentage() > current.getPercentage()) {
                bestPerUser.put(attempt.getUsername(), attempt);
            }
        }
        return toEntries(bestPerUser.values());
    }

    private List<LeaderboardEntry> toEntries(Iterable<Attempt> attempts) {
        List<Attempt> sorted = new ArrayList<>();
        for (Attempt attempt : attempts) {
            sorted.add(attempt);
        }
        sorted.sort(Comparator.comparingDouble(Attempt::getPercentage).reversed()
                .thenComparing(Comparator.comparingInt(Attempt::getScore).reversed())
                .thenComparing(Attempt::getUsername));

        List<LeaderboardEntry> entries = new ArrayList<>();
        int rank = 1;
        for (Attempt attempt : sorted) {
            entries.add(new LeaderboardEntry(rank++, attempt.getUsername(),
                    fullNameOf(attempt.getUsername()), attempt.getScore(),
                    attempt.getTotalMarks(), attempt.getPercentage()));
        }
        return entries;
    }

    private String fullNameOf(String username) {
        return username;
    }

    /** Statistics of one player. */
    public PlayerStats statsOf(String username) {
        PlayerStats stats = new PlayerStats();
        List<Attempt> attempts = attemptStore.findByUser(username);
        stats.attempts = attempts.size();
        double sum = 0;
        for (Attempt attempt : attempts) {
            sum += attempt.getPercentage();
            stats.totalScore += attempt.getScore();
            if (attempt.isPassed()) {
                stats.passed++;
            }
            if (attempt.getPercentage() > stats.bestPercentage) {
                stats.bestPercentage = attempt.getPercentage();
            }
        }
        if (!attempts.isEmpty()) {
            stats.averagePercentage = Math.round((sum / attempts.size()) * 100.0) / 100.0;
        }
        return stats;
    }

    /** Title of the quiz an attempt belongs to. */
    public String quizTitle(int quizId) {
        return quizStore.findById(quizId).map(Quiz::getTitle).orElse("Removed quiz");
    }
}
