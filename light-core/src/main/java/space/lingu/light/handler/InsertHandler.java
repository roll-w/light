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
import space.lingu.light.LightRuntimeException;
import space.lingu.light.ManagedConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Insert handler.
 *
 * @author RollW
 */
@SuppressWarnings("unused")
public abstract class InsertHandler<T> extends Handler<T> {
    public InsertHandler(LightDatabase database) {
        super(database);
    }

    public final void insert(T entity) {
        final ManagedConnection conn = newConnection();
        final PreparedStatement stmt = acquire(conn);
        try {
            bind(stmt, entity);
            conn.beginTransaction();
            stmt.execute();
        } catch (SQLException e) {
            conn.rollback();
            throw new LightRuntimeException(e);
        } finally {
            conn.commit();
            conn.close();
        }
    }

    public final void insert(T[] entities) {
        List<T> tList = Arrays.asList(entities);
        insert(tList);
    }

    public final void insert(Iterable<? extends T> entities) {
        final ManagedConnection conn = newConnection();
        final PreparedStatement stmt = acquire(conn);
        try {
            if (conn.getMetadata().supportsBatch) {
                for (T entity : entities) {
                    stmt.clearParameters();
                    bind(stmt, entity);
                    stmt.addBatch();
                }
                stmt.executeBatch();
            } else {
                for (T entity : entities) {
                    stmt.clearParameters();
                    bind(stmt, entity);

                    conn.beginTransaction();
                    stmt.execute();
                    conn.commit();
                }
            }

        } catch (SQLException e) {
            conn.rollback();
            throw new LightRuntimeException(e);
        } finally {
            conn.close();
        }
    }

    public final long insertAndReturnId(T entity) {
        final ManagedConnection conn = newConnection();
        final PreparedStatement stmt = acquireReturnsGenerateKey(conn);
        try {
            bind(stmt, entity);
            conn.beginTransaction();
            stmt.execute();
            conn.commit();

            ResultSet set = stmt.getGeneratedKeys();
            if (set.next()) {
                return set.getLong(1);
            }
            set.close();
        } catch (SQLException e) {
            conn.rollback();
            throw new LightRuntimeException(e);
        } finally {
            conn.close();
        }
        return -1;
    }

    public final long[] insertAndReturnIdsArray(Collection<? extends T> entities) {
        final ManagedConnection conn = newConnection();
        final PreparedStatement stmt = acquireReturnsGenerateKey(conn);
        try {
            final long[] result = new long[entities.size()];
            int index = 0;
            for (T entity : entities) {
                bind(stmt, entity);
                conn.beginTransaction();
                stmt.execute();
                conn.commit();

                ResultSet set = stmt.getGeneratedKeys();
                if (set.next()) {
                    result[index] = set.getLong(1);
                }
                index++;
                set.close();
            }
            return result;
        } catch (SQLException e) {
            conn.rollback();
            throw new LightRuntimeException(e);
        } finally {
            conn.close();
        }
    }

    public final long[] insertAndReturnIdsArray(Iterable<? extends T> entities) {
        final ManagedConnection conn = newConnection();
        final PreparedStatement stmt = acquireReturnsGenerateKey(conn);
        try {
            List<Long> result = iterableBind(entities, conn, stmt);
            return convert(result);
        } catch (SQLException e) {
            conn.rollback();
            throw new LightRuntimeException(e);
        } finally {
            conn.close();
        }
    }

    public final long[] insertAndReturnIdsArray(T[] entities) {
        return insertAndReturnIdsArray(Arrays.asList(entities));
    }

    public final Long[] insertAndReturnIdsArrayBox(Collection<? extends T> entities) {
        final ManagedConnection conn = newConnection();
        final PreparedStatement stmt = acquireReturnsGenerateKey(conn);
        try {
            final Long[] result = new Long[entities.size()];
            int index = 0;
            for (T entity : entities) {
                bind(stmt, entity);
                conn.beginTransaction();
                stmt.execute();
                conn.commit();

                ResultSet set = stmt.getGeneratedKeys();
                if (set.next()) {
                    result[index] = set.getLong(1);
                }
                set.close();
                index++;
            }
            return result;
        } catch (SQLException e) {
            conn.rollback();
            throw new LightRuntimeException(e);
        } finally {
            conn.close();
        }
    }

    public final Long[] insertAndReturnIdsArrayBox(Iterable<? extends T> entities) {
        final ManagedConnection conn = newConnection();
        final PreparedStatement stmt = acquireReturnsGenerateKey(conn);
        try {
            List<Long> result = iterableBind(entities, conn, stmt);
            return result.toArray(new Long[0]);
        } catch (SQLException e) {
            conn.rollback();
            throw new LightRuntimeException(e);
        } finally {
            conn.close();
        }
    }

    private List<Long> iterableBind(Iterable<? extends T> entities, ManagedConnection conn, PreparedStatement stmt) throws SQLException {
        List<Long> res = new ArrayList<>();
        int index = 0;
        for (T entity : entities) {
            bind(stmt, entity);
            conn.beginTransaction();
            stmt.execute();
            conn.commit();
            ResultSet set = stmt.getGeneratedKeys();
            if (set.next()) {
                res.add(set.getLong(1));
            }
            index++;
            set.close();
        }
        return res;
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

    public final List<Long> insertAndReturnIdsList(Iterable<? extends T> entities) {
        return new ArrayList<>(Arrays.asList(insertAndReturnIdsArrayBox(entities)));
    }

    private static long[] convert(List<Long> longList) {
        long[] longs = new long[longList.size()];
        int i = 0;
        for (Long l : longList) {
            longs[i++] = l;
        }
        return longs;
    }

}
