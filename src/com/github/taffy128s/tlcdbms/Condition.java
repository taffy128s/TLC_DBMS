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
	
	
	
}
