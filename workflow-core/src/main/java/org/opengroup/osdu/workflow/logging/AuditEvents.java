/*
  Copyright 2021 Google LLC
  Copyright 2021 EPAM Systems, Inc

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/

package org.opengroup.osdu.workflow.logging;

import java.util.List;

import com.google.common.base.Strings;
import org.opengroup.osdu.core.common.logging.audit.AuditAction;
import org.opengroup.osdu.core.common.logging.audit.AuditPayload;
import org.opengroup.osdu.core.common.logging.audit.AuditStatus;

public class AuditEvents {
  public static final String WORKFLOW_CREATE_ID = "WF001";
  public static final String WORKFLOW_CREATE_MESSAGE = "Successfully created workflow";

  public static final String WORKFLOW_UPDATE_ID = "WF002";
  public static final String WORKFLOW_UPDATE_MESSAGE = "Successfully updated workflow";

  public static final String WORKFLOW_DELETE_ID = "WF003";
  public static final String WORKFLOW_DELETE_MESSAGE = "Successfully deleted workflow";

  public static final String WORKFLOW_RUN_ID = "WF004";
  public static final String WORKFLOW_RUN_MESSAGE = "Successfully run workflow";

  private final String user;

  public AuditEvents(String user) {
    if (Strings.isNullOrEmpty(user)) {
      throw new IllegalArgumentException("User not provided for audit events.");
    }
    this.user = user;
  }

  public AuditPayload getWorkflowCreateSuccessEvent(List<String> resources) {
    return AuditPayload.builder()
        .action(AuditAction.CREATE)
        .status(AuditStatus.SUCCESS)
        .user(this.user)
        .actionId(WORKFLOW_CREATE_ID)
        .message(WORKFLOW_CREATE_MESSAGE)
        .resources(resources)
        .build();
  }

  public AuditPayload getWorkflowUpdateSuccessEvent(List<String> resources) {
    return AuditPayload.builder()
        .action(AuditAction.UPDATE)
        .status(AuditStatus.SUCCESS)
        .user(this.user)
        .actionId(WORKFLOW_UPDATE_ID)
        .message(WORKFLOW_UPDATE_MESSAGE)
        .resources(resources)
        .build();
  }

  public AuditPayload getWorkflowDeleteSuccessEvent(List<String> resources) {
    return AuditPayload.builder()
        .action(AuditAction.DELETE)
        .status(AuditStatus.SUCCESS)
        .user(this.user)
        .actionId(WORKFLOW_DELETE_ID)
        .message(WORKFLOW_DELETE_MESSAGE)
        .resources(resources)
        .build();
  }

  public AuditPayload getWorkflowRunSuccessEvent(List<String> resources) {
    return AuditPayload.builder()
        .action(AuditAction.JOB_RUN)
        .status(AuditStatus.SUCCESS)
        .user(this.user)
        .actionId(WORKFLOW_RUN_ID)
        .message(WORKFLOW_RUN_MESSAGE)
        .resources(resources)
        .build();
  }
}
