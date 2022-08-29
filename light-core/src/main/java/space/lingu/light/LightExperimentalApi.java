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

import java.lang.annotation.*;

/**
 * 标记实验性API。
 * <p>
 * 在当前的版本中可能无效，在之后的版本中可能会变化或者被移除。
 *
 * @author RollW
 */
@Documented
@Retention(RetentionPolicy.CLASS)
@Target(value = {
        ElementType.METHOD, ElementType.TYPE,
        ElementType.CONSTRUCTOR, ElementType.ANNOTATION_TYPE
})
public @interface LightExperimentalApi {
}