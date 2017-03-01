package com.kaching123.tcr.commands.store.saleorder;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.net.Uri;

import com.kaching123.tcr.commands.store.AsyncCommand;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.jdbc.converters.SaleOrderItemJdbcConverter;
import com.kaching123.tcr.model.SaleOrderItemModel;
import com.kaching123.tcr.service.ISqlCommand;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore.SaleItemTable;
import com.telly.groundy.TaskResult;

import java.util.ArrayList;

/**
 * Created by pkabakov on 21.02.14.
 */
public class UpdateNotesSaleOrderItemCommand extends AsyncCommand {

    private static final Uri URI_ITEMS = ShopProvider.getContentUri(SaleItemTable.URI_CONTENT);

    private static final String ARG_SALE_ITEM_GUID = "arg_sale_item_guid";
    private static final String ARG_SALE_ITEM_NOTES = "arg_sale_item_notes";

    private String saleItemGuid;
    private String saleItemNotes;
    private SaleOrderItemModel model;

    @Override
    protected TaskResult doCommand() {
        saleItemGuid = getStringArg(ARG_SALE_ITEM_GUID);
        saleItemNotes = getStringArg(ARG_SALE_ITEM_NOTES);

        model = new SaleOrderItemModel(saleItemGuid);
        model.notes = saleItemNotes;

        return succeeded();
    }

    @Override
    protected ArrayList<ContentProviderOperation> createDbOperations() {
        ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>(1);
        operations.add(ContentProviderOperation.newUpdate(URI_ITEMS)
                .withSelection(SaleItemTable.SALE_ITEM_GUID + " = ?", new String[]{saleItemGuid})
                .withValue(SaleItemTable.NOTES, saleItemNotes)
                .build());
        return operations;
    }

    @Override
    protected ISqlCommand createSqlCommand() {
        SaleOrderItemJdbcConverter converter = (SaleOrderItemJdbcConverter) JdbcFactory.getConverter(model);
        return converter.updateNotes(model.getGuid(), model.notes);
    }

    public static void start(Context context, String saleItemGuid, String saleItemNotes) {
        create(UpdateNotesSaleOrderItemCommand.class)
                .arg(ARG_SALE_ITEM_GUID, saleItemGuid)
                .arg(ARG_SALE_ITEM_NOTES, saleItemNotes)
                .queueUsing(context);
    }
}
