package com.kaching123.tcr.commands.device;

import android.content.Context;
import android.net.Uri;

import com.getbase.android.db.provider.ProviderAction;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore.PrinterTable;
import com.telly.groundy.PublicGroundyTask;
import com.telly.groundy.TaskResult;

/**
 * Created by vkompaniets on 13.02.14.
 */
public class DeletePrinterCommand extends PublicGroundyTask {

    private static final Uri URI_PRINTER = ShopProvider.getContentUri(PrinterTable.URI_CONTENT);

    private static final String ARG_GUID = "arg_guid";

    @Override
    protected TaskResult doInBackground() {

        String guid = getStringArg(ARG_GUID);

        ProviderAction
                .delete(URI_PRINTER)
                .where(PrinterTable.GUID + " = ?", guid)
                .perform(getContext());

        return succeeded();
    }

    public static void start(Context context, String printerGuid){
        create(DeletePrinterCommand.class).arg(ARG_GUID, printerGuid).queueUsing(context);
    }


}
