package org.opengroup.osdu.azure.workflow.framework.util;

import lombok.ToString;
import lombok.extern.java.Log;

import java.util.HashMap;
import java.util.Map;

import static org.opengroup.osdu.workflow.consts.TestConstants.HEADER_DATA_PARTITION_ID;

@Log
@ToString
public abstract class HTTPClient extends org.opengroup.osdu.workflow.util.HTTPClient {
	public Map<String, String> getCommonHeaderWithoutPartition() {
		Map<String, String> headers = new HashMap<>();
		headers.put(HEADER_DATA_PARTITION_ID, "");
		return headers;
	}
}
