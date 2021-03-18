package org.opengroup.osdu.workflow.provider.interfaces;

public interface IWorkflowTasksSharingRepository {
  String getSignedUrl(String workflowId, String runId);
}
