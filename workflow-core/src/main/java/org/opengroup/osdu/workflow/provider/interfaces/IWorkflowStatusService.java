package org.opengroup.osdu.workflow.provider.interfaces;

import com.sun.jersey.api.client.ClientResponse;

public interface IWorkflowStatusService {
  void saveWorkflowStatus(ClientResponse response,
      String workflowStatusId, String workflowNamex, String runId);

}
