/**
 * Copyright 2020 IBM Corp. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opengroup.osdu.workflow.provider.ibm.exception;

import org.opengroup.osdu.core.common.exception.BadRequestException;

public class WorkflowStatusNotSavedException extends BadRequestException{

	public WorkflowStatusNotSavedException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}
	
	public WorkflowStatusNotSavedException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}
	

}
