package com.kaching123.tcr.commands.wireless;

import android.content.ContentProviderOperation;
import android.content.Context;

import com.getbase.android.db.provider.ProviderAction;
import com.kaching123.tcr.commands.store.AsyncCommand;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.jdbc.converters.JdbcConverter;
import com.kaching123.tcr.jdbc.converters.UnitsJdbcConverter;
import com.kaching123.tcr.model.Unit;
import com.kaching123.tcr.model.Unit.Status;
import com.kaching123.tcr.service.BatchSqlCommand;
import com.kaching123.tcr.service.ISqlCommand;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore.UnitTable;
import com.telly.groundy.TaskResult;

import java.util.ArrayList;

/**
 * Created by vkompaniets on 30.07.2014.
 */
public class ApplyMultipleWarrantyCommand extends AsyncCommand {

    private static final String ARG_ITEM_GUID = "ARG_ITEM_GUID";
    private static final String ARG_WARRANTY = "ARG_WARRANTY";

    private String itemGuid;
    private int warranty;


    @Override
    protected TaskResult doCommand() {
        itemGuid = getStringArg(ARG_ITEM_GUID);
        warranty = getIntArg(ARG_WARRANTY);

        return succeeded();
    }

    @Override
    protected ISqlCommand createSqlCommand() {
        BatchSqlCommand batch = batchUpdate(UnitTable.TABLE_NAME);

        UnitsJdbcConverter converter = (UnitsJdbcConverter) JdbcFactory.getConverter(UnitTable.TABLE_NAME);
        batch.add(converter.updateWarranty(itemGuid, warranty, getAppCommandContext()));

        return batch;
    }

    @Override
    protected ArrayList<ContentProviderOperation> createDbOperations() {
        ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>(1);

        operations.add(ContentProviderOperation.newUpdate(ShopProvider.getContentUri(UnitTable.URI_CONTENT))
                .withSelection(UnitTable.ITEM_ID + " = ? AND " + UnitTable.STATUS + " = ?", new String[]{itemGuid, String.valueOf(Status.NEW.ordinal())})
                .withValue(UnitTable.WARRANTY_PERIOD, warranty)
                .build());

        return operations;
    }

    public static void start(Context context, String itemGuid, int warranty){
        create(ApplyMultipleWarrantyCommand.class).arg(ARG_ITEM_GUID, itemGuid).arg(ARG_WARRANTY, warranty).queueUsing(context);
    }
}
