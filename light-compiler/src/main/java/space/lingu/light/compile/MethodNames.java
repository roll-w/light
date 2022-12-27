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

package space.lingu.light.compile;

import space.lingu.light.LightDatabase;
import space.lingu.light.handler.SQLHandler;
import space.lingu.light.sql.SQLEscaper;
import space.lingu.light.struct.Table;

import java.sql.ResultSet;
import java.util.List;

/**
 * Method names.
 *
 * @author RollW
 */
public final class MethodNames {
    /**
     * {@link space.lingu.light.struct.StructUtil#findByName(String, List)}
     */
    public static final String sFindByName = "findByName";

    /**
     * {@link space.lingu.light.LightDatabase#registerTable(Table)}
     */
    public static final String sRegisterTable = "registerTable";

    /**
     * {@link LightDatabase#registerAllTables()}
     */
    public static final String sRegisterAllTables = "registerAllTables";

    /**
     * {@link space.lingu.light.util.ResultSetUtil#getColumnIndexSwallow(ResultSet, String, SQLEscaper)}
     * <p>
     * {@link space.lingu.light.util.ResultSetUtil#getColumnIndexSwallow(ResultSet, String)}
     */
    public static final String sGetColumnIndexSwallow = "getColumnIndexSwallow";
    /**
     * {@link space.lingu.light.util.ResultSetUtil#getColumnIndexOrThrow(ResultSet, String)}}
     */
    public static final String sGetColumnIndexOrThrow = "getColumnIndexOrThrow";

    /**
     * {@link space.lingu.light.DaoConnectionGetter#getConnection()}
     */
    public static final String sGetConnection = "getConnection";

    /**
     * {@link SQLHandler#newConnection()}
     */
    public static final String sSQLHandlerNewConnection = "newConnection";

    private MethodNames() {
    }
}
