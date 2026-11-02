package com.quizsystem.store;

import com.quizsystem.model.Quiz;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Stores quizzes in {@code data/quizzes.txt}. */
public class QuizStore {

    private final Storage storage = new Storage("quizzes.txt");
    private final List<Quiz> quizzes = new ArrayList<>();

    public QuizStore() {
        load();
    }

    private void load() {
        quizzes.clear();
        for (String line : storage.readAll()) {
            String[] p = Storage.split(line);
            Quiz quiz = new Quiz();
            quiz.setId(Integer.parseInt(Storage.field(p, 0)));
            quiz.setTitle(Storage.field(p, 1));
            quiz.setDescription(Storage.field(p, 2));
            quiz.setCategory(Storage.field(p, 3));
            quiz.setTimeLimitMinutes(Integer.parseInt(Storage.field(p, 4)));
            quiz.setPassPercentage(Integer.parseInt(Storage.field(p, 5)));
            quiz.setActive(Boolean.parseBoolean(Storage.field(p, 6)));
            quiz.setCreatedBy(Storage.field(p, 7));
            quizzes.add(quiz);
        }
    }

    private void save() {
        List<String> lines = new ArrayList<>();
        for (Quiz quiz : quizzes) {
            lines.add(Storage.join(
                    String.valueOf(quiz.getId()),
                    quiz.getTitle(),
                    quiz.getDescription(),
                    quiz.getCategory(),
                    String.valueOf(quiz.getTimeLimitMinutes()),
                    String.valueOf(quiz.getPassPercentage()),
                    String.valueOf(quiz.isActive()),
                    quiz.getCreatedBy()));
        }
        storage.writeAll(lines);
    }

    public int nextId() {
        int max = 0;
        for (Quiz quiz : quizzes) {
            max = Math.max(max, quiz.getId());
        }
        return max + 1;
    }

    public void save(Quiz quiz) {
        quizzes.removeIf(q -> q.getId() == quiz.getId());
        quizzes.add(quiz);
        save();
    }

    public void delete(int quizId) {
        quizzes.removeIf(q -> q.getId() == quizId);
        save();
    }

    public Optional<Quiz> findById(int id) {
        for (Quiz quiz : quizzes) {
            if (quiz.getId() == id) {
                return Optional.of(quiz);
            }
        }
        return Optional.empty();
    }

    /** All quizzes, newest first. */
    public List<Quiz> findAll() {
        List<Quiz> copy = new ArrayList<>(quizzes);
        copy.sort((a, b) -> Integer.compare(b.getId(), a.getId()));
        return copy;
    }

    /** Only quizzes that players are allowed to see. */
    public List<Quiz> findActive() {
        List<Quiz> result = new ArrayList<>();
        for (Quiz quiz : quizzes) {
            if (quiz.isActive()) {
                result.add(quiz);
            }
        }
        result.sort((a, b) -> Integer.compare(b.getId(), a.getId()));
        return result;
    }

    public int count() {
        return quizzes.size();
    }
}
