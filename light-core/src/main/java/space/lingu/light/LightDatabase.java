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


import space.lingu.light.sql.DialectProvider;
import space.lingu.light.log.JdkDefaultLogger;
import space.lingu.light.log.LightLogger;
import space.lingu.light.connect.ConnectionPool;
import space.lingu.light.struct.Table;

import java.sql.*;
import java.util.*;
import java.util.concurrent.Executor;

/**
 * @author RollW
 */
public abstract class LightDatabase {
    private static final String IMPL_SUFFIX = "_Impl";

    private DialectProvider mDialectProvider;
    private ConnectionPool mConnectionPool;
    private DatasourceConfig mSourceConfig;

    private Executor mQueryExecutor;
    private Executor mTransactionExecutor;

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

    public void setLogger(LightLogger logger) {
        if (logger == null) {
            return;
        }
        this.mLogger = logger;
    }

    public LightDatabase() {
    }

    protected void init(DatabaseConfiguration conf) {
        this.mName = conf.name;
        this.mSourceConfig = conf.datasourceConfig;
        if (conf.logger != null) {
            this.mLogger = conf.logger;
        }
        this.mDialectProvider = conf.dialectProvider;
        conf.connectionPool.setDataSourceConfig(mSourceConfig);
        this.mConnectionPool = conf.connectionPool;
    }

    public void executeRawSqlWithNoReturn(String sql) {
        Connection conn = requireConnection();
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

    public Connection requireConnection() {
        checkConnectionPool();
        Connection connection = mConnectionPool.requireConnection();
        if (mName == null) {
            return connection;
        }
        try {
            PreparedStatement useStmt = connection.prepareStatement(
                    getDialectProvider().useDatabase(mName)
            );
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
        PreparedStatement stmt = null;
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
            mLogger.error("An error occurred while require PreparedStatement.", e);
            mLogger.trace(e);
        }

        return stmt;
    }

    protected void clearAllTables() {
    }

    protected void destroyTable(String tableName) {
        executeRawSqlWithNoReturn(mDialectProvider.destroyTable(tableName));
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

    public static class Builder<T extends LightDatabase> {
        private final Class<T> mDatabaseClass;
        private final String mName;
        private final Database database;
        private DatasourceConfig mConfig;
        private final DialectProvider mProvider;
        private ConnectionPool mConnectionPool;
        private LightLogger mLogger;

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
            return this;
        }

        /**
         * 设置冲突时重建数据表
         * @return this
         */
        @LightExperimentalApi
        public Builder<T> deleteOnConflict() {
            return deleteOnConflict(true);
        }

        /**
         * 设置冲突时是否重建数据表
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
                mConfig = new DatasourceLoader(database.datasourceConfig()).load();
            }
        }

        private DatabaseConfiguration createConf() {
            generateConfig();
            return new DatabaseConfiguration(
                    mName,
                    mConfig,
                    mConnectionPool,
                    mProvider,
                    mLogger
            );
        }

        public T build() {
            T database = Light.getGeneratedImplInstance(mDatabaseClass,
                    IMPL_SUFFIX);
            database.init(createConf());
            return database;
        }
    }

}
