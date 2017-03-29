package com.github.taffy128s.tlcdbms;

public class Condition {
	private String mLeftTableName; 
	private String mLeftAttribute;
	private String mRightTableName;
	private String mRightAttribute;
	private BinaryOperator mOperator;
	private Object mLeftConstant;
	private Object mRightConstant;
	public Condition(Object leftConstant, String leftTableName, String leftAttribute, 
			Object rightConstant, String rightTableName, String rightAttribute, BinaryOperator operator) {
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
	public BinaryOperator getOperator() {
		return mOperator;
	}
	
	
	
}
