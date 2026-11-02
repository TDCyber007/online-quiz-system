package com.quizsystem.model;

/** Difficulty of a question, also used to derive the default mark of that question. */
public enum Difficulty {
    EASY(1),
    MEDIUM(2),
    HARD(3);

    private final int defaultPoints;

    Difficulty(int defaultPoints) {
        this.defaultPoints = defaultPoints;
    }

    public int getDefaultPoints() {
        return defaultPoints;
    }

    public static Difficulty from(String value) {
        if (value != null) {
            for (Difficulty difficulty : values()) {
                if (difficulty.name().equalsIgnoreCase(value.trim())) {
                    return difficulty;
                }
            }
        }
        return MEDIUM;
    }
}
