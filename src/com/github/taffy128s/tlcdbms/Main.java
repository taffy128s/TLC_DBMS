package com.github.taffy128s.tlcdbms;

import java.io.IOException;
import java.io.InputStreamReader;    
import java.io.Reader;

import com.github.taffy128s.tlcdbms.sqlparsers.ParseResult;
import com.github.taffy128s.tlcdbms.sqlparsers.SQLParser;    
    
public class Main {    

    public static void main(String[] args) throws IOException {
        String singleIns = "";
        SQLParser parser = new SQLParser();
        Reader reader = new InputStreamReader(System.in);
        int temp;
        while ((temp = reader.read()) != -1) {
            char c = (char) temp;
            if (c != ';') singleIns += c;
            else {
                if (!singleIns.equals("") && !singleIns.equals("\n") && !singleIns.equals("\r\n")) {
                    ParseResult result = parser.parse(singleIns);
                    if (result != null) {
                        System.out.print(result.toString());
                    }
                }
                singleIns = "";
            }
        }
    }
    
}
