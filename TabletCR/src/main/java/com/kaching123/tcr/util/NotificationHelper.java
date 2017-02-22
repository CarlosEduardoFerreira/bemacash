package com.kaching123.tcr.util;

import android.app.NotificationManager;
import android.content.Context;
import android.support.v4.app.NotificationCompat;

import com.kaching123.tcr.R;

/**
 * Created by gdubina on 07/11/13.
 */
public class NotificationHelper {

    private static final int SYNC_NOTIFICATION_ID = 0x1;
    private static final int UPLOAD_NOTIFICATION_ID = 0x2;
    private static int mCurrentNotification;

    public static void addSyncNotification(Context context){
        mCurrentNotification = SYNC_NOTIFICATION_ID;

        NotificationManager notifyManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder
                .setSmallIcon(R.mipmap.ic_launcher)
                .setAutoCancel(false)
                .setContentTitle(context.getString(R.string.sync_notify_title))
                .setContentText(context.getString(R.string.sync_notify_message))
                .setProgress(0, 0, true);

        notifyManager.notify(SYNC_NOTIFICATION_ID, builder.build());
    }

    public static void showSyncNewDataNotification(Context context) {
        mCurrentNotification = SYNC_NOTIFICATION_ID;
        setMessageNotification(context, context.getString(R.string.sync_notify_new_items));
        mCurrentNotification = 0;
    }

    public static void showSyncErrorNotification(Context context, String err) {
        mCurrentNotification = SYNC_NOTIFICATION_ID;
        setMessageNotification(context, err);
    }

    public static void removeSyncNotification(Context context) {
        mCurrentNotification = 0;
        NotificationManager notifyManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notifyManager.cancel(SYNC_NOTIFICATION_ID);
    }

    public static void addUploadNotification(Context context){
        mCurrentNotification = UPLOAD_NOTIFICATION_ID;

        NotificationManager notifyManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder
                .setSmallIcon(R.mipmap.ic_launcher)
                .setAutoCancel(false)
                .setContentTitle(context.getString(R.string.upload_notify_title))
                .setContentText(context.getString(R.string.upload_notify_message))
                .setProgress(0, 0, true);

        notifyManager.notify(UPLOAD_NOTIFICATION_ID, builder.build());

    }

    public static void showUploadErrorNotification(Context context) {
        mCurrentNotification = UPLOAD_NOTIFICATION_ID;
        showUploadErrorNotification(context, context.getString(R.string.upload_notify_message_error));
    }

    public static void showUploadErrorNotification(Context context, String errorMessage) {
        mCurrentNotification = UPLOAD_NOTIFICATION_ID;
        setMessageNotification(context, errorMessage);
    }

    public static void removeUploadNotification(Context context) {
        mCurrentNotification = 0;
        NotificationManager notifyManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notifyManager.cancel(UPLOAD_NOTIFICATION_ID);
    }


    public static void setMessageNotification(Context context, String message) {
        if (mCurrentNotification == UPLOAD_NOTIFICATION_ID){
            setMessageUploadNotification(context, message);

        } else if (mCurrentNotification == SYNC_NOTIFICATION_ID){
            setMessageDownloadNotification(context, message);
        }
    }

    static void setMessageUploadNotification(Context context, String message) {
        NotificationManager notifyManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(context.getString(R.string.upload_notify_title))
                .setContentText(message);
        notifyManager.notify(UPLOAD_NOTIFICATION_ID, builder.build());
    }

    static void setMessageDownloadNotification(Context context, String message) {
        NotificationManager notifyManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(context.getString(R.string.sync_notify_title))
                .setContentText(message);
        notifyManager.notify(SYNC_NOTIFICATION_ID, builder.build());
    }
}
