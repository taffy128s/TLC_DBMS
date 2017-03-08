package com.github.taffy128s.tlcdbms.sqlparsers;

import com.github.taffy128s.tlcdbms.CommandType;
import com.github.taffy128s.tlcdbms.DataType;
import com.github.taffy128s.tlcdbms.DataTypeIdentifier;

import java.util.ArrayList;

public class ParseResult {
    private CommandType mCommandType;
    private String mTablename;
    private ArrayList<String> mAttributeNames;
    private ArrayList<DataType> mAttributeTypes;
    private ArrayList<String> mBlocks;
    private int mPrimaryKeyIndex;

    public ParseResult() {
        mCommandType = CommandType.NONE;
        mTablename = "";
        mPrimaryKeyIndex = -1;
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
    
    public void setBlocks(ArrayList<String> blocks) {
        mBlocks = blocks;
    }
    
    public ArrayList<String> getBlocks() {
        return mBlocks;
    }

    public void setPrimaryKeyIndex(int primaryKeyIndex) {
        mPrimaryKeyIndex = primaryKeyIndex;
    }

    public int getPrimaryKeyIndex() {
        return mPrimaryKeyIndex;
    }

    /**
     * Returns a string representation of the object. In general, the
     * {@code toString} method returns a string that
     * "textually represents" this object. The result should
     * be a concise but informative representation that is easy for a
     * person to read.
     * It is recommended that all subclasses override this method.
     * <p>
     * The {@code toString} method for class {@code Object}
     * returns a string consisting of the name of the class of which the
     * object is an instance, the at-sign character `{@code @}', and
     * the unsigned hexadecimal representation of the hash code of the
     * object. In other words, this method returns a string equal to the
     * value of:
     * <blockquote>
     * <pre>
     * getClass().getName() + '@' + Integer.toHexString(hashCode())
     * </pre></blockquote>
     *
     * @return a string representation of the object.
     */
    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        if (mCommandType == CommandType.CREATE) {
            stringBuilder.append("CREATE\n");
            stringBuilder.append("Table ").append(mTablename).append("\n");
            stringBuilder.append("Attribute names\n");
            for (String name : mAttributeNames) {
                stringBuilder.append(name + " ");
            }
            stringBuilder.append("\n");
            stringBuilder.append("Attribute types\n");
            for (DataType type : mAttributeTypes) {
                if (type.getType() == DataTypeIdentifier.INT) {
                    stringBuilder.append("INT ");
                } else {
                    stringBuilder.append("VARCHAR " + type.getLimit());
                }
            }
            stringBuilder.append("\n");
            stringBuilder.append("PRIMARY KEY INDEX " + mPrimaryKeyIndex + "\n");
        } else {
            stringBuilder.append("INSERT\n");
            stringBuilder.append("Table ").append(mTablename).append("\n");
            stringBuilder.append("Block content\n");
            for (String block : mBlocks) {
                stringBuilder.append(block + " ");
            }
            stringBuilder.append("\n");
        }
        return stringBuilder.toString();
    }
}
