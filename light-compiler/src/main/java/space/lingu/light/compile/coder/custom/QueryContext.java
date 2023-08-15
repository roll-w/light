package space.lingu.light.compile.coder.custom;

/**
 * Context with processing a custom query.
 *
 * @author RollW
 */
public class QueryContext {
    /**
     * Variable name of the SQLHandler.
     */
    private final String handlerVarName;

    /**
     * Variable name of the ManagedConnection.
     */
    private final String connVarName;

    /**
     * Variable name of the PreparedStatement.
     */
    private final String stmtVarName;

    /**
     * Variable name of the ResultSet. Could be null.
     */
    private String resultSetVarName;

    /**
     * Variable name of the out object (return value).
     * Can be null.
     */
    private String outVarName;

    /**
     * Can release/close the result set.
     */
    private final boolean canReleaseSet;

    /**
     * Needs return a value.
     */
    private final boolean needsReturn;

    /**
     * In transaction.
     */
    private final boolean inTransaction;

    public QueryContext(String handlerVarName, String connVarName,
                        String stmtVarName,
                        String resultSetVarName, String outVarName,
                        boolean canReleaseSet, boolean needsReturn,
                        boolean inTransaction) {
        this.handlerVarName = handlerVarName;
        this.connVarName = connVarName;
        this.stmtVarName = stmtVarName;
        this.resultSetVarName = resultSetVarName;
        this.outVarName = outVarName;
        this.canReleaseSet = canReleaseSet;
        this.needsReturn = needsReturn;
        this.inTransaction = inTransaction;
    }

    public String getHandlerVarName() {
        return handlerVarName;
    }

    public String getConnVarName() {
        return connVarName;
    }

    public String getResultSetVarName() {
        return resultSetVarName;
    }

    public void setResultSetVarName(String resultSetVarName) {
        if (this.resultSetVarName != null) {
            throw new IllegalStateException("resultSetVarName already set");
        }
        this.resultSetVarName = resultSetVarName;
    }

    public String getOutVarName() {
        return outVarName;
    }

    public void setOutVarName(String outVarName) {
        if (this.outVarName != null) {
            throw new IllegalStateException("outVarName already set");
        }
        this.outVarName = outVarName;
    }

    public String getStmtVarName() {
        return stmtVarName;
    }

    public boolean isCanReleaseSet() {
        return canReleaseSet;
    }

    public boolean isNeedsReturn() {
        return needsReturn;
    }

    public boolean isInTransaction() {
        return inTransaction;
    }

    public QueryContext fork(String outVarName, String resultSetVarName) {
        return new QueryContext(
                handlerVarName, connVarName, stmtVarName,
                resultSetVarName, outVarName,
                canReleaseSet, needsReturn,
                inTransaction
        );
    }

    public QueryContext fork(String outVarName) {
        return new QueryContext(
                handlerVarName, connVarName, stmtVarName,
                resultSetVarName, outVarName,
                canReleaseSet, needsReturn,
                inTransaction
        );
    }
}
