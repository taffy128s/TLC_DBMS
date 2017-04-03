package com.github.taffy128s.tlcdbms.sqlparsers;

import com.github.taffy128s.tlcdbms.*;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Stack;

/**
 * SQL parser for this project.
 */
public class SQLParser {
    private String mCommand;
    private ArrayList<String> mTokens;
    private ArrayList<Integer> mPositions;
    private boolean mIsValid;
    private boolean mTokenEnded;
    private int mIndex;

    /**
     * SQL Block. Return type of getBlock().
     */
    private class SQLBlock {
        private boolean mValid;
        private String mData;

        /**
         * Initialize.
         *
         * @param data data content, null-able.
         * @param valid true if this block is valid, false otherwise.
         */
        SQLBlock(String data, boolean valid) {
            mData = data;
            mValid = valid;
        }

        /**
         * Data getter.
         *
         * @return data.
         */
        String getData() {
            return mData;
        }

        /**
         * Check validation of this block.
         *
         * @return true if valid, false otherwise.
         */
        boolean isValid() {
            return mValid;
        }
    }

    /**
     * Constructor.
     */
    public SQLParser() {

    }

    /**
     * Parse given command and return a SQLParseResult.
     *
     * @param command command to parse.
     * @return parse result, null if failed.
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
     * @return parse result, null if failed.
     */
    private SQLParseResult parseCommand() {
        String command = nextToken(true);
        if (mTokens.size() == 0) return null;
        if (command.equalsIgnoreCase("create")) {
            return parseCreate();
        } else if (command.equalsIgnoreCase("insert")) {
            return parseInsert();
        } else if (command.equalsIgnoreCase("drop")) {
            return parseDrop();
        } else if (command.equalsIgnoreCase("show")) {
            return parseShow();
        } else if (command.equalsIgnoreCase("desc")) {
            return parseDesc();
        } else if (command.equalsIgnoreCase("load")) {
            return parseLoad();
        } else if (command.equalsIgnoreCase("quit")) {
            return parseQuit();
        } else if (command.equalsIgnoreCase("exit")) {
            return parseExit();
        } else if (command.equalsIgnoreCase("select")) {
            return parseSelect();
        } else {
            printErrorMessage("Unexpected command '" + command + "'.");
            return null;
        }
    }

