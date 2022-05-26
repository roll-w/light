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

package space.lingu.light.log;

import java.util.ArrayList;
import java.util.List;

/**
 * Light内部日志接口。需要使用第三方库日志库的使用此接口进行封装。
 * @author RollW
 */
public interface LightLogger {
    boolean isDebugEnabled();
    void setDebugEnable(boolean isDebug);

    void debug(String message);
    void debug(String message, Throwable throwable);
    void debug(Object message);

    void error(String message);
    void error(String message, Throwable throwable);
    void error(Object message);
    void error(Object message, Throwable throwable);

    void info(String message);
    void info(String message, Throwable throwable);
    void info(Object message);
    void info(Object message, Throwable throwable);


    void trace(String message);
    void trace(String message, Throwable throwable);
    void trace(Throwable throwable);
    void trace(Object message);
    void trace(Object message, Throwable throwable);

    void warn(String message);
    void warn(String message, Throwable throwable);
    void warn(Object message);
    void warn(Object message, Throwable throwable);

    static String formatStackTraces(StackTraceElement[] elements) {
        // 堆栈信息格式化的简单实现
        if (elements == null || elements.length == 0){
            return "Cannot get current method list.";
        }
        List<String> stringList = new ArrayList<>();
        for (StackTraceElement e : elements){
            stringList.add(e.toString());
        }
        StringBuilder result = new StringBuilder();
        int i = 0;
        for (String a : stringList){
            if (i == stringList.size() - 1){
                result.append(a);
            } else {
                result.append(a).append(",\n\t");
            }
            i++;
        }
        return result.toString();
    }
}
