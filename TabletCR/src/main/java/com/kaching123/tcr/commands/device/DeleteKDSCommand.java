package com.kaching123.tcr.commands.device;

import android.content.Context;
import android.net.Uri;

import com.getbase.android.db.provider.ProviderAction;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore.KDSTable;
import com.telly.groundy.PublicGroundyTask;
import com.telly.groundy.TaskResult;
import com.telly.groundy.annotations.OnSuccess;

/**
 * Created by vkompaniets on 13.02.14.
 */
public class DeleteKDSCommand extends PublicGroundyTask {

    private static final Uri URI_KDS = ShopProvider.getContentUri(KDSTable.URI_CONTENT);

    private static final String ARG_GUID = "arg_guid";

    @Override
    protected TaskResult doInBackground() {

        String guid = getStringArg(ARG_GUID);

        ProviderAction
                .delete(URI_KDS)
                .where(KDSTable.GUID + " = ?", guid)
                .perform(getContext());

        return succeeded();
    }

    public static void start(Context context, String kdsGuid, Callback callback){
        create(DeleteKDSCommand.class).callback(callback).arg(ARG_GUID, kdsGuid).queueUsing(context);
    }

    public static void sync(Context context, String aliasGuid)
    {
        ProviderAction
                .delete(URI_KDS)
                .where(KDSTable.ALIAS_GUID + " = ?", aliasGuid)
                .perform(context);
    }

    public static void start(Context context, String kdsGuid){
        create(DeleteKDSCommand.class).arg(ARG_GUID, kdsGuid).queueUsing(context);
    }

    public static abstract class Callback {

        @OnSuccess(DeleteKDSCommand.class)
        public final void onSuccess() {
            handleSuccess();
        }

        protected abstract void handleSuccess();
    }
}
