package com.kaching123.tcr.commands.rest.sync;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by Rodrigo Busata on 9/6/2016.
 */
public class GetResponseDownloadBatch {

    public String requestId;

    public GetResponseDownloadBatch(String requestId) {
        this.requestId = requestId;
    }

    public static String getDataFromFile(String file) throws IOException {
        File dataFile = new File(file);
        StringBuilder text = new StringBuilder();

        BufferedReader br = new BufferedReader(new FileReader(dataFile));
        String line;

        while ((line = br.readLine()) != null) {
            text.append(line);
            text.append('\n');
        }
        br.close();

        return text.toString();
    }
}
