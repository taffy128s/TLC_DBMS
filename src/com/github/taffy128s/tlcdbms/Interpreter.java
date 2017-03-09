package com.github.taffy128s.tlcdbms;

import com.github.taffy128s.tlcdbms.sqlparsers.SQLParseResult;
import com.github.taffy128s.tlcdbms.sqlparsers.SQLParser;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

public class Interpreter {
    public SQLParser parser;
    public DBManager manager;

    public Interpreter() {
        parser = new SQLParser();
        manager = new DBManager();
    }

    public void start() {
        String singleIns = "";
        Reader reader = new InputStreamReader(System.in);
        int temp;
        try {
            while ((temp = reader.read()) != -1) {
                char c = (char) temp;
                if (c == ';') {
                    if (!singleIns.equals("")) {

                        execute(singleIns);
                    }
                    singleIns = "";
                } else {
                    singleIns += c;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Exit");
        }
    }

    public void execute(String singleInstruction) {
        SQLParseResult sqlParseResult = parser.parse(singleInstruction);
        if (sqlParseResult == null) {
            return;
        }
        // make call to manager(private member)
        switch (sqlParseResult.getCommandType()) {
            case CREATE:
                manager.create(sqlParseResult);
                break;
            case INSERT:
                manager.insert(sqlParseResult);
                break;
            case SELECT:
                break;
            default:
                System.out.println("YOU SHALL NOT GO HERE!!! GO BACK AND DEBUG!!!");
                break;
        }
    }
}
