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
    private int mIndex;

    public SQLParser() {

    }

    public ParseResult parse(String command) {
        mCommand = command;
        mTokens = new ArrayList<>();
        mPositions = new ArrayList<>();
        mIndex = -1;
        splitTokens();
        if (!isValid) {
            return null;
        }
        return parseCommand();
    }

    private ParseResult parseCommand() {
        String command = nextToken(true);
        if (mTokens.size() == 0) return null;
        if (command.equalsIgnoreCase("create")) {
            return parseCreate();
        } else if (command.equalsIgnoreCase("insert")) {
            return parseInsert();
        } else {
            printErrorMessage("Unexpected command " + command, command.length());
            return null;
        }
    }

    private ParseResult parseCreate() {
        ParseResult result = new ParseResult();
        result.setCommandType(CommandType.CREATE);
        if (!checkTokenIgnoreCase("table", true)) {
            printErrorMessage("Expect keyword TABLE", mTokens.get(mIndex).length());
            return null;
        }
        String tablename = getTableName();
        if (tablename == null) {
            return null;
        }
        result.setTablename(tablename);
        if (!checkTokenIgnoreCase("(", true)) {
            printErrorMessage("Left parenthesis '(' expected after table name", 2);
            return null;
        }
        ArrayList<String> attributeNames = new ArrayList<>();
        ArrayList<DataType> attributeTypes = new ArrayList<>();
        int index = 0;
        while (true) {
            String attributeName = getAttributeName();
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
                int limit = Integer.parseInt(elements[1]);
                attributeTypes.add(new DataType(DataTypeIdentifier.VARCHAR, limit));
            }
            if (attributeType.contains("PRIMARY")) {
                if (result.getPrimaryKeyIndex() != -1) {
                    printErrorMessage(
                            "Multiple Primary Key",
                            mIndex - 1,
                            11);
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
            printErrorMessage("Right parenthesis ')' expected after attribute definition.", 2);
            return null;
        }
        if (!isEnded()) {
            System.out.println("Unexpected strings at end of line.");
            return null;
        }
        if (attributeNames.isEmpty()) {
            printErrorMessage("No attributes specified for this new table.", 2);
            return null;
        }
        result.setAttributeNames(attributeNames);
        result.setAttibuteTypes(attributeTypes);
        return result;
    }

    private ParseResult parseInsert() {
        ParseResult result = new ParseResult();
        result.setCommandType(CommandType.INSERT);
        if (!checkTokenIgnoreCase("into", true)) {
        	printErrorMessage("Expect keyword INTO", mTokens.get(mIndex).length());
        	return null;
        }
        String tablename = getTableName();
        if (tablename == null) {
        	return null;
        }
        result.setTablename(tablename);
        System.out.println(tablename);
        if (!checkTokenIgnoreCase("values", true)) {
            printErrorMessage("Expect keyword VALUES", mTokens.get(mIndex).length());
            return null;
        }
        if (!checkTokenIgnoreCase("(", true)) {
        	printErrorMessage("Left parenthesis '(' expected after table name", 2);
        	return null;
        }
        ArrayList<String> blocks = new ArrayList<>();
        while(true) {

        }
    }

    /**
     * Get current token.
     *
     * @return current token string, "" if failed
     */
    private String currentToken() {
        if (mIndex >= 0 && mIndex < mTokens.size()) {
            return mTokens.get(mIndex);
        } else {
            return "";
        }
    }

    /**
     * Get next token.
     *
     * @param increment true to increase mIndex
     * @return next token string, "" if failed
     */
    private String nextToken(boolean increment) {
        if (mIndex + 1 >= 0 && mIndex + 1 < mTokens.size()) {
            if (increment) {
                return mTokens.get(++mIndex);
            } else {
                return mTokens.get(mIndex + 1);
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

    private String getTableName() {
        String name = nextToken(true);
        if (!name.matches("[a-zA-Z_]*")) {
            printErrorMessage("Invalid table name " + name, name.length());
            return null;
        } else if (SQLKeyWords.isSQLKeyword(name)) {
            printErrorMessage("Invalid table name " + name, name.length());
            return null;
        } else {
            return name;
        }
    }

    private String getAttributeName() {String name = nextToken(true);
        if (!name.matches("[a-zA-Z_]*")) {
            printErrorMessage("Invalid attribute name " + name, name.length());
            return null;
        } else if (SQLKeyWords.isSQLKeyword(name)) {
            printErrorMessage("Invalid attribute name " + name, name.length());
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
                printErrorMessage("Left parenthesis '(' expected after VARCHAR", 2);
                return "";
            }
            String limit = nextToken(true);
            if (!DataChecker.isValidVarCharLimitation(limit)) {
                printErrorMessage("Invalid limitation", limit.length());
                return "";
            }
            if (!checkTokenIgnoreCase(")", true)) {
                printErrorMessage("Right parenthesis ')' expected at end of VARCHAR definition.", 2);
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
            printErrorMessage("Invalid data type " + type, type.length());
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
                System.out.println("KEY keyword expected after PRIMARY");
                return "ERROR";
            }
        } else {
            return "";
        }
    }

    private void printErrorMessage(String message, int underlineLength) {
        System.out.println(mCommand);
        printUnderLine(getPosition(mIndex), underlineLength);
        System.out.println(message);
    }

    private void printErrorMessage(String message, int index, int underlineLength) {
        System.out.println(mCommand);
        printUnderLine(getPosition(index), underlineLength);
        System.out.println(message);
    }

    private void printUnderLine(int startIndex, int length) {
        for (int i = 0; i < startIndex; ++i) {
            System.out.print(" ");
        }
        System.out.print("^");
        for (int i = 0; i < length - 1; ++i) {
            System.out.print("~");
        }
        System.out.println("");
    }

    private void splitTokens() {
        mCommand = mCommand.replaceAll("\n", " ");
        mCommand = mCommand.replaceAll("\t", "    ");
        String preProcessCommand = "";
        boolean quoteFlag = false;
        for (int i = 0; i < mCommand.length(); ++i) {
            if (quoteFlag && mCommand.charAt(i) != '\'') {
                preProcessCommand += mCommand.charAt(i);
                continue;
            }
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
                    preProcessCommand += "\0";
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
            System.out.println("Single quote not matched");
            isValid = false;
        }
        String[] splited = preProcessCommand.split("\0");
        int startLocation = 0;
        for (String token : splited) {
            if (token.length() > 0) {
                startLocation = mCommand.indexOf(token, startLocation);
                mTokens.add(token);
                mPositions.add(startLocation);
                startLocation += token.length();
            }
        }
        isValid = true;
    }
}
