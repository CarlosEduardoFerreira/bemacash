package com.kaching123.tcr.commands.rest.sync;

import java.io.IOException;

/**
 * Created by Rodrigo Busata on 9/6/2016.
 */
public class GetResponseUploadBatch {

    public String requestId;

    public GetResponseUploadBatch(String requestId) {
        this.requestId = requestId;
    }

    public static String getDataFromFile(String file) throws IOException {
        return GetResponseDownloadBatch.getDataFromFile(file);
    }
}
