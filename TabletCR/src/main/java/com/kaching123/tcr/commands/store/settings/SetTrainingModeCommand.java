package com.kaching123.tcr.commands.store.settings;

import android.content.Context;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.commands.support.SendLogCommand;
import com.kaching123.tcr.service.SyncCommand;
import com.kaching123.tcr.service.SyncCommand.OfflineException;
import com.kaching123.tcr.store.ShopProviderExt;
import com.telly.groundy.PublicGroundyTask;
import com.telly.groundy.TaskResult;
import com.telly.groundy.annotations.OnFailure;
import com.telly.groundy.annotations.OnSuccess;

/**
 * Created by pkabakov on 02.07.2014.
 */
public class SetTrainingModeCommand extends PublicGroundyTask {

    private final static String ARG_TRAINING_MODE = "ARG_TRAINING_MODE";
    private final static String ARG_COPY_TRAINING_DATABASE = "ARG_COPY_TRAINING_DATABASE";

    private boolean isTrainingMode;
    private boolean shouldCopyToTrainingDatabase;

    public static void start(Context context, boolean isTrainingMode, boolean shouldCopyToTrainingDatabase, SetTrainingModeBaseCallback callback) {
        create(SetTrainingModeCommand.class)
                .arg(ARG_TRAINING_MODE, isTrainingMode)
                .arg(ARG_COPY_TRAINING_DATABASE, shouldCopyToTrainingDatabase)
                .callback(callback)
                .queueUsing(context);
    }

    @Override
    protected TaskResult doInBackground() {
        isTrainingMode = getBooleanArg(ARG_TRAINING_MODE);
        shouldCopyToTrainingDatabase = isTrainingMode && getBooleanArg(ARG_COPY_TRAINING_DATABASE);

        getApp().lockOnTrainingMode();
        try {
            if (getApp().isTrainingMode() == isTrainingMode) {
                Logger.e("SetTrainingMode: requested training mode is already set!");
                return succeeded();
            }

            if (isTrainingMode) {
                boolean result;
                if (shouldCopyToTrainingDatabase)
                    result = ShopProviderExt.callMethod(getContext(), ShopProviderExt.Method.METHOD_COPY_TRAINING_DATABASE, null, null);
                else
                    result = ShopProviderExt.callMethod(getContext(), ShopProviderExt.Method.METHOD_CREATE_TRAINING_DATABASE, null, null);
                if (!result)
                    return failed();
            }

            getApp().setTrainingMode(isTrainingMode);

            if (!isTrainingMode || (isTrainingMode && !shouldCopyToTrainingDatabase)) {
                getApp().clearDbRelatedPreferences();
                syncData();
            }
        } finally {
            getApp().unlockOnTrainingMode();
        }

        if (!isTrainingMode) {
            boolean result = ShopProviderExt.callMethod(getContext(), ShopProviderExt.Method.METHOD_CLEAR_TRAINING_DATABASE, null, null);
            if (!result) {
                Logger.e("SetTrainingMode: failed to clear training database!");
            }
        }

        return succeeded();
    }

    private void syncData() {
        try {
            new SyncCommand(getContext(), true).syncNow(getApp().getOperator(), getApp().getShopId());
        } catch (OfflineException e) {
            Logger.e("SetTrainingMode: failed to sync!", e);
        } catch (Exception e) {
            Logger.e("SetTrainingMode: failed to sync!", e);
            SendLogCommand.start(getContext());
        }
    }

    public static abstract class SetTrainingModeBaseCallback {

        @OnSuccess(SetTrainingModeCommand.class)
        public final void onSuccess() {
            handleSuccess();
        }

        protected abstract void handleSuccess();

        @OnFailure(SetTrainingModeCommand.class)
        public final void onFailure() {
            handleError();
        }

        protected abstract void handleError();
    }
}
