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

/**
 * @author RollW
 */
public final class Warnings {
    public static final String INFO =
            "\n\nThis warning is intended to allow you to recheck the correctness of your annotations. " +
                    "To ignore this warning permanently, use the @LightIgnore annotation.";

    public static final String FIELD_NOT_ANNOTATED =
            "There is a field not covered by @DataColumn annotation." + INFO;

    public static final String PRIMARY_KEY_NOT_FOUND =
            "Not set a primary key in the table." + INFO;

    public static final String CANNOT_APPLY_TO_STATIC_FIELD =
            "" + INFO;


}
