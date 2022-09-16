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

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Further packaging for the {@link Connection}.
 * Implement many useful methods.
 *
 * @author RollW
 */
@SuppressWarnings("unused")
public class SharedConnection {
    private final AtomicBoolean mLock = new AtomicBoolean(false);
    private volatile Connection mConnection;
    protected final LightDatabase mDatabase;
    private final LightDatabase.Metadata metadata;

    public SharedConnection(LightDatabase database) {
        mDatabase = database;
        metadata = database.getMetadata();
    }

    public Connection acquire() {
        return getConnection(mLock.compareAndSet(false, true));
    }

    private Connection getConnection(boolean canUseCached) {
        final Connection conn;
        if (canUseCached) {
            if (mConnection == null) {
                mConnection = requireFromDatabase();
            }
            conn = mConnection;
        } else {
            conn = requireFromDatabase();
        }
        return conn;
    }

    private Connection requireFromDatabase() {
        return mDatabase.requireConnection();
    }

    public LightDatabase.Metadata getMetadata() {
        return metadata;
    }

    public void beginTransaction() {
        if (notSupportTransaction()) {
            return;
        }
        autoCommit(false);
    }

    public void commit() {
        if (notSupportTransaction()) {
            return;
        }
        try {
            mConnection.commit();
        } catch (SQLException e) {
            throw new LightRuntimeException(e);
        } finally {
            autoCommit(true);
        }
    }

    public void rollback() {
        if (notSupportTransaction()) {
            return;
        }
        try {
            mConnection.rollback();
        } catch (SQLException e) {
            throw new LightRuntimeException(e);
        } finally {
            autoCommit(true);
        }
    }

    public void release(Connection connection) {
        if (mConnection == connection) {
            mLock.set(false);
        }
    }

    private void close() {
        mDatabase.releaseConnection(mConnection);
    }

    private void autoCommit(boolean autoCommit) {
        try {
            mConnection.setAutoCommit(autoCommit);
        } catch (SQLException e) {
            throw new LightRuntimeException(e);
        }
    }

    private boolean notSupportTransaction() {
        return !metadata.supportsTransaction;
    }

}