    /**
     * Parse SELECT.
     *
     * @return parse result, null if failed.
     */
    private SQLParseResult parseSelect() {
        SQLParseResult result = new SQLParseResult();
        result.setCommandType(CommandType.SELECT);
        ArrayList<String> targets = new ArrayList<>();
        if (checkTokenIgnoreCase("sum", false)) {
            nextToken(true);
            if (!checkTokenIgnoreCase("(", true)) {
                printErrorMessage("Missing left parenthesis.");
                return null;
            }
            String attributeName = getTargetNameWithPossibleDot();
            if (attributeName == null) {
                return null;
            }
            if (!checkTokenIgnoreCase(")", true)) {
                printErrorMessage("Missing right parenthesis.");
                return null;
            }
            result.setQueryType(QueryType.SUM);
            targets.add(attributeName);
        } else if (checkTokenIgnoreCase("count", false)) {
            nextToken(true);
            if (!checkTokenIgnoreCase("(", true)) {
                printErrorMessage("Missing left parenthesis");
                return null;
            }
            String attributeName;
            if (checkTokenIgnoreCase("*", false)) {
                nextToken(true);
                attributeName = "*";
            } else {
                attributeName = getTargetNameWithPossibleDot();
                if (attributeName == null) {
                    return null;
                }
            }
            if (!checkTokenIgnoreCase(")", true)) {
                printErrorMessage("Missing right parenthesis");
                return null;
            }
            result.setQueryType(QueryType.COUNT);
            targets.add(attributeName);
        } else if (checkTokenIgnoreCase("*", false)) {
            nextToken(true);
            result.setQueryType(QueryType.NORMAL);
            targets.add("*");
        } else {
            String attrName = getTargetNameWithPossibleDot();
            if (attrName == null) {
                return null;
            }
            targets.add(attrName);
            while (checkTokenIgnoreCase(",", false)) {
                nextToken(true);
                attrName = getTargetNameWithPossibleDot();
                if (attrName == null) {
                    return null;
                }
                targets.add(attrName);
            }
            result.setQueryType(QueryType.NORMAL);
        }
        if (!checkTokenIgnoreCase("from", true)) {
            printErrorMessage("Expect keyword FROM after query type.");
            return null;
        }
        ArrayList<String> tableNameList = new ArrayList<>();
        HashMap<String, String> aliasMap = new HashMap<>();
        if (!getTableNameAndAlias(tableNameList, aliasMap)) {
            return null;
        }
        while (checkTokenIgnoreCase(",", false)) {
            nextToken(true);
            if (!getTableNameAndAlias(tableNameList, aliasMap)) {
                return null;
            }
        }
        for (int i = 0; i < targets.size(); i++) {
            targets.set(i, handlePrefixUsingMap(targets.get(i), tableNameList, aliasMap));
            if (targets.get(i) == null) {
                return null;
            }
        }
        result.setTargets(targets);
        result.setTablenames(tableNameList);
        result.setTableAliases(aliasMap);
        if (checkTokenIgnoreCase("where", false)) {
            nextToken(true);
            ArrayList<Condition> conditions = new ArrayList<>();
            Stack<Condition> stack = new Stack<>();
            boolean operandExpect = true;
            boolean emptyParenthesisBody = false;
            int parenthesisCounter = 0;
            do {
                if (checkTokenIgnoreCase("ORDER", false) ||
                        checkTokenIgnoreCase("LIMIT", false)) {
                    break;
                }
                if (checkTokenIgnoreCase("(", false)) {
                    operandExpect = true;
                    emptyParenthesisBody = true;
                    nextToken(true);
                    ++parenthesisCounter;
                    stack.push(null);

                } else if (checkTokenIgnoreCase(")", false)) {
                    operandExpect = false;
                    nextToken(true);
                    --parenthesisCounter;
                    if (parenthesisCounter < 0) {
                        printErrorMessage("Missing corresponding left parenthesis '('.");
                        return null;
                    }
                    if (emptyParenthesisBody) {
                        printErrorMessage("Empty Body in this parenthesis pair.");
                        return null;
                    }
                    while (stack.peek() != null) {
                        conditions.add(stack.pop());
                    }
                    stack.pop();
                    emptyParenthesisBody = false;
                } else if (operandExpect) {
                    operandExpect = false;
                    emptyParenthesisBody = false;
                    Condition operand = getCondition(tableNameList, aliasMap);
                    if (operand == null) {
                        return null;
                    }
                    conditions.add(operand);
                } else {
                    operandExpect = true;
                    emptyParenthesisBody = false;
                    String operator = nextToken(true);
                    if (!operator.equalsIgnoreCase("and") && !operator.equalsIgnoreCase("or")) {
                        printErrorMessage("Invalid operator. AND / OR expected.");
                        return null;
                    }
                    while (stackNeedPop(operator, stack)) {
                        conditions.add(stack.pop());
                    }
                    Condition opCon = new Condition(null, null, null, null, null, null, toBinaryOperator(operator));
                    stack.push(opCon);
                }
            } while (!isEnded(false));
            if (parenthesisCounter != 0) {
                System.out.println("Parenthesis mismatched in WHERE clause.");
                return null;
            }
            while (!stack.empty()) {
                conditions.add(stack.pop());
            }
            int counterOfOperator = 0;
            int counterOfOperand = 0;
            for (Condition condition : conditions) {
                if (condition.getOperator() == BinaryOperator.AND || condition.getOperator() == BinaryOperator.OR) {
                    ++counterOfOperator;
                } else {
                    ++counterOfOperand;
                }
            }
            if (counterOfOperand != 0 && counterOfOperator != 0 && counterOfOperand != counterOfOperator + 1) {
                System.out.println("Invalid WHERE clauses. Check whether there are something missing.");
                return null;
            }
            if (conditions.isEmpty()) {
                result.setConditions(null);
            } else {
                result.setConditions(conditions);
            }
        }
        if (checkTokenIgnoreCase("ORDER", false)) {
            nextToken(true);
            if (!checkTokenIgnoreCase("BY", true)) {
                printErrorMessage("Expect keyword BY after ORDER");
            }
            String orderTarget = getTargetNameWithPossibleDot();
            if (orderTarget == null) {
                return null;
            }
            if (orderTarget.contains("*")) {
                printErrorMessage("Invalid order target. Cannot include '*'.");
                return null;
            }
            ArrayList<String> attributeNames = new ArrayList<>();
            attributeNames.add(orderTarget);
            result.setAttributeNames(attributeNames);
            result.setShowSortType(SortingType.ASCENDING);
            if (checkTokenIgnoreCase("ASC", false)) {
                nextToken(true);
            } else if (checkTokenIgnoreCase("DESC", false)) {
                nextToken(true);
                result.setShowSortType(SortingType.DESCENDING);
            }
        }
        if (checkTokenIgnoreCase("LIMIT", false)) {
            nextToken(true);
            String limitation = nextToken(true);
            if (!DataChecker.isValidInteger(limitation)) {
                printErrorMessage("Invalid limitation.");
                return null;
            }
            int limit = Integer.parseInt(limitation);
            if (limit <= 0) {
                printErrorMessage("Invalid limitation.");
                return null;
            }
            result.setShowRowLimitation(limit);
        }
        if (!isEnded()) {
            System.out.println("Unexpected tokens at end of line");
            return null;
        }
        return result;
    }

