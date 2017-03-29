package com.github.taffy128s.tlcdbms.sqlparsers;

import com.github.taffy128s.tlcdbms.*;

import java.util.ArrayList;

/**
 * Parse result generated by SQLParser.
 */
public class SQLParseResult {
    private CommandType mCommandType;
    private String mTablename;
    private ArrayList<String> mTablenames;
    private ArrayList<String> mAttributeNames;
    private ArrayList<DataType> mAttributeTypes;
    private ArrayList<TableStructure> mAttributeIndices;
    private ArrayList<String> mBlocks;
    private ArrayList<String> mUpdateOrder;
    private int mPrimaryKeyIndex;
    private int mShowRowLimitation;
    private boolean mCustomOrder;
    private boolean mShowFullInfo;
    private SortingType mShowSortType;
    private QueryType mQueryType;
    private ArrayList<String> mTargets;
    private ArrayList<Condition> mConditions;
    
    /**
     * Constructor. Initialize all members.
     */
    public SQLParseResult() {
        mCommandType = CommandType.NONE;
        mTablename = "";
        mTablenames = null;
        mAttributeNames = null;
        mAttributeTypes = null;
        mAttributeIndices = null;
        mBlocks = null;
        mUpdateOrder = null;
        mPrimaryKeyIndex = -1;
        mShowRowLimitation = 0;
        mCustomOrder = false;
        mShowFullInfo = false;
        mShowSortType = SortingType.NONE;
        mQueryType = QueryType.NORMAL;
        mTargets = null;
        mConditions = null;
    }

    /**
     * CommandType setter.
     *
     * @param commandType command type to set.
     */
    public void setCommandType(CommandType commandType) {
        mCommandType = commandType;
    }

    /**
     * CommandType getter.
     *
     * @return command type.
     */
    public CommandType getCommandType() {
        return mCommandType;
    }

    /**
     * Tablename setter.
     *
     * @param tablename tablename to set.
     */
    public void setTablename(String tablename) {
        mTablename = tablename;
    }

    /**
     * Tablename getter.
     *
     * @return tablename.
     */
    public String getTablename() {
        return mTablename;
    }

    /**
     * Tablenames setter.
     *
     * @param tablenames tablenames to set.
     */
    public void setTablenames(ArrayList<String> tablenames) {
        mTablenames = tablenames;
    }

    /**
     * Tablenames getter.
     *
     * @return a list of tablenames.
     */
    public ArrayList<String> getTablenames() {
        return mTablenames;
    }

    /**
     * AttributeNames setter.
     *
     * @param attributeNames a list of attribute names.
     */
    public void setAttributeNames(ArrayList<String> attributeNames) {
        mAttributeNames = attributeNames;
    }

    /**
     * AttributeNames getter.
     *
     * @return a list of attribute names.
     */
    public ArrayList<String> getAttributeNames() {
        return mAttributeNames;
    }

    /**
     * AttributeTypes setter.
     *
     * @param attributeTypes a list of attribute types.
     */
    public void setAttributeTypes(ArrayList<DataType> attributeTypes) {
        mAttributeTypes = attributeTypes;
    }

    /**
     * AttributeTypes getter.
     *
     * @return a list of attribute types.
     */
    public ArrayList<DataType> getAttributeTypes() {
        return mAttributeTypes;
    }

    /**
     * AttributeIndices setter.
     *
     * @param attributeIndices a list of table structures.
     */
    public void setAttributeIndices(ArrayList<TableStructure> attributeIndices) {
        mAttributeIndices = attributeIndices;
    }

    /**
     * AttributeIndices getter.
     *
     * @return a list of table structures.
     */
    public ArrayList<TableStructure> getAttributeIndices() {
        return mAttributeIndices;
    }

    /**
     * Blocks setter.
     *
     * @param blocks a list of data blocks.
     */
    public void setBlocks(ArrayList<String> blocks) {
        mBlocks = blocks;
    }

    /**
     * Blocks getter.
     *
     * @return a list of data blocks.
     */
    public ArrayList<String> getBlocks() {
        return mBlocks;
    }

    /**
     * UpdateOrder setter.
     *
     * @param updateOrder a list of string(attribute names).
     */
    public void setUpdateOrder(ArrayList<String> updateOrder) {
        mUpdateOrder = updateOrder;
    }

    /**
     * UpdateOrder getter.
     *
     * @return a list of string(attribute names).
     */
    public ArrayList<String> getUpdateOrder() {
        return mUpdateOrder;
    }

    /**
     * Primary key setter.
     *
     * @param primaryKeyIndex primary key to set.
     */
    public void setPrimaryKeyIndex(int primaryKeyIndex) {
        mPrimaryKeyIndex = primaryKeyIndex;
    }

