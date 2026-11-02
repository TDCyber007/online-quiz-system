package com.quizsystem;

import com.quizsystem.ui.App;
import com.quizsystem.ui.Console;

/**
 * Entry point of the Online Quiz System.
 *
 * <p>Run it with {@code java -cp out com.quizsystem.Main} or use the
 * {@code run.bat} / {@code run.sh} scripts.</p>
 */
public class Main {

    public static void main(String[] args) {
        Console.clearScreen();
        new App().run();
    }
}
