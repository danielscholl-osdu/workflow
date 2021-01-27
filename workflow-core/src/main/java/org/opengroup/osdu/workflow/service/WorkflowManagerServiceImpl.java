package org.opengroup.osdu.workflow.service;

import java.util.List;

import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.workflow.model.CreateWorkflowRequest;
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

  @Override
  public WorkflowMetadata createWorkflow(final CreateWorkflowRequest request) {
    final WorkflowMetadata workflowMetadata = getWorkflowMetadata(request, dpsHeaders.getUserEmail(), START_VERSION);
    final WorkflowMetadata savedMetadata = workflowMetadataRepository.createWorkflow(workflowMetadata);
    workflowEngineService.createWorkflow(
        request.getRegistrationInstructions(), workflowMetadata.getWorkflowName());
    return savedMetadata;
  }

  @Override
  public WorkflowMetadata getWorkflowByName(final String workflowName) {
    return workflowMetadataRepository.getWorkflow(workflowName);
  }

  @Override
  public void deleteWorkflow(String workflowName) {
    workflowRunService.deleteWorkflowRunsByWorkflowName(workflowName);
    workflowEngineService.deleteWorkflow(workflowName);
    workflowMetadataRepository.deleteWorkflow(workflowName);
  }

  @Override
  public List<WorkflowMetadata> getAllWorkflowForTenant(String prefix) {
    return workflowMetadataRepository.getAllWorkflowForTenant(prefix);
  }

  private WorkflowMetadata getWorkflowMetadata(final CreateWorkflowRequest request,
                                               final String createdBy, final Long version) {
    return WorkflowMetadata.builder()
        .description(request.getDescription())
        .createdBy(createdBy)
        .creationTimestamp(System.currentTimeMillis())
        .version(version)
        .registrationInstructions(request.getRegistrationInstructions())
        .workflowName(request.getWorkflowName())
        .build();
  }
}
