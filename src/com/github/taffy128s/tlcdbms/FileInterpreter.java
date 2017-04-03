package com.github.taffy128s.tlcdbms;

import com.github.taffy128s.tlcdbms.sqlparsers.SQLParser;

import java.io.FileNotFoundException;
import java.io.FileReader;

public class FileInterpreter extends Interpreter {

    private FileReader inputStream = null;
    private String mFilename;
    private boolean mIsValid;
    private boolean mFromLoad;

    public FileInterpreter(String filename) {
        super();
        mFilename = filename;
        mFromLoad = false;
        if (!filename.endsWith(".sql")) {
            System.out.println("SQL file expected, but executed anyway...");
        }
        try {
            inputStream = new FileReader(filename);
            mIsValid = true;
        } catch (Exception e) {
            System.out.println(mFilename + ": no such file or directory.");
            System.exit(0);
            mIsValid = false;
        }
    }

    public FileInterpreter(String filename, DBManager manager) {
        mParser = new SQLParser();
        mManager = manager;
        mFilename = filename;
        mFromLoad = true;
        try {
            inputStream = new FileReader(filename);
            mIsValid = true;
        } catch (FileNotFoundException e) {
            System.out.println(mFilename + ": no such file or directory.");
            mIsValid = false;
        }
    }

    public void start() {
        if (!mIsValid) {
            return;
        }
        try {
            String singleIns = "";
            int temp;
            boolean haveOneQuote = false;
            while ((temp = inputStream.read()) != -1) {
                char c = (char) temp;
                if (haveOneQuote) {
                    if (c == '\'') haveOneQuote = false;
                    singleIns += c;
                } else {
                    if (c == '#') {
                        ignoreTillNewLine();
                        singleIns += ' ';
                    } else if (c == ';') {
                        singleIns = noSpaceAtBeginning(singleIns);
                        System.out.println("~> " + singleIns);
                        if (!singleIns.equals("")) {
                            execute(singleIns);
                            singleIns = "";
                        }
                    } else if (c == '\'') {
                        singleIns += c;
                        haveOneQuote = true;
                    } else singleIns += c;
                }
            }
            singleIns = noSpaceAtBeginning(singleIns);
            if (haveOneQuote) {
                System.out.println("Quotes not matched, file: " + mFilename);
            } else if (!singleIns.equals("")) {
                System.out.println("Missing semicolon in the end, file: " + mFilename);
            }
        } catch (Exception e) {
            System.out.println(mFilename + ": read file error.");
            System.exit(0);
        }
        if (!mFromLoad) {
            execute("quit");
        }
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
