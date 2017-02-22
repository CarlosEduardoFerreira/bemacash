package com.kaching123.tcr.commands.rest.sync;

import android.util.Base64;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import com.kaching123.tcr.Logger;

/**
 * Created by Rodrigo Busata on 9/6/2016.
 */
public class GetResponseBatchStatus {

    public String downloadUrl;
    public String currentEntity;
    public String totalEntities;
    public String percentage;
    public String md5File;
    public Status status;
    public String requestId;
    public String errorMessage;
    public Map<String, Response> response;

    public static GetResponseBatchStatus fromJson(String dataString) {
        String json = decompressWithDecode(dataString);
        if (json == null) return new GetResponseBatchStatus();
        Logger.d("responseBatchStatus: " + json);

        GetResponseBatchStatus entity = new Gson().fromJson(json, GetResponseBatchStatus.class);
        try {
            int status = new JSONObject(json).getInt("status");
            entity.status = status > 0 ? Status.values()[status] : null;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return entity;
    }

    public static String decompressWithDecode(String dataString) {
        byte[] data = Base64.decode(dataString.getBytes(), Base64.DEFAULT);

        try {
            Inflater inflater = new Inflater();
            inflater.setInput(data);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);
            byte[] buffer = new byte[1024];
            while (!inflater.finished()) {
                int count;
                count = inflater.inflate(buffer);

                outputStream.write(buffer, 0, count);
            }
            outputStream.close();

            return new String(outputStream.toByteArray());

        } catch (DataFormatException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public int getPercentage(){
        if (percentage == null) return 0;
        return (int)Double.parseDouble(percentage);
    }

    public enum Status {
        TODO, DOING, DONE, ERROR
    }

    public static class Response {

        public String entity;
        public List<String> files;
    }
}