package org.opengroup.osdu.workflow.provider.azure.consts;

public class WorkflowRunConstants {
  // Default number of workflow runs to send in the response when no limit is specified for get all run instances
  public static final int WORKFLOW_RUNS_LIMIT = 50;
  // As per the api spec, prefix for workflow run id cannot contain the word "backfill".
  public static final String INVALID_WORKFLOW_RUN_PREFIX = "backfill";
}
