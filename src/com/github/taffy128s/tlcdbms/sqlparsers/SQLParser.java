package com.github.taffy128s.tlcdbms.sqlparsers;

import com.github.taffy128s.tlcdbms.CommandType;
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
    private ArrayList<Integer> mPositions;
    private boolean isValid;
    private int index;

    public SQLParser() {

    }

    public ParseResult parse(String command) {
        mCommand = command;
        mTokens = new ArrayList<>();
        mPositions = new ArrayList<>();
        index = -1;
        splitTokens();
        if (!isValid) {
            return null;
        }
        return parseCommand();
    }

    private ParseResult parseCommand() {
        String command = nextToken(true);
        if (command.equalsIgnoreCase("create")) {
            return parseCreate();
        } else if (command.equalsIgnoreCase("insert")) {
            return parseInsert();
        } else {
            System.err.println(mCommand);
            printUnderLine(getPosition(index), command.length());
            System.err.println("Unexpected command " + command + ".");
            return null;
        }
    }

    private ParseResult parseCreate() {
        ParseResult result = new ParseResult();
        result.setCommandType(CommandType.CREATE);
        if (!checkTokenIgnoreCase("table", true)) {
            System.err.println("Expect TABLE keyword after CREATE.");
            return null;
        }
        String tablename = getName();
        if (tablename == null) {
            return null;
        }
        result.setTablename(tablename);
        if (!checkTokenIgnoreCase("(", true)) {
            System.err.println("Left parenthesis '(' expected after table name");
            return null;
        }
        ArrayList<String> attributeNames = new ArrayList<>();
        ArrayList<DataType> attributeTypes = new ArrayList<>();
        int index = 0;
        while (true) {
            String attributeName = getName();
            if (attributeName == null) {
                return null;
            }
            attributeNames.add(attributeName);
            String attributeType = getAttributeType();
            if (attributeType.equals("")) {
                return null;
            }
            if (attributeType.startsWith("INT")) {
                attributeTypes.add(new DataType(DataTypeIdentifier.INT, -1));
            } else {
                String[] elements = attributeType.split(" ");
                if (elements.length > 1) {
                    int limit = Integer.parseInt(attributeType.split(" ")[1]);
                    if (limit <= 0 || limit > 40) {
                        System.err.println(limit + ": Invalid varchar length limitation");
                        return null;
                    }
                    attributeTypes.add(new DataType(DataTypeIdentifier.VARCHAR, limit));
                } else {
                    return null;
                }
            }
            if (attributeType.contains("PRIMARY")) {
                if (result.getPrimaryKeyIndex() != -1) {
                    System.err.println("Multiple Primary Key.");
                } else {
                    result.setPrimaryKeyIndex(index);
                }
            }
            if (!nextToken(false).equalsIgnoreCase(",")) {
                break;
            }
            checkTokenIgnoreCase(",", true);
            ++index;
        }
        if (!checkTokenIgnoreCase(")", true)) {
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

    private int getPosition(int index) {
        return mPositions.get(index);
    }

    private boolean isEnded() {
        String token = nextToken(true);
        return (token.equals(";") || token.equals("")) && nextToken(true).equals("");
    }

    private String getName() {
        String name = nextToken(true);
        if (!name.matches("[a-zA-Z_]*")) {
            System.err.println("Invalid name " + name + ".");
            return null;
        } else {
            return name;
        }
    }

    private String getAttributeType() {
        String type = nextToken(true);
        if (type.equalsIgnoreCase("int")) {
            String primaryResult = checkPrimaryKey();
            switch (primaryResult) {
                case "PRIMARY":
                    return "INT PRIMARY";
                case "":
                    return "INT";
                default:
                    return "";
            }
        } else if (type.equalsIgnoreCase("varchar")) {
            if (!checkTokenIgnoreCase("(", true)) {
                System.err.println("Left parenthesis '(' expected after VARCHAR.");
                return "";
            }
            String limit = nextToken(true);
            if (!DataChecker.isValidInteger(limit)) {
                System.err.println(limit + ": not a valid limitation.");
                return "";
            }
            if (!checkTokenIgnoreCase(")", true)) {
                System.err.println("Right parenthesis ')' expected at end of VARCHAR definition.");
                return "";
            }
            String primaryResult = checkPrimaryKey();
            switch (primaryResult) {
                case "PRIMARY":
                    return "VARCHAR " + limit + " PRIMARY";
                case "":
                    return "VARCHAR " + limit;
                default:
                    return "";
            }
        } else {
            System.err.println("Invalid data type " + type);
            return "";
        }
    }

    private boolean checkTokenIgnoreCase(String expected, boolean increase) {
        return nextToken(increase).equalsIgnoreCase(expected);
    }

    private String checkPrimaryKey() {
        if (checkTokenIgnoreCase("primary", false)) {
            checkTokenIgnoreCase("primary", true);
            if (checkTokenIgnoreCase("key", true)) {
                return "PRIMARY";
            } else {
                System.err.println("KEY keyword expected after PRIMARY");
                return "ERROR";
            }
        } else {
            return "";
        }
    }

    private void printUnderLine(int startIndex, int length) {
        for (int i = 0; i < startIndex; ++i) {
            System.err.print(" ");
        }
        System.err.print("^");
        for (int i = 0; i < length - 1; ++i) {
            System.err.print("~");
        }
        System.err.println("");
    }

    private void splitTokens() {
        String preProcessCommand = "";
        boolean quoteFlag = false;
        for (int i = 0; i < mCommand.length(); ++i) {
            switch (mCommand.charAt(i)) {
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
                    preProcessCommand += mCommand.charAt(i);
                    break;
            }
        }
        if (quoteFlag) {
            System.err.println("Single quote not matched");
            isValid = false;
        }
        String[] splited = preProcessCommand.split("\0");
        int startLocation = 0;
        for (String token : splited) {
            startLocation = mCommand.indexOf(token, startLocation);
            if (token.length() > 0) {
                mTokens.add(token);
                mPositions.add(startLocation);
            }
        }
        isValid = true;
    }
}
