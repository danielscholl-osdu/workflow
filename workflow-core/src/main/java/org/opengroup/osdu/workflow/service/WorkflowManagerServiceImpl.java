package org.opengroup.osdu.workflow.service;

import java.util.Collections;
import java.util.List;

import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.workflow.logging.AuditLogger;
import org.opengroup.osdu.workflow.model.CreateWorkflowRequest;
import org.opengroup.osdu.workflow.model.WorkflowEngineRequest;
import org.opengroup.osdu.workflow.model.WorkflowMetadata;
import org.opengroup.osdu.workflow.provider.interfaces.IWorkflowEngineService;
import org.opengroup.osdu.workflow.provider.interfaces.IWorkflowManagerService;
import org.opengroup.osdu.workflow.provider.interfaces.IWorkflowMetadataRepository;
import org.opengroup.osdu.workflow.provider.interfaces.IWorkflowRunService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class WorkflowManagerServiceImpl implements IWorkflowManagerService {
  private static final long START_VERSION = 1;

  @Autowired
  private DpsHeaders dpsHeaders;

  @Autowired
  private IWorkflowMetadataRepository workflowMetadataRepository;

  @Autowired
  private IWorkflowEngineService workflowEngineService;

  @Autowired
  private IWorkflowRunService workflowRunService;

  @Autowired
  private AuditLogger auditLogger;

  @Override
  public WorkflowMetadata createWorkflow(final CreateWorkflowRequest request) {
    final WorkflowMetadata workflowMetadata = getWorkflowMetadata(request, dpsHeaders.getUserEmail());
    final WorkflowMetadata savedMetadata = workflowMetadataRepository.createWorkflow(workflowMetadata);
    final WorkflowEngineRequest rq = new WorkflowEngineRequest(workflowMetadata.getWorkflowName());
    workflowEngineService.createWorkflow(rq, request.getRegistrationInstructions());
    auditLogger.workflowCreateEvent(Collections.singletonList(savedMetadata.toString()));
    return savedMetadata;
  }

  @Override
  public WorkflowMetadata getWorkflowByName(final String workflowName) {
    return workflowMetadataRepository.getWorkflow(workflowName);
  }

  @Override
  public void deleteWorkflow(String workflowName) {
    final WorkflowMetadata workflowMetadata = workflowMetadataRepository.getWorkflow(workflowName);
    workflowRunService.deleteWorkflowRunsByWorkflowName(workflowName);
    WorkflowEngineRequest rq = new WorkflowEngineRequest(workflowName, workflowMetadata.isDeployedThroughWorkflowService());
    workflowEngineService.deleteWorkflow(rq);
    workflowMetadataRepository.deleteWorkflow(workflowName);
    auditLogger.workflowDeleteEvent(Collections.singletonList(workflowName));
  }

  @Override
  public List<WorkflowMetadata> getAllWorkflowForTenant(String prefix) {
    return workflowMetadataRepository.getAllWorkflowForTenant(prefix);
  }

  private WorkflowMetadata getWorkflowMetadata(final CreateWorkflowRequest request,
                                               final String createdBy) {
    return WorkflowMetadata.builder()
        .description(request.getDescription())
        .createdBy(createdBy)
        .creationTimestamp(System.currentTimeMillis())
        .version(WorkflowManagerServiceImpl.START_VERSION)
        .registrationInstructions(request.getRegistrationInstructions())
        .workflowName(request.getWorkflowName())
        .build();
  }
}
