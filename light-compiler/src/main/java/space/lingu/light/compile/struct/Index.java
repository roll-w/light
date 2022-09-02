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

import space.lingu.light.Order;

import java.util.List;

/**
 * 索引
 * @author RollW
 */
public class Index {
    public static final String DEFAULT_PREFIX = "index_";

    private final String name;
    private final boolean unique;
    private final Field.Fields fields;
    private final List<Order> orders;

    public Index(String name, boolean unique,
                 Field.Fields fields, List<Order> orders) {
        this.name = name;
        this.unique = unique;
        this.fields = fields;
        this.orders = orders;
    }

    public String getName() {
        return name;
    }

    public boolean isUnique() {
        return unique;
    }

    public Field.Fields getFields() {
        return fields;
    }

    public List<Order> getOrders() {
        return orders;
    }
}
