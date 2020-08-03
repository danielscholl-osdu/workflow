package org.opengroup.osdu.workflow.consts;

import static org.opengroup.osdu.workflow.consts.DefaultVariable.WORKFLOW_HOST;
import static org.opengroup.osdu.workflow.consts.DefaultVariable.getEnvironmentVariableOrDefaultKey;
import static org.opengroup.osdu.workflow.util.PayloadBuilder.buildContext;
import static org.opengroup.osdu.workflow.util.PayloadBuilder.buildStartWorkflow;

public class TestConstants {

	//Headers consts
	public static final String HEADER_CORRELATION_ID = "correlation-id";
	public static final String HEADER_DATA_PARTITION_ID = "data-partition-id";

	//Api endpoints
	public static final String START_WORKFLOW_API_ENDPOINT = "/startWorkflow";
	public static final String GET_STATUS_API_ENDPOINT = "/getStatus";
	public static final String UPDATE_STATUS_API_ENDPOINT = "/updateStatus";

	public static final String WORKFLOW_TYPE_INGEST = "ingest";

	public static final String WORKFLOW_ID_FIELD = "WorkflowID";
	public static final String STATUS_FIELD = "Status";

	public static final String WORKFLOW_STATUS_TYPE_FINISHED = "finished";
	public static final String WORKFLOW_STATUS_TYPE_SUBMITTED = "submitted";

	public static final String START_WORKFLOW_URL =
			getEnvironmentVariableOrDefaultKey(WORKFLOW_HOST) + START_WORKFLOW_API_ENDPOINT;
	public static final String GET_STATUS_URL =
			getEnvironmentVariableOrDefaultKey(WORKFLOW_HOST) + GET_STATUS_API_ENDPOINT;
	public static final String UPDATE_STATUS_URL =
			getEnvironmentVariableOrDefaultKey(WORKFLOW_HOST) + UPDATE_STATUS_API_ENDPOINT;

	public static final String NON_EXISTING_WORKFLOW_ID = "non-existing-workflow-id";

	public static final String WORKFLOW_ID_NOT_BLANK_MESSAGE = "WorkflowID: must not be blank";
	public static final String WORKFLOW_TYPE_NOT_NULL_MESSAGE = "WorkflowType: must not be null";
	public static final String WORKFLOW_ALREADY_HAS_STATUS_MESSAGE = "Workflow status for workflow id: %s already has status:%s and can not be updated";
	public static final String WORKFLOW_STATUS_NOT_ALLOWED_MESSAGE = "Status: Not allowed workflow status type: SUBMITTED, Should be one of: [RUNNING, FINISHED, FAILED]";

	public static String getValidWorkflowPayload(){
		return buildStartWorkflow(buildContext(), WORKFLOW_TYPE_INGEST);
	}

	public static String getInvalidWorkflowPayload(){
		return buildStartWorkflow(null, null);
	}
}