    private boolean stackNeedPop(String operator, Stack<Condition> stack) {
        if (stack.isEmpty() || stack.peek() == null) {
            return false;
        }
        String top;
        if (stack.peek().getOperator() == BinaryOperator.AND) {
            top = "AND";
        } else {
            top = "OR";
        }
        return operatorToNum(top) >= operatorToNum(operator);
    }

    private int operatorToNum(String operator) {
        if (operator == null) {
            return 0;
        }
        switch (operator) {
            case "AND":
                return 2;
            case "OR":
                return 1;
            default:
                return -1;
        }
    }

    /**
     * Get condition.
     *
     * @param tableNameList: list used for checking the presence of attributes.
     * @param aliasMap: map used for replacing the prefixes of attributes.
     * @return condition, null if failed.
     *
     * Expected condition format:
     * [valid attribute] [valid operator] [valid attribute]
     */
    private Condition getCondition(ArrayList<String> tableNameList, HashMap<String, String> aliasMap) {
        String leftOperand, rightOperand, operator;
        leftOperand = nextToken(true);
        operator = nextToken(true);
        rightOperand = nextToken(true);
        if (!isValidOp(operator)) {
            System.out.println("Invalid statement: "
                    + leftOperand + " " + operator + " " + rightOperand);
            return null;
        }
        if (isCompareOp(operator)) {
            if (DataChecker.isValidQuotedVarChar(leftOperand)
                    || DataChecker.isValidQuotedVarChar(rightOperand)
                    || DataChecker.isStringNull(leftOperand)
                    || DataChecker.isStringNull(rightOperand)) {
                System.out.println("Invalid statement: "
                        + leftOperand + " " + operator + " " + rightOperand);
                return null;
            }
        }
        if (DataChecker.isValidInteger(leftOperand) || DataChecker.isValidQuotedVarChar(leftOperand) || DataChecker.isStringNull(leftOperand)) {
            if (DataChecker.isValidInteger(rightOperand) || DataChecker.isValidQuotedVarChar(rightOperand) || DataChecker.isStringNull(rightOperand)) {
                Condition retCon = new Condition(leftOperand, null, null, rightOperand, null, null, toBinaryOperator(operator));
                return retCon;
            } else if (rightOperand.matches("[a-zA-Z_][0-9a-zA-Z_]*")
                    || rightOperand.matches("[a-zA-Z_][0-9a-zA-Z_]*[.][a-zA-Z_][0-9a-zA-Z_]*")) {
                rightOperand = handlePrefixUsingMap(rightOperand, tableNameList, aliasMap);
                if (rightOperand == null) {
                    return null;
                }
                String[] splits = rightOperand.split("\\.");
                Condition retCon;
                if (splits.length == 1) {
                    retCon = new Condition(leftOperand, null, null, null, null, rightOperand, toBinaryOperator(operator));
                    return retCon;
                } else {
                    retCon = new Condition(leftOperand, null, null, null, splits[0], splits[1], toBinaryOperator(operator));
                    return retCon;
                }
            } else {
                System.out.println("Invalid statement: "
                        + leftOperand + " " + operator + " " + rightOperand);
                return null;
            }
        } else if (leftOperand.matches("[a-zA-Z_][0-9a-zA-Z_]*")
                || leftOperand.matches("[a-zA-Z_][0-9a-zA-Z_]*[.][a-zA-Z_][0-9a-zA-Z_]*")) {
            if (DataChecker.isValidInteger(rightOperand) || DataChecker.isValidQuotedVarChar(rightOperand) || DataChecker.isStringNull(rightOperand)) {
                leftOperand = handlePrefixUsingMap(leftOperand, tableNameList, aliasMap);
                if (leftOperand == null) {
                    return null;
                }
                String[] splits = leftOperand.split("\\.");
                Condition retCon;
                if (splits.length == 1) {
                    retCon = new Condition(null, null, leftOperand, rightOperand, null, null, toBinaryOperator(operator));
                    return retCon;
                } else {
                    retCon = new Condition(null, splits[0], splits[1], rightOperand, null, null, toBinaryOperator(operator));
                    return retCon;
                }
            } else if (rightOperand.matches("[a-zA-Z_][0-9a-zA-Z_]*")
                    || rightOperand.matches("[a-zA-Z_][0-9a-zA-Z_]*[.][a-zA-Z_][0-9a-zA-Z_]*")) {
                leftOperand = handlePrefixUsingMap(leftOperand, tableNameList, aliasMap);
                if (leftOperand == null) {
                    return null;
                }
                rightOperand = handlePrefixUsingMap(rightOperand, tableNameList, aliasMap);
                if (rightOperand == null) {
                    return null;
                }
                String[] splitsLeft = leftOperand.split("\\.");
                String[] splitsRight = rightOperand.split("\\.");
                Condition retCon;
                if (splitsLeft.length == 1) {
                    if (splitsRight.length == 1) {
                        retCon = new Condition(null, null, leftOperand, null, null, rightOperand, toBinaryOperator(operator));
                        return retCon;
                    } else {
                        retCon = new Condition(null, null, leftOperand, null, splitsRight[0], splitsRight[1], toBinaryOperator(operator));
                        return retCon;
                    }
                } else {
                    if (splitsRight.length == 1) {
                        retCon = new Condition(null, splitsLeft[0], splitsLeft[1], null, null, rightOperand, toBinaryOperator(operator));
                        return retCon;
                    } else {
                        retCon = new Condition(null, splitsLeft[0], splitsLeft[1], null, splitsRight[0], splitsRight[1], toBinaryOperator(operator));
                        return retCon;
                    }
                }
            } else {
                System.out.println("Invalid statement: "
                        + leftOperand + " " + operator + " " + rightOperand);
                return null;
            }
        } else {
            System.out.println("Invalid statement: "
                    + leftOperand + " " + operator + " " + rightOperand);
            return null;
        }
    }

