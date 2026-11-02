package com.quizsystem.ui;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Locale;

/** Small helpers for printing a readable console menu. */
public final class Console {

    public static final String RESET = "\u001B[0m";
    public static final String BOLD = "\u001B[1m";
    public static final String DIM = "\u001B[2m";
    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String BLUE = "\u001B[34m";
    public static final String MAGENTA = "\u001B[35m";
    public static final String CYAN = "\u001B[36m";

    /** The charset the console uses. Text is written with it so nothing gets garbled. */
    private static final Charset CHARSET = detectCharset();

    /** True when the console can show the nicer Unicode symbols. */
    private static final boolean UNICODE = CHARSET.newEncoder().canEncode("─✓•█");

    /** The stream every message is written to. */
    private static final PrintStream OUT =
            new PrintStream(new FileOutputStream(FileDescriptor.out), true, CHARSET);

    public static final String LINE = UNICODE ? "─" : "-";
    public static final String DOUBLE_LINE = UNICODE ? "═" : "=";
    public static final String TICK = UNICODE ? "✓" : "+";
    public static final String CROSS = UNICODE ? "✗" : "x";
    public static final String BULLET = UNICODE ? "•" : "-";
    public static final String BAR_FULL = UNICODE ? "█" : "#";
    public static final String BAR_EMPTY = UNICODE ? "░" : ".";

    private static final boolean ANSI = detectAnsiSupport();
    private static final boolean WINDOWS =
            System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("win");

    private Console() {
    }

    /** The charset used for the console. */
    public static Charset charset() {
        return CHARSET;
    }

    /**
     * Finds out which charset the console expects.
     *
     * <p>The JDK tells us the encoding it uses for {@code System.out}. When that
     * information is missing we fall back to the encoding of the operating system.</p>
     */
    private static Charset detectCharset() {
        String[] candidates = {
                System.getProperty("stdout.encoding"),
                System.getProperty("sun.stdout.encoding"),
                System.getProperty("native.encoding"),
                System.getProperty("file.encoding")
        };
        for (String candidate : candidates) {
            if (candidate == null || candidate.isEmpty()) {
                continue;
            }
            try {
                return Charset.forName(candidate);
            } catch (IllegalCharsetNameException | UnsupportedCharsetException e) {
                // try the next one
            }
        }
        return Charset.defaultCharset();
    }

    private static boolean detectAnsiSupport() {
        String override = System.getProperty("quiz.ansi");
        if (override != null) {
            return Boolean.parseBoolean(override);
        }
        if (System.getenv("NO_COLOR") != null) {
            return false;
        }
        String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        if (!os.contains("win")) {
            return true;
        }
        return System.getenv("WT_SESSION") != null
                || System.getenv("ANSICON") != null
                || System.getenv("ConEmuANSI") != null
                || System.getenv("TERM_PROGRAM") != null
                || System.getenv("TERMINAL_EMULATOR") != null;
    }

    /** True when the terminal understands colour codes. */
    public static boolean supportsColor() {
        return ANSI;
    }

    private static String color(String code, String text) {
        return ANSI ? code + text + RESET : text;
    }

    /** Applies a colour code to a text, but only when the terminal supports it. */
    public static String paint(String code, String text) {
        return color(code, text);
    }

    public static void print(String text) {
        OUT.print(text);
        OUT.flush();
    }

    public static void println() {
        OUT.println();
        OUT.flush();
    }

    public static void println(String text) {
        OUT.println(text);
        OUT.flush();
    }

    /** Clears the screen. */
    public static void clearScreen() {
        if (ANSI) {
            print("\u001B[2J\u001B[H");
        } else if (WINDOWS) {
            try {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } catch (IOException e) {
                // the screen simply stays as it is
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /** The application header. */
    public static void banner(String title, String subtitle) {
        println();
        println(color(CYAN, DOUBLE_LINE));
        println(color(BOLD + CYAN, center(title)));
        if (subtitle != null && !subtitle.isEmpty()) {
            println(color(DIM, center(subtitle)));
        }
        println(color(CYAN, DOUBLE_LINE));
    }

    /** A section heading inside a screen. */
    public static void section(String title) {
        println();
        StringBuilder heading = new StringBuilder(LINE).append(LINE).append(' ').append(title).append(' ');
        for (int i = heading.length(); i < 58; i++) {
            heading.append(LINE);
        }
        println(color(BOLD + BLUE, heading.toString()));
    }

    /** A thin separator line. */
    public static void rule() {
        println(color(DIM, LINE));
    }

    public static void error(String message) {
        println(color(RED, CROSS + " ") + message);
    }

    public static void success(String message) {
        println(color(GREEN, TICK + " ") + message);
    }

    public static void info(String message) {
        println(color(CYAN, BULLET + " ") + message);
    }

    public static void warn(String message) {
        println(color(YELLOW, "! ") + message);
    }

    /** Waits until the user presses enter. */
    public static void pause() {
        print(color(DIM, "\nPress Enter to continue..."));
        InputReader.readLine();
    }

    /** Reads a line of text. */
    public static String readLine(String prompt) {
        print(prompt);
        return InputReader.readLine();
    }

    /**
     * Reads a line of text but gives up after the given time.
     *
     * @return the line, or {@code null} when nothing was typed in time
     */
    public static String readLine(String prompt, long timeoutMillis) {
        print(prompt);
        return InputReader.readLine(timeoutMillis);
    }

    /**
     * Reads a number inside the given range and repeats until the input is valid.
     */
    public static int readInt(String prompt, int min, int max) {
        while (true) {
            String line = readLine(prompt);
            if (line == null) {
                throw new EndOfInputException();
            }
            line = line.trim();
            if (line.isEmpty()) {
                error("Please enter a number between " + min + " and " + max + ".");
                continue;
            }
            try {
                int value = Integer.parseInt(line);
                if (value < min || value > max) {
                    error("Please enter a number between " + min + " and " + max + ".");
                    continue;
                }
                return value;
            } catch (NumberFormatException e) {
                error("'" + line + "' is not a number.");
            }
        }
    }

    /** Asks a yes or no question. */
    public static boolean confirm(String prompt) {
        while (true) {
            String line = readLine(prompt + " [y/n]: ");
            if (line == null) {
                throw new EndOfInputException();
            }
            line = line.trim().toLowerCase(Locale.ROOT);
            if (line.equals("y") || line.equals("yes")) {
                return true;
            }
            if (line.equals("n") || line.equals("no")) {
                return false;
            }
            error("Please answer with y or n.");
        }
    }

    private static String center(String text) {
        int width = 60;
        int padding = Math.max(0, (width - text.length()) / 2);
        return " ".repeat(padding) + text;
    }

    /** Thrown when the console input is closed. */
    public static class EndOfInputException extends RuntimeException {

        private static final long serialVersionUID = 1L;

        public EndOfInputException() {
            super("Console input closed");
        }
    }
}
