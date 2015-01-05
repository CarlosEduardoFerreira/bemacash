package com.kaching123.tcr.commands.support;

import android.content.ContentProviderOperation;

import com.kaching123.tcr.commands.rest.sync.SyncApi;
import com.kaching123.tcr.commands.store.AsyncCommand;
import com.kaching123.tcr.service.ISqlCommand;
import com.telly.groundy.TaskResult;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

/**
 * @author Ivan v. Rikhmayer
 *         This class is intended to
 */
public abstract class WebAPICommand extends AsyncCommand {

    public static final int CONNECTION_TIMEOUT = (int) TimeUnit.SECONDS.toMillis(10);


    @Override
    protected TaskResult doCommand() {
        return doCommand(getApi());
    }

    protected abstract TaskResult doCommand(SyncApi api);

    @Override
    protected boolean validateAppCommandContext() {
        return false;
    }
    @Override
    protected ArrayList<ContentProviderOperation> createDbOperations() {
        return null;
    }

    @Override
    protected ISqlCommand createSqlCommand() {
        return null;
    }


    private SyncApi getApi() {
       return getApp().getRestAdapter().create(SyncApi.class);
    }


    public static class WebAPIException extends RuntimeException {

        public WebAPIException(String message, Throwable throwable) {
            super(message, throwable);
        }
    }
}