    /**
     * Transform String operator to BinaryOperator.
     *
     * @param input: string to transform.
     * @return BinaryOperator.
     */
    private BinaryOperator toBinaryOperator(String input) {
        if (input.equalsIgnoreCase(">=")) {
            return BinaryOperator.GREATER_EQUAL;
        } else if (input.equalsIgnoreCase(">")) {
            return BinaryOperator.GREATER_THAN;
        } else if (input.equalsIgnoreCase("<")) {
            return BinaryOperator.LESS_THAN;
        } else if (input.equalsIgnoreCase("<=")) {
            return BinaryOperator.LESS_EQUAL;
        } else if (input.equalsIgnoreCase("<>")) {
            return BinaryOperator.NOT_EQUAL;
        } else if (input.equalsIgnoreCase("=")) {
            return BinaryOperator.EQUAL;
        } else if (input.equalsIgnoreCase("AND")) {
            return BinaryOperator.AND;
        } else {
            return BinaryOperator.OR;
        }
    }

    /**
     * Check if the given String is a comparison operator or not.
     *
     * @param input: String to check.
     * @return true if it is a comparison operator, false if not.
     */
    private boolean isCompareOp(String input) {
        if (input.equals(">") || input.equals(">=") || input.equals("<") || input.equals("<=")) {
            return true;
        }
        return false;
    }

    /**
     * Check if the given String is a valid operator or not.
     *
     * @param input: String to check.
     * @return true if it is valid, false if not.
     */
    private boolean isValidOp(String input) {
        if (input.equals("<>") || input.equals("=") || input.equals(">")
                || input.equals(">=") || input.equals("<") || input.equals("<=")) {
            return true;
        }
        return false;
    }

    /**
     * Replace the prefix of input if it uses an alias.
     *
     * @param input: String to modify.
     * @param tableNameList: table list available now.
     * @param aliasMap: provide an alias-tableName mapping.
     * @return string modified.
     */
    private String handlePrefixUsingMap(String input, ArrayList<String> tableNameList, HashMap<String, String> aliasMap) {
        String[] splits = input.split("\\.");
        if (splits.length == 1) {
            return input;
        } else if (splits.length == 2) {
            if (aliasMap.containsKey(splits[0])) {
                return splits[0] + "." + splits[1];
            } else {
                if (tableNameList.contains(splits[0])) {
                    return input;
                } else {
                    System.out.println("Prefix is not in table list: " + input);
                    return null;
                }
            }
        } else {
            System.out.println("Multiple dots near: " + input);
            return null;
        }
    }

