package org.whispersystems.textsecuregcm.mybatis;

import java.util.Optional;

/**
 * Maps Java 8 {@link java.util.Optional}s to database values. This mapping uses {@link
 * java.sql.ResultSet#getObject(int) getObject} and {@link java.sql.PreparedStatement#setObject(int, Object, int)
 * setObject} to store the underlying value, which means the underlying JDBC driver needs to have a useful
 * implementation of those methods.
 */
public class Java8OptionalTypeHandler extends OptionalTypeHandler<Optional<Object>> {
    @Override
    protected Object unpackOptional(Optional<Object> parameter) {
        return parameter.orElse(null);
    }

    @Override
    protected Optional<Object> makeOptional(Object rawValue) {
        if (rawValue == null)
            return Optional.empty();
        return Optional.of(rawValue);
    }
}
