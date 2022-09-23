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

import java.util.List;

/**
 * database info create by light.
 * <p>
 * Also, this is also used as an example of annotation.
 *
 * @author RollW
 */
@DataTable(tableName = "__light_info_table")
@LightExperimentalApi
public class LightInfo {
    public static final String sTableName = "__light_info_table";
    public static final String KEY_HASH = "hash";
    public static final String KEY_VERSION = "version";

    @PrimaryKey
    @DataColumn(configuration = {
            @LightConfiguration(
                    key = LightConfiguration.KEY_VARCHAR_LENGTH,
                    value = "100")
    })
    public final String k;

    @DataColumn
    public final String v;

    public LightInfo(String k, String v) {
        this.k = k;
        this.v = v;
    }

    @Dao
    public abstract static class LightInfoDao {
        @Insert(onConflict = OnConflictStrategy.REPLACE)
        protected abstract void insert(LightInfo... infos);

        @Update(onConflict = OnConflictStrategy.REPLACE)
        protected abstract void update(LightInfo... infos);

        @Query("SELECT * FROM __light_info_table")
        protected abstract List<LightInfo> get();

        protected void updateVersion(int version) {
            // TODO
        }

        protected boolean checkAndUpdate(String hash, String version) {
            // TODO
            return false;
        }
    }
}
