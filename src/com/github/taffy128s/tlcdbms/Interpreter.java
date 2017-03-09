package com.github.taffy128s.tlcdbms;

import com.github.taffy128s.tlcdbms.sqlparsers.DBManager;
import com.github.taffy128s.tlcdbms.sqlparsers.SQLParseResult;
import com.github.taffy128s.tlcdbms.sqlparsers.SQLParser;

import java.io.InputStreamReader;
import java.io.Reader;

public class Interpreter {
    public DBManager manager;

    public Interpreter() {
        manager = new DBManager();
    }

    public void start() {
        String singleIns = "";
        SQLParser parser = new SQLParser();
        Reader reader = new InputStreamReader(System.in);
        int temp;
        try {
            while ((temp = reader.read()) != -1) {
                char c = (char) temp;
                if (c == ';') {
                    if (!singleIns.equals("")) {
                        SQLParseResult result = parser.parse(singleIns);
                        if (result != null) {
                            System.out.print(result.toString());
                        }
                        execute(result);
                    }
                    singleIns = "";
                } else {
                    singleIns += c;
                }
            }
        } catch (Exception e) {
            System.out.println("Exit");
        }
    }

    public void execute(SQLParseResult sqlParseResult) {
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
