package org.whispersystems.textsecuregcm.mybatis;

import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;

import javax.annotation.Nullable;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

/**
 * Maps Optional-like types to JDBC.
 * <p>
 * Subclasses must ensure that the following invariants hold:
 * <pre>
 *     // given Object rawValue = ...;
 *     T optional = makeOptional(rawValue);
 *     assert rawValue == unpackOptional(optional);
 *
 *     // given T optional = ...;
 *     Object rawValue = unpackOptional(optional);
 *     T newOptional = makeOptional(rawValue);
 *     assert newOptional.equals(optional);
 * </pre>
 *
 * @param <T>
 *         The optional-like type to map.
 */
public abstract class OptionalTypeHandler<T> implements TypeHandler<T> {
    @Override
    public void setParameter(
            PreparedStatement ps, int i, @Nullable T parameter, @Nullable JdbcType jdbcType) throws SQLException {
        int typeCode = jdbcType == null ? Types.OTHER : jdbcType.TYPE_CODE;

        if (parameter == null) {
            ps.setNull(i, typeCode);
            return;
        }

        Object rawParameter = unpackOptional(parameter);
        if (rawParameter == null) {
            ps.setNull(i, typeCode);
            return;
        }

        assert rawParameter != null;
        ps.setObject(i, rawParameter, typeCode);
    }

    @Override
    public T getResult(ResultSet rs, String columnName) throws SQLException {
        Object rawValue = rs.getObject(columnName);
        return makeOptional(rawValue);
    }

    @Override
    public T getResult(ResultSet rs, int columnIndex) throws SQLException {
        Object rawValue = rs.getObject(columnIndex);
        return makeOptional(rawValue);
    }

    @Override
    public T getResult(CallableStatement cs, int columnIndex) throws SQLException {
        Object rawValue = cs.getObject(columnIndex);
        return makeOptional(rawValue);
    }

    /**
     * Unpack an Optional-like value to its contained value, or <code>null</code>. Subclasses must supply this: when
     * this method returns <code>null</code>, this TypeHandler will store an SQL <code>NULL</code> to the database. When
     * this method returns a non-null value, this TypeHandler will store it to the database using the appropriate
     * setObject method.
     *
     * @param parameter
     *         a non-null Optional-like value.
     * @return the unwrapped value, or <code>null</code>.
     */
    @Nullable
    protected abstract Object unpackOptional(T parameter);

    /**
     * Wrap a database value in an Optional-like value. Subclasses must supply this: when passed <code>null</code> (read
     * from an SQL <code>NULL</code> value), this must return a non-null Optional-like value representing no value, such
     * as {@code Optional.absent()}. When passed a non-null value, this must wrap the value in an Optional-like wrapper,
     * such as {@code Optional.of(value)}.
     *
     * @param rawValue
     *         the raw value read from the database.
     * @return an Optional-like value wrapping <var>rawValue</var>.
     */
    protected abstract T makeOptional(@Nullable Object rawValue);
}

