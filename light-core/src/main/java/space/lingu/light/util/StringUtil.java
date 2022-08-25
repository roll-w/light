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

package space.lingu.light.util;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Locale;

public class StringUtil {
    /**
     * 如果为字符串是否为空串
     * @param obj 对象
     * @return 如果为字符串是否为空串
     */
    public static boolean isEmpty(Object obj) {
        if (null == obj) {
            return true;
        } else if (obj instanceof CharSequence) {
            return 0 == ((CharSequence) obj).length();
        }
        return false;
    }

    public static boolean isEmpty(String s) {
        if (s == null) {
            return true;
        }
        return s.isEmpty();
    }

    public static StringBuilder createStringBuilder() {
        return new StringBuilder();
    }

    /**
     * 将对象转为字符串
     * @param obj 对象
     * @param charset 字符集
     * @return 字符串
     */
    public static String toString(Object obj, Charset charset) {
        if (null == obj) {
            return null;
        }

        if (obj instanceof String) {
            return (String) obj;
        } else if (obj instanceof byte[]) {
            return toString((byte[]) obj, charset);
        } else if (obj instanceof Byte[]) {
            return toString((Byte[]) obj, charset);
        } else if (obj instanceof ByteBuffer) {
            return toString((ByteBuffer) obj, charset);
        } else if (ArrayUtil.isArray(obj)) {
            return ArrayUtil.toString(obj);
        }

        return obj.toString();
    }

    /**
     * 解码字节码
     * @param data    字符串
     * @param charset 字符集
     * @return 解码后的字符串
     */
    public static String toString(byte[] data, Charset charset) {
        if (data == null) {
            return null;
        }

        if (null == charset) {
            return new String(data);
        }
        return new String(data, charset);
    }

    /**
     * 解码字节码
     * @param data 字符串
     * @param charset 字符集
     * @return 解码后的字符串
     */
    public static String toString(Byte[] data, Charset charset) {
        if (data == null) {
            return null;
        }

        byte[] bytes = new byte[data.length];
        Byte dataByte;
        for (int i = 0; i < data.length; i++) {
            dataByte = data[i];
            bytes[i] = (null == dataByte) ? -1 : dataByte;
        }

        return toString(bytes, charset);
    }


    /**
     * 将编码的byteBuffer数据转换为字符串
     * @param data 数据
     * @param charset 字符集
     * @return 字符串
     */
    public static String toString(ByteBuffer data, Charset charset) {
        if (null == charset) {
            charset = Charset.defaultCharset();
        }
        return charset.decode(data).toString();
    }

    /**
     * 调用对象的toString方法，null会返回"null"
     * @param obj 对象
     * @return 字符串
     * @see String#valueOf(Object)
     */
    public static String toString(Object obj) {
        return String.valueOf(obj);
    }

    public static String firstUpperCase(String s) {
        if (isEmpty(s)) {
            return "";
        }
        if (Character.isUpperCase(s.charAt(0))) {
            return s;
        }
        if (s.length() == 1) {
            return s.toUpperCase(Locale.US);
        }
        return s.substring(0, 1).toUpperCase(Locale.US) + s.substring(1);
    }

    public static String firstLowerCase(String s) {
        if (isEmpty(s)) {
            return "";
        }
        if (Character.isLowerCase(s.charAt(0))) {
            return s;
        }
        if (s.length() == 1) {
            return s.toLowerCase(Locale.US);
        }
        return s.substring(0, 1).toLowerCase(Locale.US) + s.substring(1);
    }
}
