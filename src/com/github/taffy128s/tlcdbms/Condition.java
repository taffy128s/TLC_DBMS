package com.github.taffy128s.tlcdbms;

public class Condition {
	private String mLeftTableName; 
	private String mLeftValue;
	private String mRightTableName;
	private String mRightValue;
	private BinaryOperator mOperator;
	private Boolean mLeftConstant;
	private Boolean mRightConstant;
	public Condition(Boolean leftConstant, String leftTableName, String leftValue, Boolean rightConstant, String rightTableName, String rightValue, BinaryOperator operator) {
		mLeftConstant = leftConstant;
		mRightConstant = rightConstant;
		mLeftTableName = leftTableName;
		mLeftValue = leftValue;
		mRightTableName = rightTableName;
		mRightValue = rightValue;
		mOperator = operator;
	}
	public String getLeftTableName() {
		return mLeftTableName;
	}
	public String getLeftValue() {
		return mLeftValue;
	}
	public String getRightTableName() {
		return mRightTableName;
	}
	public String getRightValue() {
		return mRightValue;
	}
	public BinaryOperator getOperator() {
		return mOperator;
	}
	
	
	
}
