package com.kaching123.tcr.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.FileProvider;
import android.widget.Toast;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.R;
import com.kaching123.tcr.model.ApplicationVersion;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class LogUtils {

    //private final static String TAG = App.TAG;

    private LogUtils() { }

    /*
     * Loosely based on LogUtils.java from DashClock:
     *
     * Copyright 2013 Google Inc.
     *
     * Licensed under the Apache License, Version 2.0 (the "License");
     * you may not use this file except in compliance with the License.
     * You may obtain a copy of the License at
     *
     *     http://www.apache.org/licenses/LICENSE-2.0
     *
     * Unless required by applicable law or agreed to in writing, software
     * distributed under the License is distributed on an "AS IS" BASIS,
     * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     * See the License for the specific language governing permissions and
     * limitations under the License.
     */
    /*
     * Most modifications inspired by:
     *
     * Copyright (C) 2011 Random Android Code Snippets
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

    private final static String[] logcatCmd = { "logcat", "-v", "threadtime", "-d", };

    public static String getDebugLog(final Context context) {
        BufferedReader logcat = null;
        StringBuilder log = new StringBuilder("<html><body>");
        String newLine = "<br>";
        try {
            final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmm");
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

            // Append app version name
            ApplicationVersion version = Util.getApplicationVersion(context);
            log.append("App version:").append(newLine).append(version.name + " (" + version.code + ")").append(newLine).append(newLine);

            // Append device build fingerprint
            log.append("Device fingerprint:").append(newLine).append(Build.FINGERPRINT).append(newLine).append(newLine);

            // Append app's logs
            Process process = Runtime.getRuntime().exec(logcatCmd);
            logcat = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = logcat.readLine()) != null) {
                log.append(line);
                log.append(newLine);
            }
        } catch (IOException e) {
            log.append(newLine);
            log.append("Generate file error: ").append(e.getMessage());
            Logger.e("[LOG] Error accessing or sending app's logs.", e);
            Toast.makeText(context, "Error accessing or sending app's logs.", Toast.LENGTH_SHORT).show();
        } finally {
            if (null != logcat) {
                try { logcat.close(); } catch (Exception ignore) { }
            }
        }
        log.append("</body></html>");
        return log.toString();
    }

    public static void sendDebugLog(final Activity context) {
        /*if (!BuildConfig.DEBUG) {
            return;
        }*/

        Writer log = null;
        BufferedReader logcat = null;
        try {
            File logsDir = context.getCacheDir();
            if (logsDir == null) {
                throw new IOException("Cache directory inaccessible");
            }
            logsDir = new File(logsDir, "logs");
            deleteRecursive(logsDir);
            logsDir.mkdirs();

            final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmm");
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            final String fileName = "bema_tablet_log_" + sdf.format(new Date()) + ".txt";

            final File logFile = new File(logsDir, fileName);
            log = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(logFile)));

            // Append app version name
            ApplicationVersion version = Util.getApplicationVersion(context);
            log.append("App version:\n").append(version.name).append("\n\n");

            // Append device build fingerprint
            log.append("Device fingerprint:\n").append(Build.FINGERPRINT).append("\n\n");

            // Append app's logs
            Process process = Runtime.getRuntime().exec(logcatCmd);
            logcat = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = logcat.readLine()) != null) {
                log.append(line);
                log.append("\n");
            }
            log.flush();
            log.close();
            log = null;

            // Send the file
            final Uri shareUri = FileProvider.getUriForFile(context, context.getPackageName() + ".files", logFile);
            final Intent sendIntent = ShareCompat.IntentBuilder.from(context)
                        .setStream(shareUri)
                        .setType("message/rfc822")
                        .addEmailTo(context.getString(R.string.cfg_debug_send_logs_address))
                        .setSubject(context.getString(R.string.debug_send_logs_email_subject))
                        .getIntent()
                        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            Logger.d("[LOG] Sending %s via %s", shareUri, sendIntent);
            context.startActivity(Intent.createChooser(sendIntent, "Send debug info"));

        } catch (IOException e) {
            Logger.e("[LOG] Error accessing or sending app's logs.", e);
            Toast.makeText(context, "Error accessing or sending app's logs.", Toast.LENGTH_SHORT).show();
        } finally {
            if (null != logcat) {
                try { logcat.close(); } catch (Exception ignore) { }
            }
            if (null != log) {
                try { log.close(); } catch (Exception ignore) { }
            }
        }

    }

    private static void deleteRecursive(final File file) {
        if (file.exists()) {
            file.listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    if (pathname.isDirectory()) {
                        deleteRecursive(pathname);
                    } else {
                        pathname.delete();
                    }
                    return false;
                }
            });
        }
    }
}
