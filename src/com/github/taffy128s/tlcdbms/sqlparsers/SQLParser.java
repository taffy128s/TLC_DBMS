package com.github.taffy128s.tlcdbms.sqlparsers;

import com.github.taffy128s.tlcdbms.CommandType;
import com.github.taffy128s.tlcdbms.DataChecker;
import com.github.taffy128s.tlcdbms.DataType;
import com.github.taffy128s.tlcdbms.DataTypeIdentifier;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * SQL parser.
 */
public class SQLParser {
    private String mCommand;
    private ArrayList<String> mTokens;
    private ArrayList<Integer> mPositions;
    private boolean mIsValid;
    private boolean mTokenEnded;
    private int mIndex;

    /**
     * Constructor.
     */
    public SQLParser() {

    }

    /**
     * Parse given command and return a SQLParseResult.
     *
     * @param command command to parse
     * @return parse result, null if failed
     */
    public SQLParseResult parse(String command) {
        mCommand = command;
        mTokens = new ArrayList<>();
        mPositions = new ArrayList<>();
        mIndex = -1;
        mTokenEnded = false;
        splitTokens();
        if (!mIsValid) {
            return null;
        }
        return parseCommand();
    }

    /**
     * Parse Command (CREATE or INSERT or SELECT) and call
     * corresponding function for further processing.
     *
     * @return parse result, null if failed
     */
    private SQLParseResult parseCommand() {
        String command = nextToken(true);
        if (mTokens.size() == 0) return null;
        if (command.equalsIgnoreCase("create")) {
            return parseCreate();
        } else if (command.equalsIgnoreCase("insert")) {
            return parseInsert();
        } else if (command.equalsIgnoreCase("quit")) {
            return parseQuit();
        } else if (command.equalsIgnoreCase("exit")) {
            return parseExit();
        } else {
            printErrorMessage("Unexpected command " + command);
            return null;
        }
    }

