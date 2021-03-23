package org.whispersystems.textsecuregcm.mybatis;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

/**
 * Maps {@link java.util.UUID} to the underlying database's <code>UUID</code> type. This only works on databases which
 * support UUIDs: only PostgreSQL is supported. UUIDs will be retrieved via the {@link
 * java.sql.ResultSet#getObject(String) getObject} methods, and only a cast conversion will be applied. This type
 * handler will return <code>null</code> for SQL <code>NULL</code> values.
 */
public class UuidTypeHandler extends BaseTypeHandler<UUID> {
    /**
     * Pass a UUID to the underlying statement's {@link java.sql.PreparedStatement#setObject(int, Object)} method. This
     * relies on the JDBC driver to support UUIDs.
     */
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, UUID parameter, JdbcType jdbcType) throws SQLException {
        ps.setObject(i, parameter);
    }

    @Override
    public UUID getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return (UUID) rs.getObject(columnName);
    }

    @Override
    public UUID getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return (UUID) rs.getObject(columnIndex);
    }

    @Override
    public UUID getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return (UUID) cs.getObject(columnIndex);
    }
}
