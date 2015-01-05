package com.kaching123.tcr.commands.store.inventory;

import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;

import com.kaching123.tcr.commands.store.AsyncCommand;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.jdbc.converters.ItemsJdbcConverter;
import com.kaching123.tcr.model.PriceType;
import com.kaching123.tcr.service.ISqlCommand;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore.ItemTable;
import com.telly.groundy.TaskResult;

import java.math.BigDecimal;
import java.util.ArrayList;

import static com.kaching123.tcr.model.ContentValuesUtil._decimal;

/**
 * Created by gdubina on 20.01.14.
 */
public class UpdateItemPriceCommand extends AsyncCommand {

    private static final Uri ITEM_URI = ShopProvider.getContentUri(ItemTable.URI_CONTENT);

    private String guid;
    private BigDecimal price;
    private boolean needResetPriceType;

    private int updatedRowsCount;

    @Override
    protected TaskResult doCommand() {
        return succeeded();
    }

    @Override
    protected ArrayList<ContentProviderOperation> createDbOperations() {
        ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>(1);

        ContentValues v = new ContentValues(2);
        v.put(ItemTable.SALE_PRICE, _decimal(price));
        needResetPriceType = price == null || BigDecimal.ZERO.compareTo(price) == 0;
        if(needResetPriceType){
            v.put(ItemTable.PRICE_TYPE, PriceType.OPEN.ordinal());
        }

        operations.add(ContentProviderOperation.newUpdate(ITEM_URI)
                .withValues(v)
                .withSelection(ItemTable.GUID + " = ?", new String[]{guid})
                .build());
        //tryAddPriceValidation(update);

        return operations;
    }

    /*private void tryAddPriceValidation(Update update) {
        if (price == null || BigDecimal.ZERO.compareTo(price) == 0)
            update.where(ItemTable.PRICE_TYPE + " != ?", PriceType.FIXED.ordinal());
    }*/

    @Override
    protected ISqlCommand createSqlCommand() {
        if(needResetPriceType){
            return ((ItemsJdbcConverter) JdbcFactory.getConverter(ItemTable.TABLE_NAME)).updatePriceSQL(guid, price, PriceType.OPEN, getAppCommandContext());
        }else{
            return ((ItemsJdbcConverter) JdbcFactory.getConverter(ItemTable.TABLE_NAME)).updatePriceSQL(guid, price, getAppCommandContext());
        }
    }

    @Override
    protected void afterCommand(ContentProviderResult[] dbOperationResults) {
        updatedRowsCount = dbOperationResults == null ? 0 : dbOperationResults[0].count;
    }

    /** use in import. can be standalone **/
    public int sync(Context context, String guid, BigDecimal price, IAppCommandContext appCommandContext) {
        this.guid = guid;
        this.price = price;
        this.updatedRowsCount = 0;
        TaskResult result = super.syncStandalone(context, appCommandContext);
        return isFailed(result) ? 0 : updatedRowsCount;
    }
}
