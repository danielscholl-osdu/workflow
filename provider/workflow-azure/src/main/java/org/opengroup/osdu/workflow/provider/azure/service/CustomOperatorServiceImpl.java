package org.opengroup.osdu.workflow.provider.azure.service;

import lombok.extern.slf4j.Slf4j;
import org.opengroup.osdu.core.common.exception.BadRequestException;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.workflow.provider.azure.interfaces.ICustomOperatorMetadataRepository;
import org.opengroup.osdu.workflow.provider.azure.interfaces.ICustomOperatorService;
import org.opengroup.osdu.workflow.provider.azure.model.customoperator.CustomOperator;
import org.opengroup.osdu.workflow.provider.azure.model.customoperator.CustomOperatorsPage;
import org.opengroup.osdu.workflow.provider.azure.model.customoperator.RegisterCustomOperatorRequest;
import org.opengroup.osdu.workflow.provider.interfaces.IWorkflowEngineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CustomOperatorServiceImpl implements ICustomOperatorService {
  private static final String FILE_NAME_PREFIX = ".py";
  private static final Integer GET_OPERATORS_MAX_LIMIT = 50;

  @Autowired
  private ICustomOperatorMetadataRepository customOperatorMetadataRepository;

  @Autowired
  private DpsHeaders dpsHeaders;

  @Autowired
  private IWorkflowEngineService workflowEngineService;

  @Override
  public CustomOperator registerNewOperator(RegisterCustomOperatorRequest customOperatorRequest) {
    final CustomOperator customOperator = customOperatorMetadataRepository
        .saveMetadata(createCustomOperatorFromRequest(customOperatorRequest));
    workflowEngineService.saveCustomOperator(customOperatorRequest.getContent(),
        customOperatorRequest.getName() + FILE_NAME_PREFIX);
    return customOperator;
  }

  private CustomOperator createCustomOperatorFromRequest(
      RegisterCustomOperatorRequest registerRequest) {
    return CustomOperator.builder().name(registerRequest.getName())
        .createdBy(dpsHeaders.getUserEmail()).createdAt(System.currentTimeMillis())
        .className(registerRequest.getClassName()).description(registerRequest.getDescription())
        .properties(registerRequest.getProperties()).build();
  }

  @Override
  public CustomOperatorsPage getAllOperators(Integer limit, String cursor) {
    if(limit <=0 || limit > GET_OPERATORS_MAX_LIMIT) {
      throw new BadRequestException(
          String.format("Value of limit should be in the range of 1 to %d",
              GET_OPERATORS_MAX_LIMIT));
    }
    return customOperatorMetadataRepository.getMetadataForAllCustomOperators(limit, cursor);
  }

  @Override
  public CustomOperator getOperatorById(String id) {
    return customOperatorMetadataRepository.getMetadataByCustomOperatorId(id);
  }
}
