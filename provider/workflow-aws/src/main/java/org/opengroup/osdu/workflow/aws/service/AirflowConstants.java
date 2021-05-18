package org.opengroup.osdu.workflow.aws.service;

public final class AirflowConstants {

    public static final String AIRFLOW_TRIGGER_DAG_ERROR_MESSAGE =
      "Failed to trigger workflow with id %s and name %s";
    public static final String AIRFLOW_DELETE_DAG_ERROR_MESSAGE =
        "Failed to delete workflow with name %s";
    public static final String AIRFLOW_WORKFLOW_RUN_NOT_FOUND =
        "No WorkflowRun executed for Workflow: %s on %s ";
    public final static String AIRFLOW_PAYLOAD_PARAMETER_NAME = "conf";
    public final static String RUN_ID_PARAMETER_NAME = "run_id";
    public final static String EXECUTION_DATE_PARAMETER_NAME = "execution_date";
    public final static String EXECUTION_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ssXXX";
    public static final String FILE_NAME_PREFIX = ".py";
    public final static String AIRFLOW_CONTROLLER_PAYLOAD_PARAMETER_TRIGGER_CONFIGURATION = "_trigger_config";
    public final static String AIRFLOW_CONTROLLER_PAYLOAD_PARAMETER_WORKFLOW_ID = "trigger_dag_id";
    public final static String AIRFLOW_CONTROLLER_PAYLOAD_PARAMETER_WORKFLOW_RUN_ID = "trigger_dag_run_id";
    public final static String AIRFLOW_MICROSECONDS_FLAG = "replace_microseconds";
    public static final String KEY_DAG_CONTENT = "dagContent";
    
}
