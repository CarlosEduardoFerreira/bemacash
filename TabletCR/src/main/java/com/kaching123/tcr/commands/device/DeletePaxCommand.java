package com.kaching123.tcr.commands.device;

import android.content.Context;
import android.net.Uri;

import com.getbase.android.db.provider.ProviderAction;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.store.ShopStore.PaxTable;
import com.telly.groundy.PublicGroundyTask;
import com.telly.groundy.TaskResult;

public class DeletePaxCommand extends PublicGroundyTask {

    private static final Uri URI_PRINTER = ShopProvider.getContentUri(PaxTable.URI_CONTENT);

    private static final String ARG_GUID = "arg_guid";

    @Override
    protected TaskResult doInBackground() {

        String guid = getStringArg(ARG_GUID);
        getApp().getShopPref().paxPort().remove();
        getApp().getShopPref().paxUrl().remove();
        ProviderAction
                .delete(URI_PRINTER)
                .where(PaxTable.GUID + " = ?", guid)
                .perform(getContext());

        removeData();

        return succeeded();
    }

    private void removeData() {
        getApp().setPaxTipsEnabled(false);
    }

    public static void start(Context context, String printerGuid){
        create(DeletePaxCommand.class).arg(ARG_GUID, printerGuid).queueUsing(context);
    }


}
