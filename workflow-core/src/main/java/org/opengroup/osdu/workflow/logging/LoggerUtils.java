package org.opengroup.osdu.workflow.logging;

public class LoggerUtils {

	public static final int STRING_LENGTH_FOR_LOGGING = 1000;

	public static String getTruncatedData(String data) {
		return data.length() < STRING_LENGTH_FOR_LOGGING
				? data
				: data.substring(0, STRING_LENGTH_FOR_LOGGING) + "... (truncated)";
	}
}
