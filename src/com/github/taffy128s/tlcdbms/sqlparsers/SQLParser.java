package com.github.taffy128s.tlcdbms.sqlparsers;

import com.github.taffy128s.tlcdbms.DataChecker;
import com.github.taffy128s.tlcdbms.DataType;
import com.github.taffy128s.tlcdbms.DataTypeIdentifier;

import java.util.ArrayList;

/**
 * SQL parser
 */
public class SQLParser {
    private String mCommand;
    private ArrayList<String> mTokens;
    private int index;

    public SQLParser(String command) {
        mCommand = command;
        mTokens = new ArrayList<>();
        index = -1;
        splitTokens(mCommand);
    }

    public ParseResult parse() {
        return parseCommand();
    }

    private ParseResult parseCommand() {
        String command = nextToken();
        if (command.equalsIgnoreCase("create")) {
            return parseCreate();
        } else if (command.equalsIgnoreCase("insert")) {
            return parseInsert();
        } else {
            System.err.println("Unexpected command " + command + ".");
            return null;
        }
    }

    private ParseResult parseCreate() {
        ParseResult result = new ParseResult();
        if (!checkTokenIgnoreCase("table")) {
            System.err.println("TABLE keyword expected.");
            return null;
        }
        String tablename = getName();
        if (tablename == null) {
            return null;
        }
        result.setTablename(tablename);
        if (!checkTokenIgnoreCase("(")) {
            System.err.println("Left parenthesis '(' expected after table name");
            return null;
        }
        ArrayList<String> attributeNames = new ArrayList<>();
        ArrayList<DataType> attributeTypes = new ArrayList<>();
        while (true) {
            String attributeName = getName();
            if (attributeName == null) {
                return null;
            }
            attributeNames.add(attributeName);
            String attributeType = getAttributeType();
            if (attributeType == null) {
                return null;
            }
            if (attributeType.startsWith("INT")) {
                attributeTypes.add(new DataType(DataTypeIdentifier.INT, -1));
            } else {
                int limit = Integer.parseInt(attributeType.split(" ")[1]);
                attributeTypes.add(new DataType(DataTypeIdentifier.VARCHAR, limit));
            }
            if (!nextToken(false).equalsIgnoreCase(",")) {
                break;
            }
        }
        if (!checkTokenIgnoreCase(")")) {
            System.err.println("Right parenthesis ')' expected after attribute definition.");
            return null;
        }
        if (!isEnded()) {
            System.err.println("Unexpected strings at end of line.");
            return null;
        }
        if (attributeNames.isEmpty()) {
            System.err.println("No attributes specified for the new table.");
            return null;
        }
        result.setAttributeNames(attributeNames);
        result.setAttibuteTypes(attributeTypes);
        return result;
    }

    private ParseResult parseInsert() {
        ParseResult result = new ParseResult();
        return null;
    }

    /**
     * Get current token.
     *
     * @return current token string, "" if failed
     */
    private String currentToken() {
        if (index >= 0 && index < mTokens.size()) {
            return mTokens.get(index);
        } else {
            return "";
        }
    }

    /**
     * Get next token and INCREASE INDEX BY 1.
     *
     * @return next token string, "" if failed
     */
    private String nextToken() {
        if (index + 1 >= 0 && index + 1 < mTokens.size()) {
            return mTokens.get(++index);
        } else {
            return "";
        }
    }

    /**
     * Get next token.
     *
     * @param increment true to increase index
     * @return next token string, "" if failed
     */
    private String nextToken(boolean increment) {
        if (index + 1 >= 0 && index + 1 < mTokens.size()) {
            if (increment) {
                return mTokens.get(++index);
            } else {
                return mTokens.get(index + 1);
            }
        } else {
            return "";
        }
    }

    private boolean isEnded() {
        String token = nextToken();
        return (token.equals(";") || token.equals("")) && nextToken().equals("");
    }

    private String getName() {
        String name = nextToken();
        if (!name.matches("[a-zA-Z]*")) {
            System.err.println("Invalid name " + name + ".");
            return null;
        } else {
            return name;
        }
    }

    private String getAttributeType() {
        String type = nextToken();
        if (type.equalsIgnoreCase("int")) {
            return "INT";
        } else if (type.equalsIgnoreCase("varchar")) {
            if (!checkTokenIgnoreCase("(")) {
                System.err.println("Left parenthesis '(' expected after VARCHAR.");
                return "";
            }
            String limit = nextToken();
            if (!DataChecker.isValidInteger(limit)) {
                System.err.println(limit + ": not a valid limitation.");
                return "";
            }
            if (!checkTokenIgnoreCase(")")) {
                System.err.println("Right parenthesis ')' expected at end of VARCHAR definition.");
                return "";
            }
            return "VARCHAR " + limit;
        } else {
            System.err.println("Invalid data type " + type);
            return "";
        }
    }

    private boolean checkTokenIgnoreCase(String expected) {
        return nextToken().equalsIgnoreCase(expected);
    }

    private void splitTokens(String command) {
        String preProcessCommand = "";
        boolean quoteFlag = false;
        for (int i = 0; i < command.length(); ++i) {
            switch (command.charAt(i)) {
                case '\'':
                    if (quoteFlag) {
                        preProcessCommand += "'\0";
                    } else {
                        preProcessCommand += "\0'";
                    }
                    quoteFlag = !quoteFlag;
                    break;
                case ' ':
                    if (quoteFlag) {
                        preProcessCommand += " ";
                    } else {
                        preProcessCommand += "\0";
                    }
                    break;
                case '\n':
                    preProcessCommand += "\0";
                    break;
                case '\t':
                    preProcessCommand += "\0";
                    break;
                case '<':
                    preProcessCommand += "\0<\0";
                    break;
                case '>':
                    preProcessCommand += "\0>\0";
                    break;
                case '=':
                    preProcessCommand += "\0=\0";
                    break;
                case '(':
                    preProcessCommand += "\0(\0";
                    break;
                case ')':
                    preProcessCommand += "\0)\0";
                    break;
                case ',':
                    preProcessCommand += "\0,\0";
                    break;
                case '*':
                    preProcessCommand += "\0*\0";
                    break;
                case ';':
                    preProcessCommand += "\0;\0";
                    break;
                default:
                    preProcessCommand += command.charAt(i);
                    break;
            }
        }
        String[] splited = preProcessCommand.split("\0");
        for (String token : splited) {
            if (token.length() > 0) {
                mTokens.add(token);
            }
        }
    }
}
