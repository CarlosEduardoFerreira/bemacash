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
    private static final String ARG_SERIAL = "arg_serial";

    private static final String ARG_DELETE_OTHER = "arg_delete_other";

    @Override
    protected TaskResult doInBackground() {

        String guid = getStringArg(ARG_GUID);
        String serial = getStringArg(ARG_SERIAL);
        boolean deleteOther = getBooleanArg(ARG_DELETE_OTHER);
        if (!deleteOther) {
            getApp().getShopPref().paxPort().remove();
            getApp().getShopPref().paxUrl().remove();
            ProviderAction
                    .delete(URI_PRINTER)
                    .where(PaxTable.GUID + " = ? ", guid)
                    .perform(getContext());
        } else {
//            updateCurrentPax(getApp().getShopPref().paxUrl().get(), getApp().getShopPref().paxPort().get());
            ProviderAction
                    .delete(URI_PRINTER)
                    .perform(getContext());
        }


        return succeeded();
    }

    private void updateCurrentPax(String ip, int port) {
        ProviderAction.update(URI_PRINTER)
                .value(ShopStore.PaxTable.IP, ip)
                .value(ShopStore.PaxTable.PORT, port)
                .perform(getContext());
    }

    private void removeData() {
        getApp().setPaxTipsEnabled(false);
    }

    public static void start(Context context, String printerGuid, boolean deleteOther, String serial) {
        create(DeletePaxCommand.class).arg(ARG_GUID, printerGuid).arg(ARG_DELETE_OTHER, deleteOther).arg(ARG_SERIAL, serial).queueUsing(context);
    }


}
