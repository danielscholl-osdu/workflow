/*
 * Copyright 2021 Google LLC
 * Copyright 2021 EPAM Systems, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opengroup.osdu.workflow.api;

import java.io.IOException;
import org.opengroup.osdu.core.common.info.VersionInfoBuilder;
import org.opengroup.osdu.core.common.model.info.VersionInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/v1")
@Tag(name = "info", description = "Version info endpoint")
public class InfoApi {

  @Autowired
  private VersionInfoBuilder versionInfoBuilder;

  @Operation(summary = "${infoApi.info.summary}", description = "${infoApi.info.description}", tags = { "info" })
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Version info.", content = { @Content(schema = @Schema(implementation = VersionInfo.class)) })
  })
  @GetMapping(value = "/info", produces = MediaType.APPLICATION_JSON_VALUE)
  public VersionInfo info() throws IOException {
    return versionInfoBuilder.buildVersionInfo();
  }
}

