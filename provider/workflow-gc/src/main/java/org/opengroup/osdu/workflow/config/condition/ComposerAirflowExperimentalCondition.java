/*
 *  Copyright 2020-2022 Google LLC
 *  Copyright 2020-2022 EPAM Systems, Inc
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

package org.opengroup.osdu.workflow.config.condition;

import static org.opengroup.osdu.workflow.config.AirflowConfigConstants.COMPOSER_CLIENT;
import static org.opengroup.osdu.workflow.config.AirflowConfigConstants.IS_AIRFLOW_API_VERSION_2;
import static org.opengroup.osdu.workflow.config.AirflowConfigConstants.NONE;

import com.google.common.base.Strings;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class ComposerAirflowExperimentalCondition implements Condition {

  @Override
  public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
    Environment environment = context.getEnvironment();
    String composerClientVersion = environment.getProperty(COMPOSER_CLIENT);
    Boolean isAirflowVersionV2 = environment.getProperty(IS_AIRFLOW_API_VERSION_2, Boolean.class);

    return
        (!Strings.isNullOrEmpty(composerClientVersion) && !composerClientVersion.equals(NONE))
            && Boolean.FALSE.equals(isAirflowVersionV2);
  }
}
