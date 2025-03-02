/*
 * Copyright 2020 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opengroup.osdu.workflow.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;
import java.util.List;

public enum WorkflowStatusType {

  @JsonProperty("submitted")
  SUBMITTED,

  @JsonProperty("running")
  RUNNING,

  @JsonProperty("finished")
  FINISHED,

  @JsonProperty("failed")
  FAILED,

  @JsonProperty("success")
  SUCCESS,

  @JsonProperty("queued")
  QUEUED;

  public static List<WorkflowStatusType> getActiveStatusTypes() {
    return Arrays.asList(SUBMITTED, RUNNING, QUEUED);
  }

  public static List<WorkflowStatusType> getCompletedStatusTypes() {
    return Arrays.asList(FINISHED, FAILED, SUCCESS);
  }

}
