package com.github.taffy128s.tlcdbms;

import java.io.FileReader;

public class FileInterpreter extends Interpreter {

    private FileReader inputStream = null;
    private String mFilename;

    public FileInterpreter(String filename) {
        super();
        mFilename = filename;
        try {
            filename = filename.toLowerCase();
            if (!filename.endsWith(".sql")) {
                System.out.println("SQL file expected, but executed anyway...");
            }
            inputStream = new FileReader(filename);
        } catch (Exception e) {
            System.out.println(mFilename + ": no such file or directory.");
            System.exit(0);
        }
    }

    public void start() {
        try {
            String singleIns = "";
            boolean haveOne = false;
            int temp;
            while ((temp = inputStream.read()) != -1) {
                char c = (char) temp;
                if (c == '#' && !haveOne) {
                    ignoreTillNewLine();
                    singleIns += ' ';
                    continue;
                }
                if (c == ';') {
                    singleIns = noSpaceAtBeginning(singleIns);
                    System.out.println(">> " + singleIns);
                    if (!singleIns.equals("")) {
                        execute(singleIns);
                        singleIns = "";
                    }
                } else if (c == '\'') {
                    singleIns += c;
                    haveOne = !haveOne;
                } else singleIns += c;
            }
        } catch (Exception e) {
            System.out.println(mFilename + ": read file error.");
            System.exit(0);
        }
        execute("quit");
    }

    private void ignoreTillNewLine() {
        try {
            int temp;
            while ((temp = inputStream.read()) != -1)
                if (temp == '\n')
                    break;
        } catch (Exception e) {
            System.out.println(mFilename + ": read file error.");
            System.exit(0);
        }
    }

}
