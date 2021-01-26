package org.opengroup.osdu.workflow.aws.interfaces;

import java.util.Map;

public interface ISubmitIngestService {
  boolean submitIngest(String dagName, Map<String, Object> data);
}
