package org.whispersystems.textsecuregcm.mybatis;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.net.MalformedURLException;
import java.net.URL;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Maps {@link java.net.URL}s to the underlying database's character types.
 */
public class UrlTypeHandler extends BaseTypeHandler<URL> {
    /**
     * Pass a UUID to the underlying statement's {@link java.sql.PreparedStatement#setObject(int, Object)} method. This
     * relies on the JDBC driver to support UUIDs.
     */
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, URL parameter, JdbcType jdbcType) throws SQLException {
        String urlString = parameter.toString();
        ps.setString(i, urlString);
    }

    @Override
    public URL getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String urlString = rs.getString(columnName);
        return coerceToUrl(urlString);
    }

    @Override
    public URL getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String urlString = rs.getString(columnIndex);
        return coerceToUrl(urlString);
    }

    @Override
    public URL getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String urlString = cs.getString(columnIndex);
        return coerceToUrl(urlString);
    }

    private URL coerceToUrl(String urlString) {
        if (urlString == null)
            return null;
        try {
            return new URL(urlString);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
