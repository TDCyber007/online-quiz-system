package com.quizsystem.store;

import com.quizsystem.model.Difficulty;
import com.quizsystem.model.Option;
import com.quizsystem.model.Question;
import com.quizsystem.model.QuestionType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Stores questions in {@code data/questions.txt}.
 *
 * <p>A question always has four option slots. Unused slots are stored as empty
 * strings, which keeps every record the same length.</p>
 */
public class QuestionStore {

    private static final int OPTION_SLOTS = 4;

    private final Storage storage = new Storage("questions.txt");
    private final List<Question> questions = new ArrayList<>();

    public QuestionStore() {
        load();
    }

    private void load() {
        questions.clear();
        for (String line : storage.readAll()) {
            String[] p = Storage.split(line);
            Question question = new Question();
            question.setId(Integer.parseInt(Storage.field(p, 0)));
            question.setQuizId(Integer.parseInt(Storage.field(p, 1)));
            question.setText(Storage.field(p, 2));
            question.setType(QuestionType.from(Storage.field(p, 3)));
            question.setDifficulty(Difficulty.from(Storage.field(p, 4)));
            question.setPoints(Integer.parseInt(Storage.field(p, 5)));

            int correctIndex = Integer.parseInt(Storage.field(p, 10));
            List<Option> options = new ArrayList<>();
            for (int i = 0; i < OPTION_SLOTS; i++) {
                String text = Storage.field(p, 6 + i);
                if (!text.isEmpty()) {
                    options.add(new Option(text, i == correctIndex));
                }
            }
            question.setOptions(options);
            questions.add(question);
        }
    }

    private void save() {
        List<String> lines = new ArrayList<>();
        for (Question question : questions) {
            String[] slots = new String[OPTION_SLOTS];
            for (int i = 0; i < OPTION_SLOTS; i++) {
                slots[i] = i < question.getOptions().size() ? question.getOptions().get(i).getText() : "";
            }
            lines.add(Storage.join(
                    String.valueOf(question.getId()),
                    String.valueOf(question.getQuizId()),
                    question.getText(),
                    question.getType().name(),
                    question.getDifficulty().name(),
                    String.valueOf(question.getPoints()),
                    slots[0], slots[1], slots[2], slots[3],
                    String.valueOf(question.getCorrectOptionIndex())));
        }
        storage.writeAll(lines);
    }

    public int nextId() {
        int max = 0;
        for (Question question : questions) {
            max = Math.max(max, question.getId());
        }
        return max + 1;
    }

    public void save(Question question) {
        questions.removeIf(q -> q.getId() == question.getId());
        questions.add(question);
        save();
    }

    public void delete(int questionId) {
        questions.removeIf(q -> q.getId() == questionId);
        save();
    }

    public Optional<Question> findById(int id) {
        for (Question question : questions) {
            if (question.getId() == id) {
                return Optional.of(question);
            }
        }
        return Optional.empty();
    }

    /** All questions of one quiz, in the order they were added. */
    public List<Question> findByQuiz(int quizId) {
        List<Question> result = new ArrayList<>();
        for (Question question : questions) {
            if (question.getQuizId() == quizId) {
                result.add(question);
            }
        }
        result.sort((a, b) -> Integer.compare(a.getId(), b.getId()));
        return result;
    }

    public List<Question> findAll() {
        return new ArrayList<>(questions);
    }

    public int countByQuiz(int quizId) {
        int count = 0;
        for (Question question : questions) {
            if (question.getQuizId() == quizId) {
                count++;
            }
        }
        return count;
    }

    public int count() {
        return questions.size();
    }
}
