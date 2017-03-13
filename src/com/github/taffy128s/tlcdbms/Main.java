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
        } else if (args.length == 1) {
            FileInterpreter fileHandler = new FileInterpreter(args[0]);
            fileHandler.start();
        } else {
            System.out.println("usage: executable [file]");
        }
    }
}
