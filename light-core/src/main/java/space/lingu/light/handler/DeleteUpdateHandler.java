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

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;

/**
 * 处理删除或更新操作
 * @author RollW
 */
@SuppressWarnings({"unused"})
public abstract class DeleteUpdateHandler<T> extends Handler<T> {

    public DeleteUpdateHandler(LightDatabase database) {
        super(database);
    }

    /**
     * 创建用于删除或更新的SQL语句
     *
     * @return SQL语句
     */
    @Override
    protected abstract String createQuery();

    public final int handle(T entity) {
        final PreparedStatement stmt = acquire();
        try {
            bind(stmt, entity);
            return stmt.executeUpdate();
        } catch (SQLException e) {
            printError(e);
            return 0;
        } finally {
            release(stmt);
        }
    }

    public final int handleMultiple(T[] entities) {
        return handleMultiple(Arrays.asList(entities));
    }

    public final int handleMultiple(Iterable<? extends T> entities) {
        final PreparedStatement stmt = acquire();
        try {
            int count = 0;
            for (T entity : entities) {
                bind(stmt, entity);
                count += stmt.executeUpdate();
            }
            return count;
        } catch (SQLException e) {
            printError(e);
            return 0;
        } finally {
            release(stmt);
        }
    }

    private void printError(Throwable throwable) {
        mDatabase.getLogger().error("An error occurred while delete or update.", throwable);
    }

}
