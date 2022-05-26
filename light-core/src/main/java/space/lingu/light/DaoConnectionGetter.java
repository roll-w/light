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

/**
 * 在{@link Dao}类中使用。
 * 如果需要在Dao类运行中获取{@link SharedConnection}或{@link java.sql.Connection}，则扩展此接口。
 * @author RollW
 */
public interface DaoConnectionGetter {
    default SharedConnection getConnection() {
        throw new LightRuntimeException("Unimplemented method.");
    }
}
