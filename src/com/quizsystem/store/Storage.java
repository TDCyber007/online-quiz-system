package com.quizsystem.store;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Very small helper that reads and writes the flat text files used as the data store.
 *
 * <p>Every record is stored on one line and the fields are separated by {@code |}.</p>
 */
public final class Storage {

    public static final String SEPARATOR = "|";

    /**
     * Folder that holds the data files.
     *
     * <p>It can be changed with the {@code quiz.data.dir} system property, which the
     * test suite uses to run every test against its own empty folder.</p>
     */
    private static Path dataDir() {
        return Paths.get(System.getProperty("quiz.data.dir", "data")).toAbsolutePath();
    }

    private final Path file;

    public Storage(String fileName) {
        this.file = dataDir().resolve(fileName);
    }

    public Path getFile() {
        return file;
    }

    /** Reads every non blank line of the file. Missing files are treated as empty. */
    public List<String> readAll() {
        if (!Files.exists(file)) {
            return new ArrayList<>();
        }
        try {
            List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
            List<String> result = new ArrayList<>();
            for (String line : lines) {
                if (!line.trim().isEmpty()) {
                    result.add(line);
                }
            }
            return result;
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot read " + file, e);
        }
    }

    /** Replaces the whole file with the given lines. */
    public void writeAll(List<String> lines) {
        try {
            Files.createDirectories(dataDir());
            Files.write(file, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot write " + file, e);
        }
    }

    /** Adds one line at the end of the file. */
    public void append(String line) {
        try {
            Files.createDirectories(dataDir());
            Files.write(file, Collections.singletonList(line), StandardCharsets.UTF_8,
                    java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot write " + file, e);
        }
    }

    /** Splits a stored line into its fields. */
    public static String[] split(String line) {
        return line.split("\\" + SEPARATOR, -1);
    }

    /** Joins fields back into one stored line. */
    public static String join(String... fields) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < fields.length; i++) {
            if (i > 0) {
                builder.append(SEPARATOR);
            }
            builder.append(safe(fields[i]));
        }
        return builder.toString();
    }

    /**
     * Removes the field separator from free text so a record always keeps its shape.
     * The trade off is that a literal {@code |} typed by the user is stored as {@code /}.
     */
    public static String safe(String value) {
        if (value == null) {
            return "";
        }
        return value.replace(SEPARATOR, "/")
                .replace("\r", " ")
                .replace("\n", " ")
                .trim();
    }

    /** Reads a field and returns an empty string when the index is out of range. */
    public static String field(String[] parts, int index) {
        if (index < 0 || index >= parts.length) {
            return "";
        }
        String value = parts[index];
        return value == null ? "" : value;
    }
}
