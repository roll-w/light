/*
 * Copyright (C) 2022 Lingu Light Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package space.lingu.light;


import space.lingu.light.connect.ConnectionPool;
import space.lingu.light.log.JdkDefaultLogger;
import space.lingu.light.sql.DialectProvider;
import space.lingu.light.struct.DatabaseInfo;
import space.lingu.light.struct.Table;

import java.sql.*;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

/**
 * @author RollW
 */
public abstract class LightDatabase {
    private static final String IMPL_SUFFIX = "_Impl";

    private DialectProvider mDialectProvider;
    private ConnectionPool mConnectionPool;
    private DatasourceConfig mSourceConfig;

    private Executor mQueryExecutor;

    private String mName;

    public final DatasourceConfig getDatasourceConfig() {
        return mSourceConfig;
    }

    public final ConnectionPool getConnectionPool() {
        return mConnectionPool;
    }

    private LightLogger mLogger = JdkDefaultLogger.getGlobalLogger();

    public final LightLogger getLogger() {
        return mLogger;
    }

    /**
     * Do not implement or call this method in your program!
     */
    protected abstract LightInfo.LightInfoDao _LightInfoDao();

    public void setLogger(LightLogger logger) {
        if (logger == null) {
            return;
        }
        this.mLogger = logger;
    }

    public LightDatabase() {
    }

    private DatabaseInfo mDatabaseInfo;

    protected void init(DatabaseConfiguration conf) {
        registerAllTables();

        this.mName = conf.name;
        this.mSourceConfig = conf.datasourceConfig;
        if (conf.logger != null) {
            this.mLogger = conf.logger;
        }
        this.mDialectProvider = conf.dialectProvider;
        conf.connectionPool.setDataSourceConfig(mSourceConfig);
        this.mConnectionPool = conf.connectionPool;
        mDatabaseInfo = new DatabaseInfo(mName, Collections.emptyList());

        createDatabase(mDatabaseInfo);
        createTables();
        createIndices();
    }

    protected void registerAllTables() {
    }

    protected void createDatabase(DatabaseInfo info) {
        final String sql = mDialectProvider.create(info);
        executeRaw(sql, rawConnection());
        // TODO
    }

    private void createTables() {
        List<String> statements = mTableStructCache.values()
                .stream().map(table -> {
                    if (Objects.equals(table.getName(), LightInfo.sTableName)) {
                        // not create the [LightInfo] table current until complete verify function.
                        return null;
                    }
                    return mDialectProvider.create(table);
                })
                .collect(Collectors.toList());
        for (String statement : statements) {
            if (mLogger != null) {
                mLogger.debug("execute create table statement, statement: " + statement);
            }

            executeRawSqlWithNoReturn(statement);
        }
    }

    private void createIndices() throws LightIndexCreateException {
        List<String> statements = mTableStructCache.values()
                .stream().map(table -> {
                    if (Objects.equals(table.getName(), LightInfo.sTableName)) {
                        // not create the [LightInfo] table current until complete verify function.
                        return new ArrayList<String>();
                    }
                    return table.getIndices().stream().map(index ->
                                    mDialectProvider.create(index))
                            .collect(Collectors.toList());
                })
                .collect(ArrayList::new, ArrayList::addAll, ArrayList::addAll);
        for (String statement : statements) {
            if (mLogger != null) {
                mLogger.debug("execute create index statement, statement: " + statement);
            }
            try {
                executeRawSqlWithNoReturn(statement);
            } catch (LightRuntimeException e) {
                int errorCode = ((SQLException) e.getCause()).getErrorCode();
                if (errorCode == 1061) {
                    // 1061 - MySQL Error Name: ER_DUP_KEYNAME
                    //
                    // since MySQL not support "IF NOT EXIST" in creating an index,
                    // do special treatment for MySQL.
                    return;
                }
                throw new LightIndexCreateException(e.getCause());
            }
        }
    }

    public void executeRawSqlWithNoReturn(String sql) {
        if (sql == null) {
            return;
        }
        Connection conn = requireConnection();
        executeRaw(sql, conn);
    }

