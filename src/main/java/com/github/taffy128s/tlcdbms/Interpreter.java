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
    protected SQLParser mParser;
    protected DBManager mManager;

    /**
     * Initialize.
     */
    public Interpreter() {
        mParser = new SQLParser();
        mManager = new DBManager();
        mManager.restoreFromDisk(DBManager.FILENAME);
    }

    /**
     * Start reading command (block IO).
     */
    public void start() {
        System.out.println("Welcome to TLC Database!  Command ends with ';'.");
        System.out.println();
        System.out.println("For more information, please visit:");
        System.out.println("        https://github.com/taffy128s/TLC_DBMS");
        System.out.println();
        System.out.print(">> ");
        String singleIns = "";
        Reader reader = new InputStreamReader(System.in);
        int temp;
        boolean justExecuted = false;
        boolean haveOneQuote = false;
        try {
            while ((temp = reader.read()) != -1) {
                char c = (char) temp;
                if (justExecuted && !(c == '\t' || c == '\n' || c == '\r' || c == ' ')) {
                    justExecuted = false;
                }
                if (haveOneQuote) {
                    if (c == '\'') haveOneQuote = false;
                    singleIns += c;
                } else {
                    if (c == ';') {
                        singleIns = noSpaceAtBeginning(singleIns);
                        if (!singleIns.equals("")) {
                            execute(singleIns);
                            singleIns = "";
                        }
                        justExecuted = true;
                    } else if (c == '\n') {
                        singleIns += c;
                        if (justExecuted) {
                            System.out.print(">> ");
                            justExecuted = false;
                        } else System.out.print("-> ");
                    } else if (c == '\'') {
                        haveOneQuote = !haveOneQuote;
                        singleIns += c;
                    } else singleIns += c;
                }
            }
        } catch (IOException e) {
            System.out.println("Goodbye...");
            mManager.writeToDisk(DBManager.FILENAME);
            System.exit(0);
        }
        System.out.println("Goodbye...");
        mManager.writeToDisk(DBManager.FILENAME);
        System.exit(0);
    }

    /**
     * Execute a command (pass it to Parser and DBManager).
     *
     * @param singleInstruction a command read from stdin.
     */
    protected void execute(String singleInstruction) {
        long startTime = System.currentTimeMillis();
        SQLParseResult sqlParseResult = mParser.parse(singleInstruction);
        if (sqlParseResult == null) {
            System.out.println();
            return;
        }
        if (sqlParseResult.getCommandType() == CommandType.QUIT ||
                sqlParseResult.getCommandType() == CommandType.EXIT) {
            System.out.println("Goodbye...");
            mManager.writeToDisk(DBManager.FILENAME);
            System.exit(0);
        }
        switch (sqlParseResult.getCommandType()) {
            case CREATE:
                mManager.create(sqlParseResult);
                break;
            case INSERT:
                mManager.insert(sqlParseResult);
                break;
            case SELECT:
                mManager.select(sqlParseResult);
                break;
            case DROP:
                mManager.drop(sqlParseResult);
                break;
            case SHOW_TABLE_LIST:
                mManager.showTableList(sqlParseResult);
                break;
            case SHOW_TABLE_CONTENT:
                mManager.showTableContent(sqlParseResult);
                break;
            case DESC:
                mManager.desc(sqlParseResult);
                break;
            case LOAD:
                mManager.load(sqlParseResult);
            default:
                break;
        }
        long endTime = System.currentTimeMillis();
        float deltaTime = ((float) (endTime - startTime)) / 1000;
        System.out.printf("(%.3f seconds.)\n", deltaTime);
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
