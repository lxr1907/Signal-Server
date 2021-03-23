package org.whispersystems.textsecuregcm.mybatis;

import com.google.common.base.Optional;

/**
 * Maps Guava {@link Optional}s to database values. This mapping uses {@link
 * java.sql.ResultSet#getObject(int) getObject} and {@link java.sql.PreparedStatement#setObject(int, Object, int)
 * setObject} to store the underlying value, which means the underlying JDBC driver needs to have a useful
 * implementation of those methods.
 */
public class GuavaOptionalTypeHandler extends OptionalTypeHandler<Optional<Object>> {
    @Override
    protected Object unpackOptional(Optional<Object> parameter) {
        return parameter.orNull();
    }

    @Override
    protected Optional<Object> makeOptional(Object rawValue) {
        if (rawValue == null)
            return Optional.absent();
        return Optional.of(rawValue);
    }
}