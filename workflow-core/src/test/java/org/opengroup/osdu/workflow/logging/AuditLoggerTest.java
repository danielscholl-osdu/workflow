package org.opengroup.osdu.workflow.logging;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.core.common.logging.audit.AuditAction;
import org.opengroup.osdu.core.common.logging.audit.AuditPayload;
import org.opengroup.osdu.core.common.logging.audit.AuditStatus;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class AuditLoggerTest {
  public static final String WORKFLOW_CREATE_ID = "WF001";
  public static final String WORKFLOW_CREATE_MESSAGE = "Successfully created workflow";

  public static final String WORKFLOW_UPDATE_ID = "WF002";
  public static final String WORKFLOW_UPDATE_MESSAGE = "Successfully updated workflow";

  public static final String WORKFLOW_DELETE_ID = "WF003";
  public static final String WORKFLOW_DELETE_MESSAGE = "Successfully deleted workflow";

  public static final String WORKFLOW_RUN_ID = "WF004";
  public static final String WORKFLOW_RUN_MESSAGE = "Successfully run workflow";
  private static final String USER_EMAIL = "user@email.com";

  private List<String> resources = new ArrayList<>();

  @Mock
  private JaxRsDpsLog logger;

  @Mock
  private DpsHeaders headers;

  @InjectMocks
  private AuditLogger auditLogger;

  @BeforeEach
  void init() {
    resources = Arrays.asList("resource1", "resource2");
    lenient().when(headers.getUserEmail()).thenReturn(USER_EMAIL);
  }

  @Test
  void testWorkflowCreateEvent() {
    final ArgumentCaptor<AuditPayload> auditPayloadArgumentCaptor = ArgumentCaptor.forClass(AuditPayload.class);
    auditLogger.workflowCreateEvent(resources);
    verify(logger).audit(auditPayloadArgumentCaptor.capture());

    AuditPayload auditPayload = auditPayloadArgumentCaptor.getValue();
    Map<String, Object> auditLogPayload = (HashMap) auditPayload.get("auditLog");

    assertEquals(AuditAction.CREATE, auditLogPayload.get("action"));
    assertEquals(AuditStatus.SUCCESS, auditLogPayload.get("status"));
    assertEquals(USER_EMAIL, auditLogPayload.get("user"));
    assertEquals(WORKFLOW_CREATE_ID, auditLogPayload.get("actionId"));
    assertEquals(WORKFLOW_CREATE_MESSAGE, auditLogPayload.get("message"));
    assertEquals(resources, auditLogPayload.get("resources"));
  }

  @Test
  void testWorkflowUpdateEvent() {
    final ArgumentCaptor<AuditPayload> auditPayloadArgumentCaptor = ArgumentCaptor.forClass(AuditPayload.class);
    auditLogger.workflowUpdateEvent(resources);
    verify(logger).audit(auditPayloadArgumentCaptor.capture());

    AuditPayload auditPayload = auditPayloadArgumentCaptor.getValue();
    Map<String, Object> auditLogPayload = (HashMap) auditPayload.get("auditLog");

    assertEquals(AuditAction.UPDATE, auditLogPayload.get("action"));
    assertEquals(AuditStatus.SUCCESS, auditLogPayload.get("status"));
    assertEquals(USER_EMAIL, auditLogPayload.get("user"));
    assertEquals(WORKFLOW_UPDATE_ID, auditLogPayload.get("actionId"));
    assertEquals(WORKFLOW_UPDATE_MESSAGE, auditLogPayload.get("message"));
    assertEquals(resources, auditLogPayload.get("resources"));
  }

  @Test
  void testWorkflowDeleteEvent() {
    final ArgumentCaptor<AuditPayload> auditPayloadArgumentCaptor = ArgumentCaptor.forClass(AuditPayload.class);
    auditLogger.workflowDeleteEvent(resources);
    verify(logger).audit(auditPayloadArgumentCaptor.capture());

    AuditPayload auditPayload = auditPayloadArgumentCaptor.getValue();
    Map<String, Object> auditLogPayload = (HashMap) auditPayload.get("auditLog");

    assertEquals(AuditAction.DELETE, auditLogPayload.get("action"));
    assertEquals(AuditStatus.SUCCESS, auditLogPayload.get("status"));
    assertEquals(USER_EMAIL, auditLogPayload.get("user"));
    assertEquals(WORKFLOW_DELETE_ID, auditLogPayload.get("actionId"));
    assertEquals(WORKFLOW_DELETE_MESSAGE, auditLogPayload.get("message"));
    assertEquals(resources, auditLogPayload.get("resources"));
  }

  @Test
  void testWorkflowRunEvent() {
    final ArgumentCaptor<AuditPayload> auditPayloadArgumentCaptor = ArgumentCaptor.forClass(AuditPayload.class);
    auditLogger.workflowRunEvent(resources);
    verify(logger).audit(auditPayloadArgumentCaptor.capture());

    AuditPayload auditPayload = auditPayloadArgumentCaptor.getValue();
    Map<String, Object> auditLogPayload = (HashMap) auditPayload.get("auditLog");

    assertEquals(AuditAction.JOB_RUN, auditLogPayload.get("action"));
    assertEquals(AuditStatus.SUCCESS, auditLogPayload.get("status"));
    assertEquals(USER_EMAIL, auditLogPayload.get("user"));
    assertEquals(WORKFLOW_RUN_ID, auditLogPayload.get("actionId"));
    assertEquals(WORKFLOW_RUN_MESSAGE, auditLogPayload.get("message"));
    assertEquals(resources, auditLogPayload.get("resources"));
  }

  @Test
  void testInvalidUserGivenToAuditEvents() {
    String emptyUser = "";
    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      new AuditEvents(emptyUser);
    });
  }
}
