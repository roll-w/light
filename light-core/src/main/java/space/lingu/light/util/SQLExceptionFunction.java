package space.lingu.light.util;

import space.lingu.light.LightRuntimeException;

import java.sql.SQLException;
import java.util.function.Function;

/**
 * @author RollW
 */
public interface SQLExceptionFunction<T, R> {
    R apply(T t) throws SQLException;

    default Function<T, R> unwrap() {
        return t -> {
            try {
                return apply(t);
            } catch (SQLException e) {
                throw new LightRuntimeException(e);
            }
        };
    }
}
