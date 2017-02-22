package com.kaching123.tcr.commands.rest.sync;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

import com.kaching123.tcr.Logger;

/**
 * Created by Rodrigo Busata on 9/6/2016.
 */
public class GetResponseUploadBatchStatus {

    public Integer currentFile;
    public Integer totalFiles;
    public String percentage;
    public Status status;
    public String errorMessage;
    public Map<Integer, Integer> response;

    public static GetResponseUploadBatchStatus fromJson(String dataString) {
        String json = GetResponseBatchStatus.decompressWithDecode(dataString);
        if (json == null) return new GetResponseUploadBatchStatus();
        Logger.d("responseUploadBatchStatus: " + json);

        GetResponseUploadBatchStatus entity = new Gson().fromJson(json, GetResponseUploadBatchStatus.class);
        try {
            int status = new JSONObject(json).getInt("status");
            entity.status = status > 0 ? Status.values()[status] : null;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return entity;
    }

    public int getCurrentFile(){
        return currentFile != null ? currentFile : -1;
    }

    public enum Status {
        TODO, DOING, DONE, ERROR, ABORTED, ERROR_NO_AFFECTED
    }
}