    /**
     * Primary key getter.
     *
     * @return primary key.
     */
    public int getPrimaryKeyIndex() {
        return mPrimaryKeyIndex;
    }

    /**
     * Show row limitation setter.
     *
     * @param showRowLimitation value to set.
     */
    public void setShowRowLimitation(int showRowLimitation) {
        mShowRowLimitation = showRowLimitation;
    }

    /**
     * Show row limitation getter.
     *
     * @return show row limitation.
     */
    public int getShowRowLimitation() {
        return mShowRowLimitation;
    }

    /**
     * CustomOrder setter.
     *
     * @param customOrder true if custom order exists, false otherwise.
     */
    public void setCustomOrder(boolean customOrder) {
        mCustomOrder = customOrder;
    }

    /**
     * Check custom order flag.
     *
     * @return true if custom order exists, false otherwise.
     */
    public boolean isCustomOrder() {
        return mCustomOrder;
    }

    /**
     * ShowFullInfo setter.
     *
     * @param showFullInfo value to set.
     */
    public void setShowFullInfo(boolean showFullInfo) {
        mShowFullInfo = showFullInfo;
    }

    /**
     * ShowFullInfo getter.
     *
     * @return true if show, false otherwise.
     */
    public boolean getShowFullInfo() {
        return mShowFullInfo;
    }

    /**
     * ShowSortType setter.
     *
     * @param sortType type to set.
     */
    public void setShowSortType(SortingType sortType) {
        mShowSortType = sortType;
    }

    /**
     * ShowSortType getter.
     *
     * @return sorting type.
     */
    public SortingType getShowSortType() {
        return mShowSortType;
    }

    public QueryType getQueryType() {
		return mQueryType;
	}

	public void setQueryType(QueryType mQueryType) {
		this.mQueryType = mQueryType;
	}

	public ArrayList<String> getTargets() {
		return mTargets;
	}

	public void setTargets(ArrayList<String> mTargets) {
		this.mTargets = mTargets;
	}

	public ArrayList<Condition> getConditions() {
		return mConditions;
	}

	public void setConditions(ArrayList<Condition> mConditions) {
		this.mConditions = mConditions;
	}

	@Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        if (mCommandType == CommandType.CREATE) {
            stringBuilder.append("CREATE\n");
            stringBuilder.append("Table ").append(mTablename).append("\n");
            stringBuilder.append("Attribute names\n");
            for (String name : mAttributeNames) {
                stringBuilder.append(name).append(" ");
            }
            stringBuilder.append("\n");
            stringBuilder.append("Attribute types\n");
            for (DataType type : mAttributeTypes) {
                if (type.getType() == DataTypeIdentifier.INT) {
                    stringBuilder.append("INT ");
                } else {
                    stringBuilder.append("VARCHAR ").append(type.getLimit());
                }
            }
            stringBuilder.append("\n");
            stringBuilder.append("PRIMARY KEY INDEX " + mPrimaryKeyIndex + "\n");
        } else if (mCommandType == CommandType.INSERT) {
            stringBuilder.append("INSERT\n");
            stringBuilder.append("Table ").append(mTablename).append("\n");
            stringBuilder.append("Block content\n");
            for (String block : mBlocks) {
                stringBuilder.append(block).append(" ");
            }
            stringBuilder.append("\n");
            if (mCustomOrder) {
                stringBuilder.append("Custom order\n");
                for (String attrName : mUpdateOrder) {
                    stringBuilder.append(attrName).append(" ");
                }
                stringBuilder.append("\n");
            }
        } else if (mCommandType == CommandType.SELECT) {
            stringBuilder.append("SELECT\n");
            stringBuilder.append("Query type: ");
            if (mQueryType == QueryType.COUNT) {
                stringBuilder.append("COUNT").append("\n");
            } else if (mQueryType == QueryType.SUM) {
                stringBuilder.append("SUM").append("\n");
            } else if (mQueryType == QueryType.NORMAL) {
                stringBuilder.append("NORMAL").append("\n");
            }
            stringBuilder.append("Targets:\n");
            for (String target : mTargets) {
                stringBuilder.append("  ").append(target).append("\n");
            }
            stringBuilder.append("Table names:\n");
            for (String tableName : mTablenames) {
                stringBuilder.append("  ").append(tableName).append("\n");
            }
            stringBuilder.append("Conditions:\n");
            if (mConditions == null) {
                stringBuilder.append("  ").append("No 'WHERE'").append("\n");
            } else {
                for (Condition condition : mConditions) {
                    stringBuilder.append("  ").append(condition).append("\n");
                }
            }
        }
        return stringBuilder.toString();
    }
}
