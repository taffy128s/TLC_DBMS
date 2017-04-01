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
        } else if (args.length > 0) {
            for (String arg : args) {
                FileInterpreter fileInterpreter = new FileInterpreter(arg);
                fileInterpreter.start();
            }
        } else {
            System.out.println("usage: executable [file1] [file2] [file3] ...");
        }
    }
}
