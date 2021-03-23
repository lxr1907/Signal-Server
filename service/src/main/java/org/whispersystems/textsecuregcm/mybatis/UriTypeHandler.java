package org.whispersystems.textsecuregcm.mybatis;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Maps {@link java.net.URI}s to the underlying database's character types.
 */
public class UriTypeHandler extends BaseTypeHandler<URI> {
    /**
     * Pass a UUID to the underlying statement's {@link java.sql.PreparedStatement#setObject(int, Object)} method. This
     * relies on the JDBC driver to support UUIDs.
     */
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, URI parameter, JdbcType jdbcType) throws SQLException {
        String urlString = parameter.toString();
        ps.setString(i, urlString);
    }

    @Override
    public URI getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String urlString = rs.getString(columnName);
        return coerceToUri(urlString);
    }

    @Override
    public URI getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String urlString = rs.getString(columnIndex);
        return coerceToUri(urlString);
    }

    @Override
    public URI getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String urlString = cs.getString(columnIndex);
        return coerceToUri(urlString);
    }

    private URI coerceToUri(String urlString) {
        if (urlString == null)
            return null;
        try {
            return new URI(urlString);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
    }
}