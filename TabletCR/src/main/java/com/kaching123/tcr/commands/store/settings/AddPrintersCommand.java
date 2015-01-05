package com.kaching123.tcr.commands.store.settings;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;

import com.kaching123.tcr.commands.device.PrinterInfo;
import com.kaching123.tcr.model.PrinterModel;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore.PrinterTable;
import com.telly.groundy.PublicGroundyTask;
import com.telly.groundy.TaskResult;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by gdubina on 11.02.14.
 */
public class AddPrintersCommand extends PublicGroundyTask {

    private static final Uri URI_PRINTER = ShopProvider.getContentUri(PrinterTable.URI_CONTENT);
    private static final String ARG_PRINTERS = "ARG_PRINTERS";

    private ArrayList<PrinterModel> models;

    @Override
    protected TaskResult doInBackground() {
        ArrayList<PrinterInfo> printers = getArgs().getParcelableArrayList(ARG_PRINTERS);
        models = new ArrayList<PrinterModel>(printers.size());

        ContentValues[] values = new ContentValues[printers.size()];
        for (int i = 0; i < printers.size(); i++){
            PrinterInfo p = printers.get(i);

            PrinterModel model = new PrinterModel(
                    UUID.randomUUID().toString(),
                    p.ip,
                    p.port,
                    p.macAddress,
                    p.subNet,
                    p.gateway,
                    p.dhcp,
                    null);
            models.add(model);

            values[i] = model.toValues();
        }
        getContext().getContentResolver()
                .bulkInsert(URI_PRINTER, values);
        return succeeded();
    }

    /*@Override
    protected ISqlCommand createSqlCommand() {
        BatchSqlCommand batch = batch();
        JdbcConverter<PrinterModel> converter = JdbcFactory.getConverter(PrinterTable.TABLE_NAME);
        for(PrinterModel m : models){
            batch.add(converter.insertSQL(m));
        }
        return batch;
    }
*/
    public static void start(Context context, ArrayList<PrinterInfo> printers){
        create(AddPrintersCommand.class)
                .arg(ARG_PRINTERS, printers)
                .queueUsing(context);
    }
}
