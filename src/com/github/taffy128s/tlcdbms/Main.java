package com.github.taffy128s.tlcdbms;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import com.github.taffy128s.tlcdbms.sqlparsers.SQLParseResult;
import com.github.taffy128s.tlcdbms.sqlparsers.SQLParser;

public class Main {

    public static void main(String[] args) throws IOException {
        String singleIns = "";
        SQLParser parser = new SQLParser();
        Reader reader = new InputStreamReader(System.in);
        int temp;
        while ((temp = reader.read()) != -1) {
            char c = (char) temp;
            if (c == ';') {
                if (!singleIns.equals("")) {
                    SQLParseResult result = parser.parse(singleIns);
                    if (result != null) {
                        System.out.print(result.toString());
                    }
                }
                singleIns = "";
            } else {
                singleIns += c;
            }
        }
    }

}
