/*
 *  Copyright 2020-2021 Google LLC
 *  Copyright 2020-2021 EPAM Systems, Inc
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.opengroup.osdu.workflow.provider.gcp.osm.config;

import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_SINGLETON;

import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.opengroup.osdu.core.common.model.tenant.TenantInfo;
import org.opengroup.osdu.core.common.provider.interfaces.ITenantFactory;
import org.opengroup.osdu.core.gcp.osm.model.Destination;
import org.opengroup.osdu.core.gcp.osm.translate.postgresql.PgDestinationResolution;
import org.opengroup.osdu.core.gcp.osm.translate.postgresql.PgDestinationResolver;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope(SCOPE_SINGLETON)
@ConditionalOnProperty(name = "osmDriver", havingValue = "postgres")
@RequiredArgsConstructor
public class PgTenantDestinationResolver implements PgDestinationResolver {

  private final ITenantFactory tenantInfoFactory;

  /**
   * Takes provided Destination with partitionId set to needed tenantId, gets its TenantInfo and
   * uses it to find a relevant DB server URL.
   *
   * @param destination to resolve
   * @return resolution results
   */
  @Override
  public PgDestinationResolution resolve(Destination destination) {
    TenantInfo ti = tenantInfoFactory.getTenantInfo(destination.getPartitionId());
    return PgDestinationResolution.builder().projectId(ti.getProjectId()).build();
  }

  @Override
  public void close() throws IOException {

  }
}
