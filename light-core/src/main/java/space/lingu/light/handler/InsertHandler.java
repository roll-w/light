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

package space.lingu.light.handler;

import space.lingu.light.LightDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * 插入处理器
 * @author RollW
 */
@SuppressWarnings("unused")
public abstract class InsertHandler<T> extends Handler<T> {
    public InsertHandler(LightDatabase database) {
        super(database);
    }

    public final void insert(T entity) {
        final PreparedStatement stmt = acquire();
        try {
            bind(stmt, entity);
            stmt.execute();
        } catch (SQLException e) {
            printError(e);
        } finally {
            release(stmt);
        }
    }

    public final void insert(T[] entities) {
        List<T> tList = Arrays.asList(entities);
        insert(tList);
    }

    public final void insert(Iterable<? extends T> entities) {
        // TODO 不支持批处理的则使用事务，不支持事务则每个单独提交
        final PreparedStatement stmt = acquire();
        try {
            if (mSharedConnection.getMetadata().supportsBatch) {
                for (T entity : entities) {
                    stmt.clearParameters();
                    bind(stmt, entity);
                    stmt.addBatch();
                }
                stmt.executeBatch();
            } else {
                for (T entity : entities) {
                    bind(stmt, entity);
                    stmt.execute();
                }
            }

        } catch (SQLException e) {
            printError(e);
        } finally {
            release(stmt);
        }
    }

    public final long insertAndReturnId(T entity) {
        final PreparedStatement stmt = acquire();
        try {
            bind(stmt, entity);
            stmt.execute();
            ResultSet set = stmt.getGeneratedKeys();
            if (set.next()) {
                return set.getLong(1);
            }
            set.close();
        } catch (SQLException e) {
            printError(e);
        } finally {
            release(stmt);
        }
        return -1;
    }

    public final long[] insertAndReturnIdsArray(Collection<? extends T> entities) {
        final PreparedStatement stmt = acquire();
        try {
            final long[] result = new long[entities.size()];
            int index = 0;
            for (T entity : entities) {
                bind(stmt, entity);
                stmt.execute();
                ResultSet set = stmt.getGeneratedKeys();
                if (set.next()) {
                    result[index] = set.getLong(1);
                }
                index++;
                set.close();
            }
            return result;
        } catch (SQLException e) {
            printError(e);
        } finally {
            release(stmt);
        }
        return new long[0];
    }

    public final long[] insertAndReturnIdsArray(T[] entities) {
        return insertAndReturnIdsArray(Arrays.asList(entities));
    }

    public final Long[] insertAndReturnIdsArrayBox(Collection<? extends T> entities) {
        final PreparedStatement stmt = acquire();
        try {
            final Long[] result = new Long[entities.size()];
            int index = 0;
            for (T entity : entities) {
                bind(stmt, entity);
                stmt.execute();
                ResultSet set = stmt.getGeneratedKeys();
                if (set.next()) {
                    result[index] = set.getLong(1);
                }
                set.close();
                index++;
            }
            return result;
        } catch (SQLException e) {
            printError(e);
        } finally {
            release(stmt);
        }
        return new Long[0];
    }

    public final Long[] insertAndReturnIdsArrayBox(T[] entities) {
        return insertAndReturnIdsArrayBox(Arrays.asList(entities));
    }

    public final List<Long> insertAndReturnIdsList(T[] entities) {
        return new ArrayList<>(Arrays.asList(insertAndReturnIdsArrayBox(entities)));
    }

    public final List<Long> insertAndReturnIdsList(Collection<? extends T> entities) {
        return new ArrayList<>(Arrays.asList(insertAndReturnIdsArrayBox(entities)));
    }

    private void printError(Throwable throwable) {
        mDatabase.getLogger().error("An error occurred while insert to database.", throwable);
    }

}