    /**
     * Get an attribute name with a possible dot.
     *
     * @return a valid target name.
     */
    private String getTargetNameWithPossibleDot() {
        String name = nextToken(true);
        if (!name.matches("[a-zA-Z_][0-9a-zA-Z_]*")
                && !name.matches("[a-zA-Z_][0-9a-zA-Z_]*[.][a-zA-Z_][0-9a-zA-Z_]*")
                && !name.matches("[a-zA-Z_][0-9a-zA-Z_]*[.][*]")) {
            printErrorMessage("Invalid target name '" + name + "'.");
            return null;
        } else if (SQLKeyWords.isSQLKeyword(name)) {
            printErrorMessage("Invalid target name '" + name + "'.");
            return null;
        } else {
            return name;
        }
    }

    /**
     * Get table name and its alias, and add them into list and map.
     *
     * @param tableNameList: list to add into.
     * @param aliasMap: map to store the relations.
     * @return true if succeed, false if fail.
     *
     * Expected format:
     * [valid table name] [a|As|S] [valid alias]
     */
    private boolean getTableNameAndAlias(ArrayList<String> tableNameList, HashMap<String, String> aliasMap) {
        String tableName, alias;
        tableName = getTableName();
        if (tableName == null) {
            return false;
        }
        tableNameList.add(tableName);
        if (checkTokenIgnoreCase("as", false)) {
            nextToken(true);
            alias = getAliasName();
            if (alias == null) {
                return false;
            }
            if (aliasMap.containsKey(alias)) {
                printErrorMessage("Duplicated alias table name.");
                return false;
            }
            aliasMap.put(alias, tableName);
        } else {
            if (aliasMap.containsKey(tableName)) {
                printErrorMessage("Duplicated table name.");
                return false;
            }
            aliasMap.put(tableName, tableName);
        }
        return true;
    }

    /**
     * Get alias.
     *
     * @return alias if succeed, null if fail.
     */
    private String getAliasName() {
        String name = nextToken(true);
        if (!name.matches("[a-zA-Z_][0-9a-zA-Z_]*")) {
            printErrorMessage("Invalid alias '" + name + "'.");
            return null;
        } else if (SQLKeyWords.isSQLKeyword(name)) {
            printErrorMessage("Invalid alias '" + name + "'.");
            return null;
        } else {
            return name;
        }
    }

