package com.kaching123.tcr.commands.device;

import android.content.Context;
import android.net.Uri;

import com.getbase.android.db.provider.ProviderAction;
import com.kaching123.tcr.model.PaxModel;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.store.ShopStore.PaxTable;
import com.telly.groundy.PublicGroundyTask;
import com.telly.groundy.TaskResult;

public class DeletePaxCommand extends PublicGroundyTask {

    private static final Uri URI_PRINTER = ShopProvider.getContentUri(PaxTable.URI_CONTENT);

    private static final String ARG_GUID = "arg_guid";

    private static final String ARG_PAX_MODEL = "ARG_PAX_MODEL";

    private static final String ARG_DELETE_OTHER = "arg_delete_other";

    @Override
    protected TaskResult doInBackground() {
        PaxModel model = getArgs().getParcelable(ARG_PAX_MODEL);

        String guid = model.guid;
        String ip = model.ip;
        boolean deleteOther = getBooleanArg(ARG_DELETE_OTHER);
        if (!deleteOther) {
            ProviderAction
                    .delete(URI_PRINTER)
                    .where(PaxTable.GUID + " = ? ", guid)
                    .perform(getContext());

            getApp().getShopPref().paxPort().remove();
            getApp().getShopPref().paxUrl().remove();
        } else {
//            updateCurrentPax(getApp().getShopPref().paxUrl().get(), getApp().getShopPref().paxPort().get());
            ProviderAction
                    .delete(URI_PRINTER)
                    .where(PaxTable.IP + " != ? ", ip)
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

    public static void start(Context context, PaxModel model, boolean deleteOther) {
        create(DeletePaxCommand.class).arg(ARG_PAX_MODEL, model).arg(ARG_DELETE_OTHER, deleteOther).queueUsing(context);
    }


}
