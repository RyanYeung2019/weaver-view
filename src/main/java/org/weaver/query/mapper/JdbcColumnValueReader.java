package org.weaver.query.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.weaver.config.entity.ViewField;

/**
 * Reads column values using typed JDBC getters so drivers (e.g. Oracle) do not
 * leave proprietary types (e.g. {@code oracle.sql.TIMESTAMP}) in row maps, which
 * Jackson cannot serialize.
 */
final class JdbcColumnValueReader {

	private JdbcColumnValueReader() {
	}

	static Object readValue(ResultSet rs, ViewField field) throws SQLException {
		if (field == null) {
			return null;
		}
		String col = field.getFieldDb();
		int sqlType = field.getSqlType();
		switch (sqlType) {
		case Types.BIT:
		case Types.BOOLEAN: {
			boolean b = rs.getBoolean(col);
			return rs.wasNull() ? null : Boolean.valueOf(b);
		}
		case Types.TINYINT:
		case Types.SMALLINT:
		case Types.INTEGER: {
			int v = rs.getInt(col);
			return rs.wasNull() ? null : Integer.valueOf(v);
		}
		case Types.BIGINT: {
			long v = rs.getLong(col);
			return rs.wasNull() ? null : Long.valueOf(v);
		}
		case Types.FLOAT:
		case Types.REAL: {
			float v = rs.getFloat(col);
			return rs.wasNull() ? null : Float.valueOf(v);
		}
		case Types.DOUBLE: {
			double v = rs.getDouble(col);
			return rs.wasNull() ? null : Double.valueOf(v);
		}
		case Types.NUMERIC:
		case Types.DECIMAL:
			return rs.getBigDecimal(col);
		case Types.CHAR:
		case Types.VARCHAR:
		case Types.LONGVARCHAR:
		case Types.NCHAR:
		case Types.NVARCHAR:
		case Types.LONGNVARCHAR:
			return rs.getString(col);
		case Types.DATE:
			return rs.getDate(col);
		case Types.TIME:
		case Types.TIME_WITH_TIMEZONE:
			return rs.getTime(col);
		case Types.TIMESTAMP:
		case Types.TIMESTAMP_WITH_TIMEZONE:
			return rs.getTimestamp(col);
		case Types.BINARY:
		case Types.VARBINARY:
		case Types.LONGVARBINARY:
			return rs.getBytes(col);
		case Types.CLOB:
		case Types.NCLOB:
			return rs.getString(col);
		case Types.BLOB:
			return rs.getBytes(col);
		default:
			Object o = rs.getObject(col);
			return normalizeNonStandardJdbcValue(rs, col, o);
		}
	}

	private static Object normalizeNonStandardJdbcValue(ResultSet rs, String col, Object o) throws SQLException {
		if (o == null) {
			return null;
		}
		String cn = o.getClass().getName();
		if ("oracle.sql.TIMESTAMP".equals(cn)) {
			return rs.getTimestamp(col);
		}
		if ("oracle.sql.DATE".equals(cn)) {
			return rs.getDate(col);
		}
		if ("oracle.sql.NUMBER".equals(cn)) {
			return rs.getBigDecimal(col);
		}
		if (cn.startsWith("oracle.sql.")) {
			return rs.getString(col);
		}
		return o;
	}
}
