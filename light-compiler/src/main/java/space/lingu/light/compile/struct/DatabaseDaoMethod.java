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

package space.lingu.light.compile.struct;

import javax.lang.model.element.ExecutableElement;

/**
 * 在Database定义的返回一个Dao的方法
 * @author RollW
 */
public class DatabaseDaoMethod {
    private ExecutableElement element;
    private Dao dao;

    public DatabaseDaoMethod() {
    }

    public ExecutableElement getElement() {
        return element;
    }

    public DatabaseDaoMethod setElement(ExecutableElement element) {
        this.element = element;
        return this;
    }

    public Dao getDao() {
        return dao;
    }

    public DatabaseDaoMethod setDao(Dao dao) {
        this.dao = dao;
        return this;
    }
}
