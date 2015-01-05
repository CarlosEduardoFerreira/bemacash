package com.kaching123.tcr.commands.store.settings;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;

import com.getbase.android.db.provider.ProviderAction;
import com.kaching123.tcr.model.PrinterModel;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore.PrinterTable;
import com.telly.groundy.PublicGroundyTask;
import com.telly.groundy.TaskResult;

import java.util.UUID;

/**
 * Created by gdubina on 11.02.14.
 */
public class EditPrinterCommand extends PublicGroundyTask {

    private static final Uri URI_PRINTER = ShopProvider.getContentUri(PrinterTable.URI_CONTENT);
    private static final String ARG_PRINTER = "ARG_PRINTER";

    private PrinterModel printerModel;

    @Override
    protected TaskResult doInBackground() {
        printerModel = (PrinterModel)getArgs().getSerializable(ARG_PRINTER);

        boolean needInsert = printerModel.guid == null;
        if(needInsert){
            printerModel.guid = UUID.randomUUID().toString();
        }
        ContentValues v = printerModel.toValues();

        if(needInsert){
            ProviderAction
                    .insert(URI_PRINTER)
                    .values(v)
                    .perform(getContext());
        }else{
            v.remove(PrinterTable.GUID);

            ProviderAction
                    .update(URI_PRINTER)
                    .values(v)
                    .where(PrinterTable.GUID + " = ?", printerModel.guid)
                    .perform(getContext());
        }
        return succeeded();
    }

   /* @Override
    protected ISqlCommand createSqlCommand() {
        return command(JdbcFactory.getConverter(printerModel).updateSQL(printerModel));
    }
*/
    public static void start(Context context, PrinterModel model){
        create(EditPrinterCommand.class)
                .arg(ARG_PRINTER, model)
                .queueUsing(context);
    }
}
