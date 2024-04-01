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

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * @author RollW
 */
public final class DateTimeUtils {
    // --- raws
    public static Date raw(Date date) {
        return date;
    }

    public static Time raw(Time time) {
        return time;
    }

    public static Timestamp raw(Timestamp timestamp) {
        return timestamp;
    }

    // --- long/Long

    public static Timestamp convertLong(Long time) {
        if (time == null) {
            return null;
        }
        return new Timestamp(time);
    }

    public static Timestamp convertLong(long time) {
        return new Timestamp(time);
    }

    public static Long convertLongObject(Timestamp timestamp) {
        if (timestamp == null) {
            return null;
        }
        return timestamp.getTime();
    }

    public static long convertLong(Timestamp timestamp) {
        if (timestamp == null) {
            return 0;
        }
        return timestamp.getTime();
    }

    // --- java.util.Date

    public static Date convertDate(java.util.Date date) {
        if (date == null) {
            return null;
        }
        return new Date(date.getTime());
    }

    public static java.util.Date convertDate(Date date) {
        if (date == null) {
            return null;
        }
        return new java.util.Date(date.getTime());
    }

    public static Timestamp convertInstant(Instant instant) {
        if (instant == null) {
            return null;
        }
        return Timestamp.from(instant);
    }

    public static Instant convertInstant(Timestamp timestamp) {
        if (timestamp == null) {
            return null;
        }
        return timestamp.toInstant();
    }

    // --- LocalDate

    public static Date convertLocalDate(LocalDate date) {
        if (date == null) {
            return null;
        }
        return Date.valueOf(date);
    }

    public static LocalDate convertLocalDate(Date date) {
        if (date == null) {
            return null;
        }
        return date.toLocalDate();
    }

    // --- LocalTime

    public static Time convertLocalTime(LocalTime time) {
        if (time == null) {
            return null;
        }
        return Time.valueOf(time);
    }

    public static LocalTime convertLocalTime(Time time) {
        if (time == null) {
            return null;
        }
        return time.toLocalTime();
    }

    // --- LocalDateTime

    public static Timestamp convertLocalDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return Timestamp.valueOf(dateTime);
    }

    public static LocalDateTime convertLocalDateTime(Timestamp timestamp) {
        if (timestamp == null) {
            return null;
        }
        return timestamp.toLocalDateTime();
    }

    private DateTimeUtils() {
    }
}
