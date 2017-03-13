package com.github.taffy128s.tlcdbms;

import com.github.taffy128s.tlcdbms.sqlparsers.SQLParseResult;
import com.github.taffy128s.tlcdbms.sqlparsers.SQLParser;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 * Interpreter, read command from stdin and pass it to DBManager.
 */
public class Interpreter {
    private SQLParser parser;
    private DBManager manager;

    /**
     * Initialize.
     */
    public Interpreter() {
        parser = new SQLParser();
        manager = new DBManager();
    }

    /**
     * Start reading command (block IO).
     */
    public void start() {
        System.out.println("Welcome!");
        System.out.println();
        System.out.print(">> ");
        String singleIns = "";
        Reader reader = new InputStreamReader(System.in);
        int temp;
        boolean printOnce = false;
        try {
            while ((temp = reader.read()) != -1) {
                char c = (char) temp;
                if (c == ';') {
                    singleIns = noSpaceAtBeginning(singleIns);
                    if (!singleIns.equals("")) {
                        execute(singleIns);
                        singleIns = "";
                    }
                    printOnce = true;
                } else if (c == '\n') {
                    singleIns += c;
                    if (printOnce) {
                        System.out.print(">> ");
                        printOnce = false;
                    } else System.out.print("-> ");
                } else singleIns += c;
            }
        } catch (IOException e) {
            System.out.println("Goodbye...");
            System.exit(0);
        }
        System.out.println("Goodbye...");
        System.exit(0);
    }

    /**
     * Execute a command (pass it to Parser and DBManager).
     *
     * @param singleInstruction a command read from stdin.
     */
    protected void execute(String singleInstruction) {
        SQLParseResult sqlParseResult = parser.parse(singleInstruction);
        if (sqlParseResult == null) {
            System.out.println();
            return;
        }
        if (sqlParseResult.getCommandType() == CommandType.QUIT ||
                sqlParseResult.getCommandType() == CommandType.EXIT) {
            System.out.println("Goodbye...");
            System.exit(0);
        }
        switch (sqlParseResult.getCommandType()) {
            case CREATE:
                manager.create(sqlParseResult);
                break;
            case INSERT:
                manager.insert(sqlParseResult);
                break;
            case SELECT:
                manager.select(sqlParseResult);
                break;
            case SHOW_TABLE_LIST:
                manager.showTableList(sqlParseResult);
                break;
            case SHOW_TABLE_CONTENT:
                manager.showTableContent(sqlParseResult);
                break;
            case DESC:
                manager.desc(sqlParseResult);
                break;
            default:
                break;
        }
        System.out.println();
    }

    /**
     * Remove all non-visible characters from beginning of string.
     *
     * @param input string to process.
     * @return a string processed.
     */
    protected String noSpaceAtBeginning(String input) {
        boolean firstValidEncountered = false;
        String temp = "";
        for (int i = 0; i < input.length(); i++) {
            if (!firstValidEncountered
                    && input.charAt(i) != ' '
                    && input.charAt(i) != '\r'
                    && input.charAt(i) != '\n'
                    && input.charAt(i) != '\t') {
                firstValidEncountered = true;
            }
            if (firstValidEncountered) temp += input.charAt(i);
        }
        return temp;
    }

}
