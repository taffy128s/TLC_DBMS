package com.github.taffy128s.tlcdbms;

import java.io.FileReader;

public class FileInterpreter extends Interpreter {
    
    private FileReader inputStream = null;
    
    public FileInterpreter(String filename) {
        super();
        try {
            filename = filename.toLowerCase();
            if (!filename.endsWith(".sql")) {
                System.out.println("SQL file expected, but executed anyway...");
            }
            inputStream = new FileReader(filename);
        } catch (Exception e) {
            System.out.println("Open file error.");
            System.exit(0);
        }
    }
    
    public void start() {
        try {
            String singleIns = "";
            int temp;
            while ((temp = inputStream.read()) != -1) {
                char c = (char) temp;
                if (c == '#') {
                    ignoreTillNewLine();
                    singleIns += ' ';
                    continue;
                }
                if (c == ';') {
                    singleIns = noSpaceAtBeginning(singleIns);
                    if (!singleIns.equals("")) {
                        execute(singleIns);
                    }
                    singleIns = "";
                } else {
                    singleIns += c;
                }
            }
        } catch (Exception e) {
            System.out.println("Read file error.");
            System.exit(0);
        }
    }
    
    private void ignoreTillNewLine() {
        int temp;
        try {
            while ((temp = inputStream.read()) != -1) {
                char c = (char) temp;
                if (c == '\n') {
                    break;
                }
            }
        } catch (Exception e) {
            System.out.println("Read file error.");
            System.exit(0);
        }
    }
    
}
