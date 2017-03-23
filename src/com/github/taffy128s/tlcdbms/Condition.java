package com.github.taffy128s.tlcdbms;

public class Condition {
	private String mLeftTableName; 
	private String mLeftValue;
	private String mRightTableName;
	private String mRightValue;
	private BinaryOperation mOperator;
	public Condition(String leftTableName, String leftValue, String rightTableName, String rightValue, BinaryOperation operator) {
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
	public BinaryOperation getOperator() {
		return mOperator;
	}
	
	
	
}
