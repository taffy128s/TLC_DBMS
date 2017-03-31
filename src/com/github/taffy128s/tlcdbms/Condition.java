package com.github.taffy128s.tlcdbms;

public class Condition {
    private String mLeftConstant;
    private String mLeftTableName;
    private String mLeftAttribute;
    private String mRightConstant;
    private String mRightTableName;
    private String mRightAttribute;
    private BinaryOperator mOperator;

    /**
     * Constructor.
     *
     * @param leftConstant left constant to set.
     * @param leftTableName left table name to set.
     * @param leftAttribute left attribute to set.
     * @param rightConstant right constant to set.
     * @param rightTableName right table name to set.
     * @param rightAttribute right attribute to set.
     * @param operator operator to set.
     */
    public Condition(String leftConstant, String leftTableName, String leftAttribute,
            String rightConstant, String rightTableName, String rightAttribute, BinaryOperator operator) {
        mLeftConstant = leftConstant;
        mRightConstant = rightConstant;
        mLeftTableName = leftTableName;
        mLeftAttribute = leftAttribute;
        mRightTableName = rightTableName;
        mRightAttribute = rightAttribute;
        mOperator = operator;
    }

    /**
     * Left table name getter.
     *
     * @return left table name.
     */
    public String getLeftTableName() {
        return mLeftTableName;
    }

    /**
     * Left table name setter.
     *
     * @param tableName table name to set.
     */
    public void setLeftTableName(String tableName) {
        mLeftTableName = tableName;
    }

    /**
     * Left attribute getter.
     *
     * @return left attribute.
     */
    public String getLeftAttribute() {
        return mLeftAttribute;
    }

    /**
     * Left attribute setter.
     *
     * @param leftAttribute left attribute to set.
     */
    public void setLeftAttribute(String leftAttribute) {
        mLeftAttribute = leftAttribute;
    }

    /**
     * Right table name getter.
     *
     * @return right table name.
     */
    public String getRightTableName() {
        return mRightTableName;
    }

    /**
     * Right table name setter.
     *
     * @param tableName table name to set.
     */
    public void setRightTableName(String tableName) {
        mRightTableName = tableName;
    }

    /**
     * Right attribute getter.
     *
     * @return right attribute.
     */
    public String getRightAttribute() {
        return mRightAttribute;
    }

    /**
     * Right attribute setter.
     *
     * @param rightAttribute right attribute to set.
     */
    public void setRightAttribute(String rightAttribute) {
        mRightAttribute = rightAttribute;
    }

    /**
     * Left constant getter.
     *
     * @return left constant.
     */
    public String getLeftConstant() {
        return mLeftConstant;
    }

    /**
     * Left constant setter.
     *
     * @param constant constant to set.
     */
    public void setLeftConstant(String constant) {
        mLeftConstant = constant;
    }

    /**
     * Right constant getter.
     *
     * @return right constant.
     */
    public String getRightConstant() {
        return mRightConstant;
    }

    /**
     * Right constant setter.
     *
     * @param constant constant to set.
     */
    public void setRightConstant(String constant) {
        mRightConstant = constant;
    }

    /**
     * Operator getter.
     *
     * @return operator.
     */
    public BinaryOperator getOperator() {
        return mOperator;
    }

    /**
     * Operator setter.
     *
     * @param operator operator to set.
     */
    public void setOperator(BinaryOperator operator) {
        mOperator = operator;
    }

    /**
     * Transform this class to a string.
     */
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(mLeftConstant).append(" ");
        stringBuilder.append(mLeftTableName).append(" ");
        stringBuilder.append(mLeftAttribute).append(" | ");
        stringBuilder.append(mRightConstant).append(" ");
        stringBuilder.append(mRightTableName).append(" ");
        stringBuilder.append(mRightAttribute).append(" ...");
        if (mOperator == BinaryOperator.AND) {
            stringBuilder.append("AND");
        } else if (mOperator == BinaryOperator.OR) {
            stringBuilder.append("OR");
        } else if (mOperator == BinaryOperator.GREATER_EQUAL) {
            stringBuilder.append(">=");
        } else if (mOperator == BinaryOperator.GREATER_THAN) {
            stringBuilder.append(">");
        } else if (mOperator == BinaryOperator.LESS_EQUAL) {
            stringBuilder.append("<=");
        } else if (mOperator == BinaryOperator.LESS_THAN) {
            stringBuilder.append("<");
        } else if (mOperator == BinaryOperator.EQUAL) {
            stringBuilder.append("=");
        } else if (mOperator == BinaryOperator.NOT_EQUAL) {
            stringBuilder.append("<>");
        } else {
            stringBuilder.append("unknown operator");
        }
        return stringBuilder.toString();
    }

    public static BinaryOperator reverseOperator(BinaryOperator operator) {
        switch (operator) {
            case EQUAL:
                return BinaryOperator.EQUAL;
            case NOT_EQUAL:
                return BinaryOperator.NOT_EQUAL;
            case LESS_THAN:
                return BinaryOperator.GREATER_THAN;
            case LESS_EQUAL:
                return BinaryOperator.GREATER_EQUAL;
            case GREATER_THAN:
                return BinaryOperator.LESS_THAN;
            case GREATER_EQUAL:
                return BinaryOperator.LESS_EQUAL;
            default:
                return BinaryOperator.EQUAL;
        }
    }

    public static Object getConstant(String constant) {
        if (DataChecker.isStringNull(constant)) {
            return "null";
        } else if (DataChecker.isValidInteger(constant)) {
            return Integer.parseInt(constant);
        } else if (DataChecker.isValidQuotedVarChar(constant)) {
            return constant;
        } else {
            return constant;
        }
    }

    public static boolean calculateCondition(Condition condition, DataRecord left, int leftIndex, DataRecord right, int rightIndex) {
        Object leftObject = (leftIndex != -1) ? left.get(leftIndex) : null;
        Object rightObject = (rightIndex != -1) ? right.get(rightIndex) : null;
        if (condition.getLeftConstant() != null && condition.getRightConstant() != null) {
            return calculateResult(condition.getLeftConstant(), condition.getRightConstant(), condition.getOperator());
        } else if (condition.getLeftConstant() != null && condition.getRightConstant() == null) {
            return calculateResult(condition.getLeftConstant(), rightObject, condition.getOperator());
        } else if (condition.getLeftConstant() == null && condition.getRightConstant() != null) {
            return calculateResult(leftObject, condition.getRightConstant(), condition.getOperator());
        } else {
            return calculateResult(leftObject, rightObject, condition.getOperator());
        }
    }

    public static boolean calculateResult(Object left, Object right, BinaryOperator operator) {
        switch (operator) {
            case EQUAL:
                return left.equals(right);
            case NOT_EQUAL:
                return !left.equals(right);
            case LESS_THAN:
                return ((Comparable) left).compareTo(right) < 0;
            case LESS_EQUAL:
                return ((Comparable) left).compareTo(right) <= 0;
            case GREATER_THAN:
                return ((Comparable) left).compareTo(right) > 0;
            case GREATER_EQUAL:
                return ((Comparable) left).compareTo(right) >= 0;
            default:
                return false;
        }
    }
}