    /**
     * Parse CREATE.
     *
     * @return parse result, null if failed.
     */
    private SQLParseResult parseCreate() {
        SQLParseResult result = new SQLParseResult();
        result.setCommandType(CommandType.CREATE);
        if (!checkTokenIgnoreCase("table", true)) {
            printErrorMessage("Expect keyword TABLE after CREATE.");
            return null;
        }
        String tablename = getTableName();
        if (tablename == null) {
            return null;
        }
        result.setTablename(tablename);
        if (!checkTokenIgnoreCase("(", true)) {
            printErrorMessage("Left parenthesis '(' expected after table name.");
            return null;
        }
        ArrayList<String> attributeNames = new ArrayList<>();
        ArrayList<DataType> attributeTypes = new ArrayList<>();
        ArrayList<TableStructure> attributeIndices = new ArrayList<>();
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
                printErrorMessage("Duplicate attribute name.");
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
                            "Multiple Primary Key.",
                            mIndex - 1,
                            11);
                    return null;
                } else {
                    result.setPrimaryKeyIndex(index);
                    attributeIndices.add(new TableStructure(index, TableStructType.BPLUSTREE));
                }
            }
            if (checkTokenIgnoreCase("key", false)) {
                checkTokenIgnoreCase("key", true);
                if (checkTokenIgnoreCase("bplustree", false)) {
                    checkTokenIgnoreCase("bplustree", true);
                    attributeIndices.add(new TableStructure(index, TableStructType.BPLUSTREE));
                } else if (checkTokenIgnoreCase("btree", false)) {
                    checkTokenIgnoreCase("btree", true);
                    attributeIndices.add(new TableStructure(index, TableStructType.BTREE));
                } else if (checkTokenIgnoreCase("rbtree", false)) {
                    checkTokenIgnoreCase("rbtree", true);
                    attributeIndices.add(new TableStructure(index, TableStructType.RBTREE));
                } else if (checkTokenIgnoreCase("hash", false)) {
                    checkTokenIgnoreCase("hash", true);
                    attributeIndices.add(new TableStructure(index, TableStructType.HASH));
                } else {
                    attributeIndices.add(new TableStructure(index, TableStructType.BPLUSTREE));
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
        if (attributeIndices.isEmpty()) {
            attributeIndices = null;
        }
        result.setAttributeNames(attributeNames);
        result.setAttributeTypes(attributeTypes);
        result.setAttributeIndices(attributeIndices);
        return result;
    }

    /**
     * Parse INSERT.
     *
     * @return parse result, null if failed.
     */
    private SQLParseResult parseInsert() {
        SQLParseResult result = new SQLParseResult();
        result.setCommandType(CommandType.INSERT);
        if (!checkTokenIgnoreCase("into", true)) {
            printErrorMessage("Expect keyword INTO after INSERT.");
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
                    printErrorMessage("Duplicate attribute name.");
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
            printErrorMessage("Expect keyword VALUES.");
            return null;
        }
        if (!checkTokenIgnoreCase("(", true)) {
            printErrorMessage("Left parenthesis '(' expected after table name.");
            return null;
        }
        ArrayList<String> blocks = new ArrayList<>();
        while (true) {
            SQLBlock block = getBlock();
            if (!block.isValid()) {
                return null;
            }
            blocks.add(block.getData());
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
            System.out.println("Specified: " + updateOrder.size() + ".");
            System.out.println("Given: " + blocks.size() + ".");
            return null;
        }
        result.setBlocks(blocks);
        return result;
    }

    /**
     * Parse DROP.
     *
     * @return parse result, null if failed.
     */
    private SQLParseResult parseDrop() {
        if (checkTokenIgnoreCase("table", false)) {
            checkTokenIgnoreCase("table", true);
            ArrayList<String> tablenames = new ArrayList<>();
            while (true) {
                String tablename = getTableName();
                if (tablename == null) {
                    return null;
                }
                tablenames.add(tablename);
                if (checkTokenIgnoreCase(",", false)) {
                    checkTokenIgnoreCase(",", true);
                } else {
                    break;
                }
            }
            if (!isEnded()) {
                printErrorMessage("Unexpected string at the end of line.");
                return null;
            }
            if (tablenames.isEmpty()) {
                printErrorMessage("No tablenames specified.");
                return null;
            }
            SQLParseResult result = new SQLParseResult();
            result.setCommandType(CommandType.DROP);
            result.setTablenames(tablenames);
            return result;
        } else if (checkTokenIgnoreCase("all", false)) {
            checkTokenIgnoreCase("all", true);
            if (!checkTokenIgnoreCase("tables", true)) {
                printErrorMessage("Expect keyword TABLES after DROP ALL.");
                return null;
            }
            if (!isEnded()) {
                printErrorMessage("Unexpected string at the end of line.");
                return null;
            }
            SQLParseResult result = new SQLParseResult();
            result.setCommandType(CommandType.DROP);
            result.setTablenames(new ArrayList<>());
            result.getTablenames().add(null);
            return result;
        } else {
            nextToken(true);
            printErrorMessage("Keyword TABLE or ALL expected after DROP.");
            return null;
        }
    }

    /**
     * Parse SHOW.
     *
     * @return parse result, null if failed.
     */
    private SQLParseResult parseShow() {
        if (checkTokenIgnoreCase("table", false)) {
            checkTokenIgnoreCase("table", true);
            SQLParseResult result = new SQLParseResult();
            if (checkTokenIgnoreCase("full", false)) {
                checkTokenIgnoreCase("full", true);
                result.setShowFullInfo(true);
            } else {
                result.setShowFullInfo(false);
            }
            String tablename = getTableName();
            if (tablename == null) {
                return null;
            }
            ArrayList<String> attributeNames = new ArrayList<>();
            if (checkTokenIgnoreCase("order", false)) {
                checkTokenIgnoreCase("order", true);
                if (!checkTokenIgnoreCase("by", true)) {
                    printErrorMessage("Expect keyword BY after ORDER.");
                    return null;
                }
                String attributeName = getAttributeName();
                if (attributeName == null) {
                    return null;
                }
                attributeNames.add(attributeName);
                if (checkTokenIgnoreCase("asc", false)) {
                    checkTokenIgnoreCase("asc", true);
                    result.setShowSortType(SortingType.ASCENDING);
                } else if (checkTokenIgnoreCase("desc", false)) {
                    checkTokenIgnoreCase("desc", true);
                    result.setShowSortType(SortingType.DESCENDING);
                } else {
                    result.setShowSortType(SortingType.ASCENDING);
                }
                result.setAttributeNames(attributeNames);
            }
            int limitation = -1;
            if (checkTokenIgnoreCase("limit", false)) {
                checkTokenIgnoreCase("limit", true);
                String limitString = nextToken(true);
                if (DataChecker.isValidInteger(limitString)) {
                    limitation = Integer.parseInt(limitString);
                    if (limitation <= 0) {
                        printErrorMessage("Invalid limitation (needs > 0).");
                        return null;
                    }
                } else {
                    printErrorMessage("Invalid limitation (needs an integer > 0).");
                    return null;
                }
            }
            if (!isEnded()) {
                System.out.println("Unexpected strings at end of line.");
                return null;
            }
            result.setCommandType(CommandType.SHOW_TABLE_CONTENT);
            result.setTablename(tablename);
            result.setShowRowLimitation(limitation);
            return result;
        } else if (checkTokenIgnoreCase("tables", false)) {
            checkTokenIgnoreCase("tables", true);
            if (!isEnded()) {
                System.out.println("Unexpected strings at end of line.");
                return null;
            }
            SQLParseResult result = new SQLParseResult();
            result.setCommandType(CommandType.SHOW_TABLE_LIST);
            return result;
        } else {
            printErrorMessage("Expect Keyword TABLE or TABLES after SHOW.");
            return null;
        }
    }

    /**
     * Parse DESC.
     *
     * @return parse result, null if failed.
     */
    private SQLParseResult parseDesc() {
        SQLParseResult result = new SQLParseResult();
        if (checkTokenIgnoreCase("full", false)) {
            checkTokenIgnoreCase("full", true);
            result.setShowFullInfo(true);
        } else {
            result.setShowFullInfo(false);
        }
        String tablename = getTableName();
        if (tablename == null) {
            return null;
        }
        if (!isEnded()) {
            System.out.println("Unexpected strings at end of line.");
            return null;
        }
        result.setCommandType(CommandType.DESC);
        result.setTablename(tablename);
        return result;
    }

    /**
     * Parse LOAD.
     *
     * @return parse result, null if failed.
     */
    private SQLParseResult parseLoad() {
        SQLParseResult result = new SQLParseResult();
        if (!checkTokenIgnoreCase("script", true)) {
            printErrorMessage("Expect keyword SCRIPT after LOAD.");
            return null;
        }
        if (checkTokenIgnoreCase("silent", false)) {
            nextToken(true);
            result.setShowFullInfo(false);
        } else {
            result.setShowFullInfo(true);
        }
        if (!checkTokenIgnoreCase("infile", true)) {
            if (result.getShowFullInfo()) {
                printErrorMessage("Expect keyword INFILE after SCRIPT.");
            } else {
                printErrorMessage("Expect keyword INFILE after SILENT.");
            }
            return null;
        }
        String filename = nextToken(true);
        if (filename == null) {
            printErrorMessage("A filename expected after keyword LOAD.");
            return null;
        }
        if (filename.startsWith("'")) {
            filename = filename.substring(1, filename.length() - 1);
        }
        if (!filename.matches("[ ._/a-zA-Z0-9]+")) {
            printErrorMessage("Invalid file name.");
            return null;
        }
        File file = new File(filename);
        if (!file.exists()) {
            printErrorMessage(filename + ": no such file or directory.");
            return null;
        }
        if (!isEnded()) {
            System.out.println("Unexpected tokens at end of line.");
            return null;
        }
        result.setCommandType(CommandType.LOAD);
        result.setFilename(filename);
        return result;
    }

    /**
     * Parse QUIT.
     *
     * @return parse result, null if failed.
     */
    private SQLParseResult parseQuit() {
        if (!isEnded()) {
            System.out.println("Unexpected strings at end of line.");
            return null;
        }
        SQLParseResult result = new SQLParseResult();
        result.setCommandType(CommandType.QUIT);
        return result;
    }

    /**
     * Parse EXIT.
     *
     * @return parse result, null if failed.
     */
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
     * Get next token.
     *
     * @param increment true to increase mIndex.
     * @return next token string, "" if failed.
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
     * @param index index to get.
     * @return position(string index) start from 0.
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
     * @return true if empty, false if not.
     */
    private boolean isEnded() {
        String token = nextToken(true);
        return (token.equals(";") || token.equals("")) && nextToken(true).equals("");
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
     * @param increase true to increase index of tokens, false if not.
     * @return true if empty, false if not.
     */
    private boolean isEnded(boolean increase) {
        String token = nextToken(increase);
        return (token.equals(";") || token.equals(""));
    }

    /**
     * Get table name from next token.
     * <br>** WILL INCREASE TOKEN INDEX **
     *
     * @return a string with name if valid, null if invalid.
     */
    private String getTableName() {
        String name = nextToken(true);
        if (!name.matches("[a-zA-Z_][0-9a-zA-Z_]*")) {
            printErrorMessage("Invalid table name '" + name + "'.");
            return null;
        } else if (SQLKeyWords.isSQLKeyword(name)) {
            printErrorMessage("Invalid table name '" + name + "'.");
            return null;
        } else {
            return name;
        }
    }

    /**
     * Get attribute name from next token.
     * <br>** WILL INCREASE TOKEN INDEX **
     *
     * @return a string with name if valid, null if invalid.
     */
    private String getAttributeName() {
        String name = nextToken(true);
        if (!name.matches("[a-zA-Z_][0-9a-zA-Z_]*")) {
            printErrorMessage("Invalid attribute name '" + name + "'.");
            return null;
        } else if (SQLKeyWords.isSQLKeyword(name)) {
            printErrorMessage("Invalid attribute name '" + name + "'.");
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
     * @return a string of attribute type, null if invalid.
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
                printErrorMessage("Left parenthesis '(' expected after VARCHAR.");
                return null;
            }
            String limit = nextToken(true);
            if (!DataChecker.isValidVarCharLimitation(limit)) {
                printErrorMessage("Invalid limitation.");
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
            printErrorMessage("Invalid data type '" + type + "'.");
            return null;
        }
    }

    /**
     * Get a data block.
     *
     * @return a string of data block, null if failed.
     */
    private SQLBlock getBlock() {
        String block = nextToken(true);
        if (DataChecker.isStringNull(block)) {
            return new SQLBlock(null, true);
        } else if (DataChecker.isValidInteger(block)) {
            return new SQLBlock(block, true);
        } else if (DataChecker.isValidQuotedVarChar(block)) {
            return new SQLBlock(block, true);
        } else {
            printErrorMessage("Invalid data format.");
            return new SQLBlock(null, false);
        }
    }

    /**
     * Check next token with input string (case insensitive).
     *
     * @param expected expect string, string to be compared.
     * @param increase true to increase index.
     * @return true if matched, false if not.
     */
    private boolean checkTokenIgnoreCase(String expected, boolean increase) {
        return nextToken(increase).equalsIgnoreCase(expected);
    }

    /**
     * Check PRIMARY KEY keywords.
     * Used in getAttributeType().
     *
     * @return "PRIMARY" if primary key, "" if no primary key, null if syntax error.
     */
    private String checkPrimaryKey() {
        if (checkTokenIgnoreCase("primary", false)) {
            checkTokenIgnoreCase("primary", true);
            if (checkTokenIgnoreCase("key", true)) {
                return "PRIMARY";
            } else {
                printErrorMessage("Expect keyword KEY after PRIMARY.");
                return null;
            }
        } else {
            return "";
        }
    }

    /**
     * Print error message. Underline will be drawn under the mIndex-th tokens.
     *
     * @param message error message to show.
     */
    private void printErrorMessage(String message) {
        System.out.println(mCommand);
        int underlineLength = (mTokenEnded) ? 1 : mTokens.get(mIndex).length();
        printUnderLine(getPosition(mIndex), underlineLength);
        System.out.println(message);
    }

    /**
     * Print error message. Underline will be drawn under the index-th tokens
     * with length underlineLength specified from parameters.
     *
     * @param message error message to show.
     * @param index set token index for start position.
     * @param underlineLength underline length.
     */
    private void printErrorMessage(String message, int index, int underlineLength) {
        System.out.println(mCommand);
        printUnderLine(getPosition(index), underlineLength);
        System.out.println(message);
    }

    /**
     * Draw underline ^~~~~~~~.
     *
     * @param startPosition start position.
     * @param length length, note that length("^~~") == 3.
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
                    if (i + 1 < mCommand.length()) {
                        if (mCommand.charAt(i + 1) == '>' || mCommand.charAt(i + 1) == '=') {
                            preProcessCommand += "\0<" + mCommand.charAt(i + 1) + "\0";
                            i++;
                        } else preProcessCommand += "\0<\0";
                    } else preProcessCommand += "\0<\0";
                    break;
                case '>':
                    if (i + 1 < mCommand.length()) {
                        if (mCommand.charAt(i + 1) == '=') {
                            preProcessCommand += "\0>" + mCommand.charAt(i + 1) + "\0";
                            i++;
                        } else preProcessCommand += "\0>\0";
                    } else preProcessCommand += "\0>\0";
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
                case ';':
                    preProcessCommand += "\0;\0";
                    break;
                default:
                    preProcessCommand += mCommand.charAt(i);
                    break;
            }
        }
        if (quoteFlag) {
            System.out.println("Single quote not matched.");
            mIsValid = false;
            return;
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
