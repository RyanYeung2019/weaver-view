package org.weaver.config;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.util.Assert;
import org.weaver.config.entity.ViewField;

/**
 *
 * @author <a href="mailto:30808333@qq.com">Ryan Yeung</a>
 * 
 */

class ViewUtils {
	/**
	 * Set of characters that qualify as parameter separators, indicating that a
	 * parameter name in an SQL String has ended.
	 */
	private static final String PARAMETER_SEPARATORS = "\"':&,;()|=+-*%/\\<>^";
	private static final boolean[] separatorIndex = new boolean[128];

	
	public static Object stubObjVal(String type) {
		Object val = "0";
		if (ViewField.FIELDTYPE_BOOLEAN.equals(type)) {
			val = Boolean.valueOf(false);
		}
		if (ViewField.FIELDTYPE_DATE.equals(type) || ViewField.FIELDTYPE_TIME.equals(type)
				|| ViewField.FIELDTYPE_DATETIME.equals(type)) {
			val = new Date();
		}
		if (ViewField.FIELDTYPE_NUMBER.equals(type)) {
			if (val.toString().contains(".")) {
				val = Double.parseDouble("0");
			} else {
				val = Long.parseLong("0");
			}
		}
		return val;
	}
	
	
	public static List<String> parseSqlStatement(String sql) {
		Assert.notNull(sql, "SQL must not be null");
		List<String> parameterList = new ArrayList<>();

		char[] statement = sql.toCharArray();

		int i = 0;
		while (i < statement.length) {
			int skipToPosition = i;
			while (i < statement.length) {
				skipToPosition = skipCommentsAndQuotes(statement, i);
				if (i == skipToPosition) {
					break;
				} else {
					i = skipToPosition;
				}
			}
			if (i >= statement.length) {
				break;
			}
			char c = statement[i];
			if (c == ':' || c == '&') {
				int j = i + 1;
				if (c == ':' && j < statement.length && statement[j] == ':') {
					// Postgres-style "::" casting operator should be skipped
					i = i + 2;
					continue;
				}
				String parameter = null;
				if (c == ':' && j < statement.length && statement[j] == '{') {
					// :{x} style parameter
					while (statement[j] != '}') {
						j++;
						if (j >= statement.length) {
							throw new InvalidDataAccessApiUsageException(
									"Non-terminated named parameter declaration at position " + i + " in statement: "
											+ sql);
						}
						if (statement[j] == ':' || statement[j] == '{') {
							throw new InvalidDataAccessApiUsageException("Parameter name contains invalid character '"
									+ statement[j] + "' at position " + i + " in statement: " + sql);
						}
					}
					if (j - i > 2) {
						parameter = sql.substring(i + 2, j);
						if (!parameterList.contains(parameter))
							parameterList.add(parameter);
					}
					j++;
				} else {
					boolean paramWithSquareBrackets = false;
					while (j < statement.length) {
						c = statement[j];
						if (isParameterSeparator(c)) {
							break;
						}
						if (c == '[') {
							paramWithSquareBrackets = true;
						} else if (c == ']') {
							if (!paramWithSquareBrackets) {
								break;
							}
							paramWithSquareBrackets = false;
						}
						j++;
					}
					if (j - i > 1) {
						parameter = sql.substring(i + 1, j);
						if (!parameterList.contains(parameter))
							parameterList.add(parameter);
					}
				}
				i = j - 1;
			} else {
				if (c == '\\') {
					int j = i + 1;
					if (j < statement.length && statement[j] == ':') {
						i = i + 2;
						continue;
					}
				}
				if (c == '?') {
					int j = i + 1;
					if (j < statement.length && (statement[j] == '?' || statement[j] == '|' || statement[j] == '&')) {
						// Postgres-style "??", "?|", "?&" operator should be skipped
						i = i + 2;
						continue;
					}
				}
			}
			i++;
		}
		return parameterList;
	}
	


	/**
	 * Set of characters that qualify as comment or quote starting characters.
	 */
	private static final String[] START_SKIP = { "'", "\"", "--", "/*", "`" };
	static {
		for (char c : PARAMETER_SEPARATORS.toCharArray()) {
			separatorIndex[c] = true;
		}
	}
	private static boolean isParameterSeparator(char c) {
		return (c < 128 && separatorIndex[c]) || Character.isWhitespace(c);
	}



	/**
	 * Set of characters that are the corresponding comment or quote ending
	 * characters.
	 */
	private static final String[] STOP_SKIP = { "'", "\"", "\n", "*/", "`" };

	private static int skipCommentsAndQuotes(char[] statement, int position) {
		for (int i = 0; i < START_SKIP.length; i++) {
			if (statement[position] == START_SKIP[i].charAt(0)) {
				boolean match = true;
				for (int j = 1; j < START_SKIP[i].length(); j++) {
					if (statement[position + j] != START_SKIP[i].charAt(j)) {
						match = false;
						break;
					}
				}
				if (match) {
					int offset = START_SKIP[i].length();
					for (int m = position + offset; m < statement.length; m++) {
						if (statement[m] == STOP_SKIP[i].charAt(0)) {
							boolean endMatch = true;
							int endPos = m;
							for (int n = 1; n < STOP_SKIP[i].length(); n++) {
								if (m + n >= statement.length) {
									// last comment not closed properly
									return statement.length;
								}
								if (statement[m + n] != STOP_SKIP[i].charAt(n)) {
									endMatch = false;
									break;
								}
								endPos = m + n;
							}
							if (endMatch) {
								// found character sequence ending comment or quote
								return endPos + 1;
							}
						}
					}
					// character sequence ending comment or quote not found
					return statement.length;
				}
			}
		}
		return position;
	}

}
