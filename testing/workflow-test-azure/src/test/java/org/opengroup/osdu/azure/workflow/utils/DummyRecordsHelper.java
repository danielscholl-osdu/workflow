package org.opengroup.osdu.azure.workflow.utils;

import com.google.gson.Gson;
import com.sun.jersey.api.client.ClientResponse;

public class DummyRecordsHelper {


    public BadRequestMock getRecordsMockFromBadRequestResponse(ClientResponse response) {
        String json = response.getEntity(String.class);
        Gson gson = new Gson();
        return gson.fromJson(json, BadRequestMock.class);
    }
    public class BadRequestMock {
        public String status;
        public String message;
        public String[] errors;

    }

}