    /**
     * Parse CREATE.
     *
     * @return parse result, null if failed
     */
    private SQLParseResult parseCreate() {
        SQLParseResult result = new SQLParseResult();
        result.setCommandType(CommandType.CREATE);
        if (!checkTokenIgnoreCase("table", true)) {
            printErrorMessage("Expect keyword TABLE");
            return null;
        }
        String tablename = getTableName();
        if (tablename == null) {
            return null;
        }
        result.setTablename(tablename);
        if (!checkTokenIgnoreCase("(", true)) {
            printErrorMessage("Left parenthesis '(' expected after table name");
            return null;
        }
        ArrayList<String> attributeNames = new ArrayList<>();
        ArrayList<DataType> attributeTypes = new ArrayList<>();
        int index = 0;
        boolean hasComma = false;
        HashSet<String> attrNameSet = new HashSet<>();
        while (true) {
            if (nextToken(false).equalsIgnoreCase(")") && !hasComma) {
                break;
            }
            String attributeName = getAttributeName();
            if (attributeName == null) {
                return null;
            }
            if (attrNameSet.contains(attributeName)) {
                printErrorMessage("Duplicated attribute name");
                return null;
            }
            attrNameSet.add(attributeName);
            attributeNames.add(attributeName);
            String attributeType = getAttributeType();
            if (attributeType == null) {
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
                    return null;
                } else {
                    result.setPrimaryKeyIndex(index);
                }
            }
            if (!checkTokenIgnoreCase(",", false)) {
                break;
            }
            checkTokenIgnoreCase(",", true);
            hasComma = true;
            ++index;
        }
        if (!checkTokenIgnoreCase(")", true)) {
            printErrorMessage("Right parenthesis ')' expected after attribute definition.");
            return null;
        }
        if (!isEnded()) {
            System.out.println("Unexpected strings at end of line.");
            return null;
        }
        if (attributeNames.isEmpty()) {
            printErrorMessage("No attributes specified for this new table.", 2, mTokens.get(2).length());
            return null;
        }
        result.setAttributeNames(attributeNames);
        result.setAttributeTypes(attributeTypes);
        return result;
    }

    /**
     * Parse INSERT.
     *
     * @return parse result, null if failed
     */
    private SQLParseResult parseInsert() {
        SQLParseResult result = new SQLParseResult();
        result.setCommandType(CommandType.INSERT);
        if (!checkTokenIgnoreCase("into", true)) {
            printErrorMessage("Expect keyword INTO");
            return null;
        }
        String tablename = getTableName();
        if (tablename == null) {
            return null;
        }
        result.setTablename(tablename);
        ArrayList<String> updateOrder = null;
        HashSet<String> attrNameSet = new HashSet<>();
        if (checkTokenIgnoreCase("(", false)) {
            checkTokenIgnoreCase("(", true);
            result.setCustomOrder(true);
            updateOrder = new ArrayList<>();
            while (true) {
                String attrName = getAttributeName();
                if (attrName == null) {
                    return null;
                }
                if (attrNameSet.contains(attrName)) {
                    printErrorMessage("Duplicated attribute name");
                    return null;
                }
                attrNameSet.add(attrName);
                updateOrder.add(attrName);
                if (!checkTokenIgnoreCase(",", false)) {
                    break;
                }
                checkTokenIgnoreCase(",", true);
            }
            if (!checkTokenIgnoreCase(")", true)) {
                printErrorMessage("Right parenthesis ')' expected after attribute definition.");
                return null;
            }
            result.setUpdateOrder(updateOrder);
        } else {
            result.setCustomOrder(false);
        }
        if (!checkTokenIgnoreCase("values", true)) {
            printErrorMessage("Expect keyword VALUES");
            return null;
        }
        if (!checkTokenIgnoreCase("(", true)) {
            printErrorMessage("Left parenthesis '(' expected after table name");
            return null;
        }
        ArrayList<String> blocks = new ArrayList<>();
        while (true) {
            String block = getBlock();
            if (block == null) {
                return null;
            }
            blocks.add(block);
            if (!checkTokenIgnoreCase(",", false)) {
                break;
            }
            checkTokenIgnoreCase(",", true);
        }
        if (!checkTokenIgnoreCase(")", true)) {
            printErrorMessage("Right parenthesis ')' expected after attribute definition.");
            return null;
        }
        if (!isEnded()) {
            System.out.println("Unexpected strings at end of line.");
            return null;
        }
        if (blocks.isEmpty()) {
            printErrorMessage("Empty tuple to insert to table.", 2, mTokens.get(2).length());
            return null;
        }
        if (updateOrder != null && blocks.size() != updateOrder.size()) {
            System.out.println("Data numbers not matched.");
            return null;
        }
        result.setBlocks(blocks);
        return result;
    }

    private SQLParseResult parseQuit() {
        if (!isEnded()) {
            System.out.println("Unexpected strings at end of line.");
            return null;
        }
        SQLParseResult result = new SQLParseResult();
        result.setCommandType(CommandType.QUIT);
        return result;
    }

    private SQLParseResult parseExit() {
        if (!isEnded()) {
            System.out.println("Unexpected strings at end of line.");
            return null;
        }
        SQLParseResult result = new SQLParseResult();
        result.setCommandType(CommandType.EXIT);
        return result;
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
            mTokenEnded = true;
            return "";
        }
    }

    /**
     * Get position of index-th token in mCommand string.
     *
     * @param index index to get
     * @return position(string index) start from 0
     */
    private int getPosition(int index) {
        if (!mTokenEnded) {
            return mPositions.get(index);
        } else {
            return mPositions.get(index) + mTokens.get(index).length() + 1;
        }
    }

    /**
     * Check whether there is still token not used.
     * <br>** WILL INCREASE TOKEN INDEX **
     * <p>
     * Case 1 return true:<br>
     *     {";"} // remaining only a semicolon
     * <p>
     * Case 2 return true:<br>
     *     {} // already empty
     *
     * @return true if empty, false if not
     */
    private boolean isEnded() {
        String token = nextToken(true);
        return (token.equals(";") || token.equals("")) && nextToken(true).equals("");
    }

    /**
     * Get table name from next token.
     * <br>** WILL INCREASE TOKEN INDEX **
     *
     * @return a string with name if valid, null if invalid
     */
    private String getTableName() {
        String name = nextToken(true);
        if (!name.matches("[a-zA-Z_]+")) {
            printErrorMessage("Invalid table name " + name);
            return null;
        } else if (SQLKeyWords.isSQLKeyword(name)) {
            printErrorMessage("Invalid table name " + name);
            return null;
        } else {
            return name;
        }
    }

    /**
     * Get attribute name from next token.
     * <br>** WILL INCREASE TOKEN INDEX **
     *
     * @return a string with name if valid, null if invalid
     */
    private String getAttributeName() {
        String name = nextToken(true);
        if (!name.matches("[a-zA-Z_]+")) {
            printErrorMessage("Invalid attribute name " + name);
            return null;
        } else if (SQLKeyWords.isSQLKeyword(name)) {
            printErrorMessage("Invalid attribute name " + name);
            return null;
        } else {
            return name;
        }
    }

    /**
     * Get attribute type from next token.
     * <br>** WILL INCREASE TOKEN INDEX **
     * <p>
     *     Return examples:<br>
     *     <code>null</code><br>
     *     <code>"INT"</code><br>
     *     <code>"INT PRIMARY"</code><br>
     *     <code>"VARCHAR 20"</code><br>
     *     <code>"VARCHAR 20 PRIMARY"</code><br>
     *
     * @return a string of attribute type, null if invalid
     */
    private String getAttributeType() {
        String type = nextToken(true);
        if (type.equalsIgnoreCase("int")) {
            String primaryResult = checkPrimaryKey();
            if (primaryResult == null) {
                return null;
            }
            switch (primaryResult) {
                case "PRIMARY":
                    return "INT PRIMARY";
                default:
                    return "INT";
            }
        } else if (type.equalsIgnoreCase("varchar")) {
            if (!checkTokenIgnoreCase("(", true)) {
                printErrorMessage("Left parenthesis '(' expected after VARCHAR");
                return null;
            }
            String limit = nextToken(true);
            if (!DataChecker.isValidVarCharLimitation(limit)) {
                printErrorMessage("Invalid limitation");
                return null;
            }
            if (!checkTokenIgnoreCase(")", true)) {
                printErrorMessage("Right parenthesis ')' expected at end of VARCHAR definition.");
                return null;
            }
            String primaryResult = checkPrimaryKey();
            if (primaryResult == null) {
                return null;
            }
            switch (primaryResult) {
                case "PRIMARY":
                    return "VARCHAR " + limit + " PRIMARY";
                default:
                    return "VARCHAR " + limit;
            }
        } else {
            printErrorMessage("Invalid data type " + type);
            return null;
        }
    }

    /**
     * Get a data block.
     *
     * @return a string of data block, null if failed
     */
    private String getBlock() {
        String block = nextToken(true);
        if (DataChecker.isValidInteger(block)) {
            return block;
        } else if (DataChecker.isValidQuotedVarChar(block)) {
            return block;
        } else {
            printErrorMessage("Invalid data format");
            return null;
        }
    }

    /**
     * Check next token with input string (case insensitive).
     *
     * @param expected expect string, string to be compared
     * @param increase true to increase index
     * @return true if matched, false if not
     */
    private boolean checkTokenIgnoreCase(String expected, boolean increase) {
        return nextToken(increase).equalsIgnoreCase(expected);
    }

    /**
     * Check PRIMARY KEY keywords.
     * Used in getAttributeType().
     *
     * @return "PRIMARY" if primary key, "" if no primary key, null if syntax error
     */
    private String checkPrimaryKey() {
        if (checkTokenIgnoreCase("primary", false)) {
            checkTokenIgnoreCase("primary", true);
            if (checkTokenIgnoreCase("key", true)) {
                return "PRIMARY";
            } else {
                System.out.println("KEY keyword expected after PRIMARY");
                return null;
            }
        } else {
            return "";
        }
    }

    /**
     * Print error message. Underline will be drawn under the mIndex-th tokens.
     *
     * @param message error message to show
     */
    private void printErrorMessage(String message) {
        System.out.println(mCommand);
        int underlineLength = (mTokenEnded) ? 3 : mTokens.get(mIndex).length();
        printUnderLine(getPosition(mIndex), underlineLength);
        System.out.println(message);
    }

    /**
     * Print error message. Underline will be drawn under the index-th tokens
     * with length underlineLength specified from parameters.
     *
     * @param message error message to show
     * @param index set token index for start position
     * @param underlineLength underline length
     */
    private void printErrorMessage(String message, int index, int underlineLength) {
        System.out.println(mCommand);
        printUnderLine(getPosition(index), underlineLength);
        System.out.println(message);
    }

    /**
     * Draw underline ^~~~~~~~.
     *
     * @param startPosition start position
     * @param length length, note that length("^~~") == 3
     */
    private void printUnderLine(int startPosition, int length) {
        for (int i = 0; i < startPosition; ++i) {
            System.out.print(" ");
        }
        System.out.print("^");
        for (int i = 0; i < length - 1; ++i) {
            System.out.print("~");
        }
        System.out.println("");
    }

    /**
     * Split tokens.
     */
    private void splitTokens() {
        mCommand = mCommand.replaceAll("\n", " ");
        mCommand = mCommand.replaceAll("\t", "    ");
        mCommand = mCommand.replaceAll("\r", "");
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
            mIsValid = false;
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
        mIsValid = true;
    }
}
