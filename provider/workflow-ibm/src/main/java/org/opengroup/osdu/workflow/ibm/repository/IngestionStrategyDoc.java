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

package org.opengroup.osdu.workflow.ibm.repository;

import org.opengroup.osdu.workflow.model.IngestionStrategy;

public class IngestionStrategyDoc {
	
    private String _id;
    private String _rev;
    private String WorkflowType;
    private String DataType;
    private String UserID;
    private String DAGName;
    

	public IngestionStrategyDoc(String _id, String _rev, String WorkflowType, String DataType, String UserID,
			String DAGName) {
		this._id = _id;
		this._rev = _rev;
		this.WorkflowType = WorkflowType;
		this.DataType = DataType;
		this.UserID = UserID;
		this.DAGName = DAGName;
	}


	public String get_id() {
		return _id;
	}


	public void set_id(String _id) {
		this._id = _id;
	}


	public String get_rev() {
		return _rev;
	}


	public void set_rev(String _rev) {
		this._rev = _rev;
	}


	public String getWorkflowType() {
		return WorkflowType;
	}


	public void setWorkflowType(String workflowType) {
		WorkflowType = workflowType;
	}


	public String getDataType() {
		return DataType;
	}


	public void setDataType(String dataType) {
		DataType = dataType;
	}


	public String getUserID() {
		return UserID;
	}


	public void setUserID(String userID) {
		UserID = userID;
	}


	public String getDAGName() {
		return DAGName;
	}


	public void setDAGName(String DAGName) {
		this.DAGName = DAGName;
	}


	@Override
	public String toString() {
		return "WorkflowStrategyDoc [workflowType=" + WorkflowType + ", dataType=" + DataType + ", userId=" + UserID
				+ ", dagName=" + DAGName + "]";
	}
	
	
	public static IngestionStrategy getIngestionStrategyFromIngestionStrategyDoc(IngestionStrategyDoc doc) {
		IngestionStrategy is = new IngestionStrategy();
		is.setWorkflowType(org.opengroup.osdu.core.common.model.WorkflowType.valueOf(doc.getWorkflowType()));
		is.setDataType(org.opengroup.osdu.core.common.model.DataType.valueOf(doc.getDataType()));
		is.setUserId(doc.getUserID());
		is.setDagName(doc.getDAGName());
		return is;
	
	}
	
    

}
