package org.opengroup.osdu.workflow.provider.azure.service;


import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.Mockito;

import org.mockito.junit.MockitoJUnitRunner;

import java.util.HashMap;


@RunWith(MockitoJUnitRunner.Silent.class)
public class SubmitIngestServiceImplTest {

  HashMap<String, Object> data = new HashMap<>();

  public void init() {

    data.put("run_id", "testworkflowId");
    data.put("conf", "test");
  }

  @Test
  public void submitIngestTestWithSuccess() {

    SubmitIngestServiceImpl submitIngestService = Mockito.mock(SubmitIngestServiceImpl.class);
    Mockito.when(submitIngestService.submitIngest("test", data)).thenReturn(Boolean.TRUE);
  }

  @Test
  public void submitIngestTestWithFailure() {

    SubmitIngestServiceImpl submitIngestService = Mockito.mock(SubmitIngestServiceImpl.class);
    Mockito.when(submitIngestService.submitIngest("test", data)).thenReturn(Boolean.FALSE);

  }

}
