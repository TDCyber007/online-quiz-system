package com.quizsystem.model;

/** The kind of question stored in the question bank. */
public enum QuestionType {
    MULTIPLE_CHOICE,
    TRUE_FALSE;

    public static QuestionType from(String value) {
        if (value != null) {
            for (QuestionType type : values()) {
                if (type.name().equalsIgnoreCase(value.trim())) {
                    return type;
                }
            }
        }
        return MULTIPLE_CHOICE;
    }
}
