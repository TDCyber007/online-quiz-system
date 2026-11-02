package com.quizsystem.ui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Reads console input on a background thread.
 *
 * <p>A separate thread is needed because the quiz timer has to be able to stop the
 * program while the user is still sitting in front of the keyboard.</p>
 */
public final class InputReader {

    private static final String EOF_MARKER = "<<EOF>>";

    private static final BlockingQueue<String> QUEUE = new LinkedBlockingQueue<>();

    private static volatile boolean closed;

    static {
        Thread reader = new Thread(InputReader::readLoop, "console-reader");
        reader.setDaemon(true);
        reader.start();
    }

    private InputReader() {
    }

    private static void readLoop() {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in, Console.charset()));
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                QUEUE.put(line);
            }
        } catch (IOException e) {
            // the console was closed, the program stops at the next read
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            closed = true;
            QUEUE.offer(EOF_MARKER);
        }
    }

    /** Reads one line, waiting until the user types something. */
    public static String readLine() {
        if (closed && QUEUE.isEmpty()) {
            return null;
        }
        try {
            String line = QUEUE.take();
            if (EOF_MARKER.equals(line)) {
                return null;
            }
            return line;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }

    /**
     * Reads one line but gives up after the given time.
     *
     * @return the line, or {@code null} when the user did not type anything in time
     */
    public static String readLine(long timeoutMillis) {
        try {
            String line = QUEUE.poll(timeoutMillis, TimeUnit.MILLISECONDS);
            if (line == null || EOF_MARKER.equals(line)) {
                return null;
            }
            return line;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }
}
