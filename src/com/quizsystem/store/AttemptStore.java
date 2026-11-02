package com.quizsystem.store;

import com.quizsystem.model.Answer;
import com.quizsystem.model.Attempt;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Stores finished attempts in {@code data/attempts.txt} and the answers of each
 * attempt in {@code data/answers.txt}.
 */
public class AttemptStore {

    private final Storage attemptStorage = new Storage("attempts.txt");
    private final Storage answerStorage = new Storage("answers.txt");
    private final List<Attempt> attempts = new ArrayList<>();
    private final Map<Integer, List<Answer>> answersByAttempt = new LinkedHashMap<>();

    public AttemptStore() {
        load();
    }

    private void load() {
        attempts.clear();
        answersByAttempt.clear();

        for (String line : attemptStorage.readAll()) {
            String[] p = Storage.split(line);
            Attempt attempt = new Attempt();
            attempt.setId(Integer.parseInt(Storage.field(p, 0)));
            attempt.setQuizId(Integer.parseInt(Storage.field(p, 1)));
            attempt.setUsername(Storage.field(p, 2));
            String started = Storage.field(p, 3);
            String finished = Storage.field(p, 4);
            attempt.setStartedAt(started.isEmpty() ? LocalDateTime.now() : LocalDateTime.parse(started));
            attempt.setFinishedAt(finished.isEmpty() ? null : LocalDateTime.parse(finished));
            attempt.setScore(Integer.parseInt(Storage.field(p, 5)));
            attempt.setTotalMarks(Integer.parseInt(Storage.field(p, 6)));
            attempt.setPassed(Boolean.parseBoolean(Storage.field(p, 7)));
            attempt.setAutoSubmitted(Boolean.parseBoolean(Storage.field(p, 8)));
            attempts.add(attempt);
        }

        for (String line : answerStorage.readAll()) {
            String[] p = Storage.split(line);
            int attemptId = Integer.parseInt(Storage.field(p, 0));
            Answer answer = new Answer(
                    Integer.parseInt(Storage.field(p, 1)),
                    Integer.parseInt(Storage.field(p, 2)),
                    Boolean.parseBoolean(Storage.field(p, 3)),
                    Integer.parseInt(Storage.field(p, 4)));
            answersByAttempt.computeIfAbsent(attemptId, k -> new ArrayList<>()).add(answer);
        }

        for (Attempt attempt : attempts) {
            attempt.setAnswers(answersByAttempt.getOrDefault(attempt.getId(), new ArrayList<>()));
        }
    }

    private void save() {
        List<String> attemptLines = new ArrayList<>();
        List<String> answerLines = new ArrayList<>();
        for (Attempt attempt : attempts) {
            attemptLines.add(Storage.join(
                    String.valueOf(attempt.getId()),
                    String.valueOf(attempt.getQuizId()),
                    attempt.getUsername(),
                    attempt.getStartedAt() == null ? "" : attempt.getStartedAt().toString(),
                    attempt.getFinishedAt() == null ? "" : attempt.getFinishedAt().toString(),
                    String.valueOf(attempt.getScore()),
                    String.valueOf(attempt.getTotalMarks()),
                    String.valueOf(attempt.isPassed()),
                    String.valueOf(attempt.isAutoSubmitted())));
            for (Answer answer : attempt.getAnswers()) {
                answerLines.add(Storage.join(
                        String.valueOf(attempt.getId()),
                        String.valueOf(answer.getQuestionId()),
                        String.valueOf(answer.getSelectedOptionIndex()),
                        String.valueOf(answer.isCorrect()),
                        String.valueOf(answer.getPointsAwarded())));
            }
        }
        attemptStorage.writeAll(attemptLines);
        answerStorage.writeAll(answerLines);
    }

    public int nextId() {
        int max = 0;
        for (Attempt attempt : attempts) {
            max = Math.max(max, attempt.getId());
        }
        return max + 1;
    }

    public void save(Attempt attempt) {
        attempts.removeIf(a -> a.getId() == attempt.getId());
        attempts.add(attempt);
        save();
    }

    public Optional<Attempt> findById(int id) {
        for (Attempt attempt : attempts) {
            if (attempt.getId() == id) {
                return Optional.of(attempt);
            }
        }
        return Optional.empty();
    }

    /** Every attempt, most recent first. */
    public List<Attempt> findAll() {
        List<Attempt> copy = new ArrayList<>(attempts);
        copy.sort(Comparator.comparing(Attempt::getStartedAt,
                Comparator.nullsLast(Comparator.reverseOrder())));
        return copy;
    }

    /** All attempts of one player, most recent first. */
    public List<Attempt> findByUser(String username) {
        List<Attempt> result = new ArrayList<>();
        for (Attempt attempt : attempts) {
            if (attempt.getUsername().equalsIgnoreCase(username)) {
                result.add(attempt);
            }
        }
        result.sort(Comparator.comparing(Attempt::getStartedAt,
                Comparator.nullsLast(Comparator.reverseOrder())));
        return result;
    }

    /** All attempts of one quiz, best score first. */
    public List<Attempt> findByQuiz(int quizId) {
        List<Attempt> result = new ArrayList<>();
        for (Attempt attempt : attempts) {
            if (attempt.getQuizId() == quizId) {
                result.add(attempt);
            }
        }
        result.sort(Comparator.comparingInt(Attempt::getScore).reversed()
                .thenComparing(Attempt::getPercentage, Comparator.reverseOrder()));
        return result;
    }

    public int count() {
        return attempts.size();
    }
}