    public void executeRaw(String sql, Connection conn) {
        if (sql == null) {
            return;
        }
        PreparedStatement stmt = resolveStatement(sql, conn, false);
        try {
            stmt.execute();
            stmt.close();
        } catch (SQLException e) {
            throw new LightRuntimeException(e);
        } finally {
            releaseConnection(conn);
        }
    }

    private Connection rawConnection() {
        checkConnectionPool();
        return mConnectionPool.requireConnection();
    }

    public Connection requireConnection() {
        Connection connection = rawConnection();
        if (mName == null) {
            return connection;
        }
        String stmt = getDialectProvider().useDatabase(mName);
        if (stmt == null) {
            return connection;
        }
        try {
            PreparedStatement useStmt = connection.prepareStatement(stmt);
            useStmt.execute();
            useStmt.close();
        } catch (SQLException e) {
            throw new LightRuntimeException(e);
        }
        return connection;
    }

    public void releaseConnection(Connection connection) {
        checkConnectionPool();
        mConnectionPool.release(connection);
    }

    private void checkConnectionPool() {
        if (mConnectionPool == null) {
            throw new NullPointerException("ConnectionPool cannot be null!");
        }
    }

    public DialectProvider getDialectProvider() {
        return mDialectProvider;
    }

    public PreparedStatement resolveStatement(String sql, Connection connection, boolean returnsGeneratedKey) {
        PreparedStatement stmt;
        if (connection == null) {
            throw new IllegalStateException("Connection is null!");
        }
        try {
            if (returnsGeneratedKey) {
                stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            } else {
                stmt = connection.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            }
        } catch (SQLException e) {
            mLogger.error("An error occurred while require a PreparedStatement.", e);
            throw new LightRuntimeException(e);
        }

        return stmt;
    }

    protected void clearAllTables() {
        mTableStructCache.values().forEach(table ->
                destroyTable(table.getName()));
    }

    protected void destroyTable(String tableName) {
        Table table = findTable(tableName);
        if (table == null) {
            return;
        }
        executeRawSqlWithNoReturn(mDialectProvider.drop(table));
    }

    private final Map<String, Table> mTableStructCache =
            Collections.synchronizedMap(new HashMap<>());

    protected void registerTable(Table table) {
        if (table == null) {
            return;
        }
        mTableStructCache.put(table.getName(), table);
    }

    public Table findTable(String tableName) {
        if (!mTableStructCache.containsKey(tableName)) {
            return null;
        }
        return mTableStructCache.get(tableName);
    }

    private volatile Metadata mMetadata;
    private final byte[] mLock = new byte[0];

    public Metadata getMetadata() {
        if (mMetadata == null) {
            synchronized (mLock) {
                if (mMetadata == null) {
                    boolean supportsBatch;
                    boolean supportsTransaction;
                    try {
                        Connection connection =
                                getConnectionPool().requireConnection();
                        DatabaseMetaData databaseMetaData =
                                connection.getMetaData();
                        supportsBatch =
                                databaseMetaData.supportsBatchUpdates();
                        supportsTransaction =
                                databaseMetaData.supportsTransactions();
                        getConnectionPool().release(connection);
                    } catch (SQLException e) {
                        throw new LightRuntimeException(e);
                    }

                    mMetadata = new Metadata(supportsBatch, supportsTransaction);
                }
            }
        }

        return mMetadata;
    }

    public static class Metadata {
        public final boolean supportsBatch;
        public final boolean supportsTransaction;

        Metadata(boolean supportsBatch, boolean supportsTransaction) {
            this.supportsBatch = supportsBatch;
            this.supportsTransaction = supportsTransaction;
        }
    }

    public static class MigrationContainer {
        private final HashMap<Integer, TreeMap<Integer, Migration>> mMigrations = new HashMap<>();

        public void addMigrations(Migration... migrations) {
            for (Migration migration : migrations) {
                addMigration(migration);
            }
        }

        public void addMigrations(List<Migration> migrations) {
            for (Migration migration : migrations) {
                addMigration(migration);
            }
        }

        private void addMigration(Migration migration) {
            final int start = migration.startVersion;
            final int end = migration.endVersion;
            TreeMap<Integer, Migration> targetMap =
                    mMigrations.computeIfAbsent(start, k -> new TreeMap<>());
            targetMap.put(end, migration);
        }

