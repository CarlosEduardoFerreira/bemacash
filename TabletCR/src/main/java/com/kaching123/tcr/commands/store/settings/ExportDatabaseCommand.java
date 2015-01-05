package com.kaching123.tcr.commands.store.settings;

import android.content.Context;

import com.kaching123.tcr.store.ShopProviderExt;
import com.telly.groundy.PublicGroundyTask;
import com.telly.groundy.TaskResult;
import com.telly.groundy.annotations.OnFailure;
import com.telly.groundy.annotations.OnSuccess;

/**
 * Created by pkabakov on 30.04.2014.
 */
public class ExportDatabaseCommand extends PublicGroundyTask {

    private static final String ARG_FOLDER_NAME = "ARG_FOLDER_NAME";

    public static void start(Context context, String folderName, BaseExportDatabaseCallback callback){
        create(ExportDatabaseCommand.class)
                .arg(ARG_FOLDER_NAME, folderName)
                .callback(callback)
                .queueUsing(context);
    }

    @Override
    protected TaskResult doInBackground() {
        boolean result = ShopProviderExt.callMethod(getContext(), ShopProviderExt.Method.METHOD_EXPORT_DATABASE, getStringArg(ARG_FOLDER_NAME), null);
        return result ? succeeded() : failed();
    }

    public static abstract class BaseExportDatabaseCallback {

        @OnSuccess(ExportDatabaseCommand.class)
        public final void onSuccess(){
            handleOnSuccess();
        }

        protected abstract void handleOnSuccess();

        @OnFailure(ExportDatabaseCommand.class)
        public final void onFailure(){
            handleOnFailure();
        }

        protected abstract void handleOnFailure();

    }
}
