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
import space.lingu.light.util.SQLExceptionFunction;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
                conn.beginTransaction();
                for (T entity : entities) {
                    stmt.clearParameters();
                    bind(stmt, entity);
                    stmt.execute();
                }
                conn.commit();
            }

        } catch (SQLException e) {
            conn.rollback();
            throw new LightRuntimeException(e);
        } finally {
            conn.close();
        }
    }

    public final long insertAndReturnLong(T entity) {
        Long result = insertAndReturn(
                entity,
                resultSet -> resultSet.getLong(1)
        );
        return result == null ? -1 : result;
    }

    public final int insertAndReturnInt(T entity) {
        Integer result = insertAndReturn(
                entity,
                resultSet -> resultSet.getInt(1)
        );
        return result == null ? -1 : result;
    }

    private <I> I insertAndReturn(
            T entity,
            SQLExceptionFunction<ResultSet, I> provider) {
        final ManagedConnection conn = newConnection();
        final PreparedStatement stmt = acquireReturnsGenerateKey(conn);
        try {
            bind(stmt, entity);
            conn.beginTransaction();
            stmt.execute();
            conn.commit();

            ResultSet set = stmt.getGeneratedKeys();
            if (set.next()) {
                return provider.apply(set);
            }
            set.close();
        } catch (SQLException e) {
            conn.rollback();
            throw new LightRuntimeException(e);
        } finally {
            conn.close();
        }
        return null;
    }

    public final long[] insertAndReturnLongArray(Collection<? extends T> entities) {
        final ManagedConnection conn = newConnection();
        final PreparedStatement stmt = acquireReturnsGenerateKey(conn);
        try {
            final long[] result = new long[entities.size()];
            int index = 0;
            conn.beginTransaction();
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
            conn.commit();
            return result;
        } catch (SQLException e) {
            conn.rollback();
            throw new LightRuntimeException(e);
        } finally {
            conn.close();
        }
    }

    public final int[] insertAndReturnIntArray(Collection<? extends T> entities) {
        final ManagedConnection conn = newConnection();
        final PreparedStatement stmt = acquireReturnsGenerateKey(conn);
        try {
            final int[] result = new int[entities.size()];
            int index = 0;
            conn.beginTransaction();
            for (T entity : entities) {
                bind(stmt, entity);
                stmt.execute();
                ResultSet set = stmt.getGeneratedKeys();
                if (set.next()) {
                    result[index] = set.getInt(1);
                }
                index++;
                set.close();
            }
            conn.commit();
            return result;
        } catch (SQLException e) {
            conn.rollback();
            throw new LightRuntimeException(e);
        } finally {
            conn.close();
        }
    }

    public final long[] insertAndReturnLongArray(Iterable<? extends T> entities) {
        final ManagedConnection conn = newConnection();
        final PreparedStatement stmt = acquireReturnsGenerateKey(conn);
        try {
            List<Long> result = insertIterable(
                    entities, conn, stmt,
                    resultSet -> resultSet.getLong(1)
            );
            return convertLong(result);
        } catch (SQLException e) {
            conn.rollback();
            throw new LightRuntimeException(e);
        } finally {
            conn.close();
        }
    }

    public final int[] insertAndReturnIntArray(Iterable<? extends T> entities) {
        final ManagedConnection conn = newConnection();
        final PreparedStatement stmt = acquireReturnsGenerateKey(conn);
        try {
            List<Integer> result = insertIterable(
                    entities, conn, stmt,
                    resultSet -> resultSet.getInt(1)
            );
            return convertInteger(result);
        } catch (SQLException e) {
            conn.rollback();
            throw new LightRuntimeException(e);
        } finally {
            conn.close();
        }
    }

    private static int[] copyFrom(long[] longArray) {
        int[] res = new int[longArray.length];
        for (int i = 0; i < longArray.length; i++) {
            res[i] = (int) longArray[i];
        }
        return res;
    }

    public final long[] insertAndReturnLongArray(T[] entities) {
        return insertAndReturnLongArray(Arrays.asList(entities));
    }

    public final int[] insertAndReturnIntArray(T[] entities) {
        return insertAndReturnIntArray(Arrays.asList(entities));
    }

    public final Long[] insertAndReturnLongArrayBox(Collection<? extends T> entities) {
        long[] res = insertAndReturnLongArray(entities);
        Long[] boxedValues = new Long[res.length];
        for (int i = 0; i < res.length; i++) {
            boxedValues[i] = res[i];
        }
        return boxedValues;
    }

    public final Integer[] insertAndReturnIntArrayBox(Collection<? extends T> entities) {
        int[] res = insertAndReturnIntArray(entities);
        Integer[] boxedValues = new Integer[res.length];
        for (int i = 0; i < res.length; i++) {
            boxedValues[i] = res[i];
        }
        return boxedValues;
    }

    public final Long[] insertAndReturnLongArrayBox(Iterable<? extends T> entities) {
        final ManagedConnection conn = newConnection();
        final PreparedStatement stmt = acquireReturnsGenerateKey(conn);
        try {
            List<Long> result = insertIterable(
                    entities, conn, stmt,
                    resultSet -> resultSet.getLong(1)
            );
            return result.toArray(new Long[0]);
        } catch (SQLException e) {
            conn.rollback();
            throw new LightRuntimeException(e);
        } finally {
            conn.close();
        }
    }

    public final Integer[] insertAndReturnIntArrayBox(Iterable<? extends T> entities) {
        final ManagedConnection conn = newConnection();
        final PreparedStatement stmt = acquireReturnsGenerateKey(conn);
        try {
            List<Integer> result = insertIterable(
                    entities, conn, stmt,
                    resultSet -> resultSet.getInt(1)
            );
            return result.toArray(new Integer[0]);
        } catch (SQLException e) {
            conn.rollback();
            throw new LightRuntimeException(e);
        } finally {
            conn.close();
        }
    }

    private <I> List<I> insertIterable(
            Iterable<? extends T> entities,
            ManagedConnection conn,
            PreparedStatement stmt,
            SQLExceptionFunction<ResultSet, I> provider
    ) throws SQLException {
        List<I> res = new ArrayList<>();
        conn.beginTransaction();
        for (T entity : entities) {
            bind(stmt, entity);
            stmt.execute();
            ResultSet set = stmt.getGeneratedKeys();
            if (set.next()) {
                I value = provider.apply(set);
                res.add(value);
            }
            set.close();
        }
        conn.commit();
        return res;
    }

    public final Long[] insertAndReturnLongArrayBox(T[] entities) {
        return insertAndReturnLongArrayBox(Arrays.asList(entities));
    }

    public final List<Long> insertAndReturnLongList(T[] entities) {
        return Arrays.asList(insertAndReturnLongArrayBox(entities));
    }

    public final List<Long> insertAndReturnLongList(Collection<? extends T> entities) {
        return Arrays.asList(insertAndReturnLongArrayBox(entities));
    }

    public final List<Long> insertAndReturnLongList(Iterable<? extends T> entities) {
        return Arrays.asList(insertAndReturnLongArrayBox(entities));
    }

    public final Integer[] insertAndReturnIntArrayBox(T[] entities) {
        return insertAndReturnIntArrayBox(Arrays.asList(entities));
    }

    public final List<Integer> insertAndReturnIntList(T[] entities) {
        return Arrays.asList(insertAndReturnIntArrayBox(entities));
    }

    public final List<Integer> insertAndReturnIntList(Collection<? extends T> entities) {
        return Arrays.asList(insertAndReturnIntArrayBox(entities));
    }

    public final List<Integer> insertAndReturnIntList(Iterable<? extends T> entities) {
        return Arrays.asList(insertAndReturnIntArrayBox(entities));
    }

    private static long[] convertLong(List<Long> longList) {
        long[] longs = new long[longList.size()];
        int i = 0;
        for (Long l : longList) {
            longs[i++] = l;
        }
        return longs;
    }

    private static int[] convertInteger(List<Integer> integers) {
        int[] ints = new int[integers.size()];
        int i = 0;
        for (Integer l : integers) {
            ints[i++] = l;
        }
        return ints;
    }
}
