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

import java.util.Date;

import org.opengroup.osdu.workflow.model.WorkflowStatus;
import org.opengroup.osdu.workflow.model.WorkflowStatusType;

public class WorkflowStatusDoc {

	private String _id;
	private String _rev;
	private String WorkflowID;
	private String AirflowRunId;
	private String Status;
	private String SubmittedAt;
	private String SubmittedBy;

	public WorkflowStatusDoc() {
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
	public String getWorkflowId() {
		return WorkflowID;
	}
	public void setWorkflowID(String workflowID) {
		this.WorkflowID = workflowID;
	}
	public String getAirflowRunId() {
		return AirflowRunId;
	}
	public void setAirflowRunId(String airflowRunId) {
		this.AirflowRunId = airflowRunId;
	}
	public String getStatus() {
		return Status;
	}
	public void setStatus(String status) {
		this.Status = status;
	}
	public String getSubmittedAt() {
		return SubmittedAt;
	}
	public void setSubmittedAt(String submittedAt) {
		this.SubmittedAt = submittedAt;
	}
	public String getSubmittedBy() {
		return SubmittedBy;
	}
	public void setSubmittedBy(String submittedBy) {
		this.SubmittedBy = submittedBy;
	}
	public WorkflowStatusDoc(String _id, String _rev, String workflowId, String airflowRunID, String status,
			String submittedAt, String submittedBy) {
		this._id = _id;
		this._rev = _rev;
		this.WorkflowID = workflowId;
		this.AirflowRunId = airflowRunID;
		this.Status = status;
		this.SubmittedAt = submittedAt;
		this.SubmittedBy = submittedBy;
	}
	@Override
	public String toString() {
		return "WorkflowStatusDoc [workflowId=" + WorkflowID + ", airflowRunID=" + AirflowRunId + ", status=" + Status
				+ ", submittedAt=" + SubmittedAt + ", submittedBy=" + SubmittedBy + "]";
	}

	public static WorkflowStatusDoc getWorkflowStatusDocFromWorkflowStatus(WorkflowStatus arg0) {
		WorkflowStatusDoc ws = new WorkflowStatusDoc();
		ws.setWorkflowID(arg0.getWorkflowId());
		ws.setAirflowRunId(arg0.getAirflowRunId());
		ws.setStatus(arg0.getWorkflowStatusType().toString());
		ws.setSubmittedAt(arg0.getSubmittedAt() == null? new Date().toString() : arg0.getSubmittedAt().toString());
		ws.setSubmittedBy(arg0.getSubmittedBy());
		return ws;
	}

	public static WorkflowStatus getWorkflowStatusFromWorkflowStatusDoc(WorkflowStatusDoc doc) {
		WorkflowStatus ws = new WorkflowStatus();
		ws.setWorkflowId(doc.getWorkflowId());
		ws.setAirflowRunId(doc.getAirflowRunId());
		ws.setWorkflowStatusType(WorkflowStatusType.valueOf(doc.getStatus()));
		Date submittedDate;
		try {
			submittedDate = new Date(doc.getSubmittedAt());
		} catch (Exception e) {
			submittedDate = null;
		}
		ws.setSubmittedAt(submittedDate);
		ws.setSubmittedBy(doc.getSubmittedBy());
		return ws;

	}

}
