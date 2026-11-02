package com.quizsystem.ui;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Counts down the time a player has for a quiz.
 *
 * <p>The countdown runs on its own thread so the program can end the quiz even
 * while the player is still typing an answer.</p>
 */
public class QuizTimer {

    private static final int[] WARNINGS = {60, 30, 10};

    private final long deadline;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(runnable -> {
        Thread thread = new Thread(runnable, "quiz-timer");
        thread.setDaemon(true);
        return thread;
    });

    private volatile boolean expired;
    private volatile boolean stopped;
    private int lastWarning = Integer.MAX_VALUE;

    public QuizTimer(int minutes) {
        this.deadline = System.currentTimeMillis() + minutes * 60_000L;
    }

    /** Starts the countdown. */
    public void start() {
        scheduler.scheduleAtFixedRate(this::tick, 0, 1, TimeUnit.SECONDS);
    }

    private void tick() {
        if (stopped) {
            return;
        }
        long remaining = remainingMillis();
        if (remaining <= 0) {
            expired = true;
            Console.println();
            Console.println(Console.paint(Console.RED + Console.BOLD,
                    "*  TIME IS UP!  Your quiz was submitted automatically."));
            return;
        }
        int seconds = (int) (remaining / 1000);
        for (int warning : WARNINGS) {
            if (seconds <= warning && lastWarning > warning) {
                lastWarning = warning;
                Console.println();
                Console.warn("Only " + warning + " second" + (warning == 1 ? "" : "s")
                        + " left for this quiz!");
                break;
            }
        }
    }

    /** Milliseconds that are still available. */
    public long remainingMillis() {
        return Math.max(0, deadline - System.currentTimeMillis());
    }

    /** Whole seconds that are still available. */
    public int remainingSeconds() {
        return (int) (remainingMillis() / 1000);
    }

    /** True as soon as the time limit has been reached. */
    public boolean isExpired() {
        return expired || remainingMillis() <= 0;
    }

    /** Remaining time formatted as {@code mm:ss}. */
    public String formatted() {
        int total = remainingSeconds();
        return String.format("%02d:%02d", total / 60, total % 60);
    }

    /** Stops the countdown. */
    public void stop() {
        stopped = true;
        scheduler.shutdownNow();
    }
}
