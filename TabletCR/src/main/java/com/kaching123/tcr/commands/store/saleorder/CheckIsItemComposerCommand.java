package com.kaching123.tcr.commands.store.saleorder;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.getbase.android.db.provider.ProviderAction;
import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.commands.store.AsyncCommand;
import com.kaching123.tcr.model.ComposerModel;
import com.kaching123.tcr.model.SaleOrderItemModel;
import com.kaching123.tcr.model.converter.SaleOrderItemFunction;
import com.kaching123.tcr.service.ISqlCommand;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore;
import com.telly.groundy.TaskResult;
import com.telly.groundy.annotations.OnSuccess;
import com.telly.groundy.annotations.Param;

import java.util.ArrayList;

/**
 * Created by Simpsor on 9/2/2016.
 */
public class CheckIsItemComposerCommand extends AsyncCommand {

    private static final Uri COMPOSER_URI = ShopProvider.contentUri(ShopStore.ComposerTable.URI_CONTENT);

    private static final String ARG_GUID = "ARG_GUID";
    private static final String PARAM_ANSWER = "PARAM_ANSWER";


    @Override
    protected TaskResult doCommand() {
        String itemGuid = getStringArg(ARG_GUID);

        Cursor composerCursor = ProviderAction.query(COMPOSER_URI)
                .where(ShopStore.ComposerTable.ITEM_HOST_ID + " =?", itemGuid)
                .perform(getContext());

        if (composerCursor.moveToFirst()) {
            composerCursor.close();
            return succeeded().add(PARAM_ANSWER, true);

        } else {
            composerCursor.close();
            return succeeded().add(PARAM_ANSWER, false);
        }

    }



    @Override
    protected ArrayList<ContentProviderOperation> createDbOperations() {
        return null;
    }

    @Override
    protected ISqlCommand createSqlCommand() {
        return null;
    }

    public static void start(Context context, String itemGuid, IsItemComposerCommandCallback callback) {
        create(CheckIsItemComposerCommand.class)
                .arg(ARG_GUID, itemGuid)
                .callback(callback)
                .queueUsing(context);
    }

    public static abstract class IsItemComposerCommandCallback {

        @OnSuccess(CheckIsItemComposerCommand.class)
        public void handleSuccess(@Param(PARAM_ANSWER) boolean isItemComposer) {
            onSuccess(isItemComposer);
        }

        protected abstract void onSuccess(boolean isItemComposer);

    }

}