        public Map<Integer, Map<Integer, Migration>> getMigrations() {
            return Collections.unmodifiableMap(mMigrations);
        }

        public List<Migration> findMigrationPath(int start, int end) {
            if (start == end) {
                return Collections.emptyList();
            }
            boolean migrateUp = end > start;
            List<Migration> result = new ArrayList<>();
            return findUpMigrationPath(result, migrateUp, start, end);
        }

        private List<Migration> findUpMigrationPath(List<Migration> result,
                                                    boolean upgrade,
                                                    int start, int end) {
            while (upgrade ? start < end : start > end) {
                TreeMap<Integer, Migration> targetNodes = mMigrations.get(start);
                if (targetNodes == null) {
                    return null;
                }
                Set<Integer> keySet;
                if (upgrade) {
                    keySet = targetNodes.descendingKeySet();
                } else {
                    keySet = targetNodes.keySet();
                }
                boolean found = false;
                for (int targetVersion : keySet) {
                    final boolean shouldAddToPath;
                    if (upgrade) {
                        shouldAddToPath = targetVersion <= end && targetVersion > start;
                    } else {
                        shouldAddToPath = targetVersion >= end && targetVersion < start;
                    }
                    if (shouldAddToPath) {
                        result.add(targetNodes.get(targetVersion));
                        start = targetVersion;
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    return null;
                }
            }
            return result;
        }

    }

    public static class Builder<T extends LightDatabase> {
        private final Class<T> mDatabaseClass;
        private final String mName;
        private final Database database;
        private DatasourceConfig mConfig;
        private final DialectProvider mProvider;
        private ConnectionPool mConnectionPool;
        private LightLogger mLogger;
        private final MigrationContainer mMigrationContainer;

        Builder(Class<T> clazz, DialectProvider provider) {
            if (clazz == null || provider == null) {
                throw new IllegalArgumentException("Cannot be null!");
            }
            mDatabaseClass = clazz;
            mProvider = provider;
            database = clazz.getAnnotation(Database.class);
            if (database == null) {
                throw new IllegalStateException("Must be annotated with '@Database'!");
            }
            mName = database.name();
            mMigrationContainer = new MigrationContainer();
        }

        Builder(Class<T> clazz, Class<? extends DialectProvider> providerClass) {
            this(clazz, Light.createDialectProviderInstance(providerClass));
        }

        public Builder<T> setLogger(LightLogger logger) {
            mLogger = logger;
            return this;
        }

        @LightExperimentalApi
        public Builder<T> addMigrations(Migration... migrations) {
            mMigrationContainer.addMigrations(migrations);
            return this;
        }

        /**
         * 设置冲突时重建数据表
         *
         * @return this
         */
        @LightExperimentalApi
        public Builder<T> deleteOnConflict() {
            return deleteOnConflict(true);
        }

        /**
         * 设置冲突时是否重建数据表
         *
         * @param enable enable
         * @return this
         */
        @LightExperimentalApi
        public Builder<T> deleteOnConflict(boolean enable) {
            return this;
        }

        public Builder<T> datasource(DatasourceConfig config) {
            mConfig = config;
            return this;
        }

        public Builder<T> setConnectionPool(Class<? extends ConnectionPool> poolClass) {
            mConnectionPool = Light.createConnectionPoolInstance(poolClass);
            return this;
        }

        public Builder<T> setConnectionPool(ConnectionPool connectionPool) {
            mConnectionPool = connectionPool;
            return this;
        }

        private void generateConfig() {
            if (mConfig != null) return;
            if (database.datasourceConfig().isEmpty()) {
                mConfig = new DatasourceLoader().load();
            } else {
                mConfig = new DatasourceLoader(database.datasourceConfig(), database.name()).load();
            }
        }

        private DatabaseConfiguration createConf() {
            generateConfig();
            return new DatabaseConfiguration(
                    mName,
                    mConfig,
                    mConnectionPool,
                    mProvider,
                    mLogger,
                    mMigrationContainer);
        }

        public T build() {
            T database = Light.getGeneratedImplInstance(mDatabaseClass,
                    IMPL_SUFFIX);
            database.init(createConf());
            return database;
        }
    }

}
