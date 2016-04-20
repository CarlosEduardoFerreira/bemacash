package com.kaching123.tcr.commands;

import android.content.Context;
import android.os.Bundle;

import com.kaching123.tcr.StorageUtils;
import com.telly.groundy.GroundyTask;
import com.telly.groundy.TaskResult;
import com.telly.groundy.annotations.OnCallback;
import com.telly.groundy.annotations.OnFailure;
import com.telly.groundy.annotations.OnSuccess;
import com.telly.groundy.annotations.Param;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import static com.telly.groundy.Groundy.create;

/**
 * Created by teli on 6/10/2015.
 */
public class ApkDownloadCommand extends GroundyTask {

    private static final String EXTRA_PROGRESS = "EXTRA_CURRENT";
    private static final String EXTRA_APK_FILE_ADDRESS = "EXTRA_APK_FILE_ADDRESS";
    private static final String EXTRA_BUILD_BUMBER = "EXTRA_BUILD_BUMBER";
    private static final String CALLBACK_DOWNLOAD_STATUS = "CALLBACK_DOWNLOAD_STATUS";
    private final static String ARG_UPDATE_URL = "ARG_UPDATE_URL";
    private final static String ARG_TARGET_BUILD_BUMBER = "ARG_TARGET_BUILD_BUMBER";
    private static final int BUFFER_SIZE = 10 * 1024; // 8k ~ 32K

    @Override
    protected TaskResult doInBackground() {

        String urlAddress = getStringArg(ARG_UPDATE_URL);
        int buildBumber = getIntArg(ARG_TARGET_BUILD_BUMBER);
        InputStream in = null;
        FileOutputStream out = null;
        String apkFileAddress = null;
        try {

            URL url = new URL(urlAddress);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

            urlConnection.setRequestMethod("GET");
            urlConnection.setDoOutput(false);
            urlConnection.setConnectTimeout(10 * 1000);
            urlConnection.setReadTimeout(10 * 1000);
            urlConnection.setRequestProperty("Connection", "Keep-Alive");
            urlConnection.setRequestProperty("Charset", "UTF-8");
            urlConnection.setRequestProperty("Accept-Encoding", "gzip, deflate");

            urlConnection.connect();
            long bytetotal = urlConnection.getContentLength();
            long bytesum = 0;
            int byteread = 0;
            in = urlConnection.getInputStream();
            File dir = StorageUtils.getCacheDirectory(getContext());
            String apkName = urlAddress.substring(urlAddress.lastIndexOf("/") + 1);
            File apkFile = new File(dir, apkName);
            out = new FileOutputStream(apkFile);
            byte[] buffer = new byte[BUFFER_SIZE];

            int oldProgress = 0;
            Bundle bundle = new Bundle();
            while ((byteread = in.read(buffer)) != -1) {
                bytesum += byteread;
                out.write(buffer, 0, byteread);

                int progress = (int) (bytesum * 100L / bytetotal);

                if (progress != oldProgress) {
                    bundle.putInt(EXTRA_PROGRESS, progress);
                    callback(CALLBACK_DOWNLOAD_STATUS, bundle);
                }
                oldProgress = progress;
            }
            apkFileAddress = apkFile.toString();
//            getApp().setUpdateFilePath(apkFile.toString());
            //write permisson for sd card.
            String[] command = {"chmod", "777", apkFile.toString()};
            ProcessBuilder builder = new ProcessBuilder(command);
            builder.start();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return failed();
        } catch (IOException e) {
            e.printStackTrace();
            return failed();
        }
        return succeeded().add(EXTRA_BUILD_BUMBER, buildBumber).add(EXTRA_APK_FILE_ADDRESS, apkFileAddress);
    }

    public static void start(Context context, String url, int buildBumber, BaseApkDownloadCallback callback) {
        create(ApkDownloadCommand.class).arg(ARG_UPDATE_URL, url).arg(ARG_TARGET_BUILD_BUMBER, buildBumber).callback(callback).queueUsing(context);
    }

    public static abstract class BaseApkDownloadCallback {

        @OnCallback(value = ApkDownloadCommand.class, name = CALLBACK_DOWNLOAD_STATUS)
        public void handleSuccess(@Param(EXTRA_PROGRESS) int progress) {
            onhandleProgress(progress);
        }

        @OnSuccess(ApkDownloadCommand.class)
        public void handleSuccess(@Param(EXTRA_APK_FILE_ADDRESS) String apkFileAddress, @Param(EXTRA_BUILD_BUMBER) int buildNumber) {
            onhandleSuccess(apkFileAddress, buildNumber);
        }

        @OnFailure(ApkDownloadCommand.class)
        public void handleFailure() {
            onhandleFailure();
        }

        protected abstract void onhandleSuccess(String apkFileAddress, int buildNumber);

        protected abstract void onhandleFailure();

        protected abstract void onhandleProgress(int progress);
    }
}
