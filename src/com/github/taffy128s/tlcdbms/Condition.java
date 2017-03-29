package com.github.taffy128s.tlcdbms;

public class Condition {
	private String mLeftConstant;
	private String mLeftTableName; 
	private String mLeftAttribute;
	private String mRightConstant;
	private String mRightTableName;
	private String mRightAttribute;
	private BinaryOperator mOperator;
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
	public String getLeftTableName() {
		return mLeftTableName;
	}
	public String getLeftAttribute() {
		return mLeftAttribute;
	}
	public String getRightTableName() {
		return mRightTableName;
	}
	public String getRightAttribute() {
		return mRightAttribute;
	}
	public String getLeftConstant() {
		return mLeftConstant;
	}
	public String getRightConstant() {
		return mRightConstant;
	}
	public BinaryOperator getOperator() {
		return mOperator;
	}
	
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
	
}
