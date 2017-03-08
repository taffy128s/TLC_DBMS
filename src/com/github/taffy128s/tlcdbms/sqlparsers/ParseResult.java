package com.github.taffy128s.tlcdbms.sqlparsers;

import com.github.taffy128s.tlcdbms.CommandType;
import com.github.taffy128s.tlcdbms.DataType;

import java.util.ArrayList;

public class ParseResult {
    private CommandType mCommandType;
    private String mTablename;
    private ArrayList<String> mAttributeNames;
    private ArrayList<DataType> mAttributeTypes;

    public ParseResult() {
        mCommandType = CommandType.NONE;
        mTablename = "";
    }

    public void setCommandType(CommandType commandType) {
        mCommandType = commandType;
    }

    public CommandType getCommandType() {
        return mCommandType;
    }

    public void setTablename(String tablename) {
        mTablename = tablename;
    }

    public String getTablename() {
        return mTablename;
    }

    public void setAttributeNames(ArrayList<String> attributeNames) {
        mAttributeNames = attributeNames;
    }

    public ArrayList<String> getAttributeNames() {
        return mAttributeNames;
    }

    public void setAttibuteTypes(ArrayList<DataType> attibuteTypes) {
        mAttributeTypes = attibuteTypes;
    }

    public ArrayList<DataType> getAttributeTypes() {
        return mAttributeTypes;
    }
}
