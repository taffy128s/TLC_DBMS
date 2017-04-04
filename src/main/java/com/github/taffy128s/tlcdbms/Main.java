package com.github.taffy128s.tlcdbms;

/**
 * Main class.
 */
public class Main {
    /**
     * Program entry.
     *
     * @param args commandline arguments
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            Interpreter interpreter = new Interpreter();
            interpreter.start();
        } else {
            for (String arg : args) {
                FileInterpreter fileInterpreter = new FileInterpreter(arg);
                fileInterpreter.start();
            }
        } 
    }
}
