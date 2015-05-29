package com.kaching123.tcr.commands.rest;

import com.kaching123.tcr.commands.rest.sync.SyncApi;
import com.kaching123.tcr.util.JdbcJSONArray;
import com.kaching123.tcr.util.JdbcJSONObject;
import com.telly.groundy.PublicGroundyTask;
import com.telly.groundy.TaskResult;

/**
 * Created by pkabakov on 12.02.14.
 */
public abstract class RestCommand extends PublicGroundyTask {

    public static class Response {

        private static final String STATUS_ERROR = "error";
        private static final String STATUS_SUCCESS = "success";

        public String status;
        public String message;

        public static Response responseFailed() {
            return new Response(Response.STATUS_ERROR, null);
        }

        public Response(String status, String message) {
            this.status = status;
            this.message = message;
        }

        public boolean isSuccess() {
            return STATUS_SUCCESS.equals(status) || "200".equals(status);
        }
        public boolean isCredentialsFial() {
            return "400".equals(status);
        }

        @Override
        public String toString() {
            return "\t status:\t" + status +
                   ";\t message:\t" + message;
        }
    }

    public static class PlainTextResponse extends Response {

        public String entity;

        public PlainTextResponse(String status, String message, String entity) {
            super(status, message);
            this.entity = entity;
        }
    }

    public static class JsonResponse extends Response {

        public JdbcJSONObject entity;

        public JsonResponse(String status, String message, JdbcJSONObject entity) {
            super(status, message);
            this.entity = entity;
        }

        public JdbcJSONObject getEntity() {
            return entity;
        }
    }

    public static class JsonArrayResponse extends Response {

        public JdbcJSONArray entity;

        public JsonArrayResponse(String status, String message, JdbcJSONArray entity) {
            super(status, message);
            this.entity = entity;
        }

        public JdbcJSONArray getEntity() {
            return entity;
        }
    }

    @Override
    protected TaskResult doInBackground() {
        Response response = execute(getRestApi(), getApp().emailApiKey);
        return response.isSuccess() ? succeeded() : failed();
    }

    private SyncApi getRestApi() {
        return getApp().getRestAdapter().create(SyncApi.class);
    }

    protected abstract Response execute(SyncApi restApi, String apiKey);
}
