package com.kaching123.tcr.commands.store.saleorder;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.net.Uri;

import com.kaching123.tcr.commands.store.AsyncCommand;
import com.kaching123.tcr.commands.store.saleorder.PrintItemsForKitchenCommand.ItemInfo;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.jdbc.converters.SaleOrderItemJdbcConverter;
import com.kaching123.tcr.model.SaleOrderItemModel;
import com.kaching123.tcr.service.BatchSqlCommand;
import com.kaching123.tcr.service.ISqlCommand;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore.SaleItemTable;
import com.telly.groundy.TaskResult;

import java.util.ArrayList;
import java.util.List;

import static com.kaching123.tcr.model.ContentValuesUtil._decimalQty;

/**
 * Created by vkompaniets on 24.09.2014.
 */
public class UpdateSaleItemKitchenQty  extends AsyncCommand{

    private static final Uri ITEM_URI = ShopProvider.getContentUri(SaleItemTable.URI_CONTENT);

    private List<ItemInfo> items;

    @Override
    protected TaskResult doCommand() {
        return succeeded();
    }

    @Override
    protected ArrayList<ContentProviderOperation> createDbOperations() {
        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>(items.size());
        for (ItemInfo item : items){
            ops.add(ContentProviderOperation
                            .newUpdate(ITEM_URI)
                            .withValue(SaleItemTable.KITCHEN_PRINTED_QTY, _decimalQty(item.qty))
                            .withSelection(SaleItemTable.SALE_ITEM_GUID + " = ?", new String[]{item.guid})
                            .build()
            );
        }
        return ops;
    }

    @Override
    protected ISqlCommand createSqlCommand() {
        BatchSqlCommand batch = batchUpdate(SaleItemTable.TABLE_NAME);
        SaleOrderItemJdbcConverter jdbcConverter = (SaleOrderItemJdbcConverter) JdbcFactory.getConverter(SaleItemTable.TABLE_NAME);
        for (ItemInfo item : items){
            batch.add(jdbcConverter.updateKitchenPrintedQty(item.guid, item.qty));
        }
        return batch;
    }

    public boolean syncStandalone(Context context, List<ItemInfo> items, IAppCommandContext appCommandContext){
        this.items = items;
        return !isFailed(syncStandalone(context, appCommandContext));
    }

}
