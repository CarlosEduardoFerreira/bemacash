package com.kaching123.tcr.commands.store.settings;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;

import com.kaching123.tcr.commands.device.PrinterInfo;
import com.kaching123.tcr.model.KDSModel;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore.KDSTable;
import com.telly.groundy.PublicGroundyTask;
import com.telly.groundy.TaskResult;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by gdubina on 11.02.14.
 */
public class AddKDSCommand extends PublicGroundyTask {

    private static final Uri URI_KDS = ShopProvider.getContentUri(KDSTable.URI_CONTENT);
    private static final String ARG_KDS = "ARG_KDS";

    private ArrayList<KDSModel> models;

    @Override
    protected TaskResult doInBackground() {
        ArrayList<PrinterInfo> printers = getArgs().getParcelableArrayList(ARG_KDS);
        models = new ArrayList<KDSModel>(printers.size());

        ContentValues[] values = new ContentValues[printers.size()];
        for (int i = 0; i < printers.size(); i++){
            PrinterInfo p = printers.get(i);

            KDSModel model = new KDSModel(
                    UUID.randomUUID().toString(),
                    p.ip,
                    p.port,
                    null);
            models.add(model);

            values[i] = model.toValues();
        }
        getContext().getContentResolver()
                .bulkInsert(URI_KDS, values);
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
    public static void start(Context context, ArrayList<KDSModel> kdsModels){
        create(AddKDSCommand.class)
                .arg(ARG_KDS, kdsModels)
                .queueUsing(context);
    }
}
