package com.kaching123.tcr.commands.store.saleorder;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.getbase.android.db.provider.ProviderAction;
import com.kaching123.tcr.commands.store.AsyncCommand;
import com.kaching123.tcr.commands.store.saleorder.PrintItemsForKitchenCommand.KitchenPrintStatus;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.jdbc.converters.SaleOrdersJdbcConverter;
import com.kaching123.tcr.model.SaleOrderModel;
import com.kaching123.tcr.service.ISqlCommand;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore.SaleOrderTable;
import com.telly.groundy.TaskResult;

import java.util.ArrayList;

import static com.kaching123.tcr.model.ContentValuesUtil._kitchenPrintStatus;

/**
 * Created by vkompaniets on 09.07.2014.
 */
public class UpdateSaleOrderKitchenPrintStatusCommand extends AsyncCommand {

    private static final Uri URI_ORDER = ShopProvider.getContentWithLimitUri(SaleOrderTable.URI_CONTENT, 1);

    private String guid;
    private KitchenPrintStatus status;
    private SaleOrderModel order;

    @Override
    protected TaskResult doCommand() {
        order = null;
        if (status == KitchenPrintStatus.UPDATED && getOldStatus() == KitchenPrintStatus.PRINT){
            return succeeded();
        }

        order = new SaleOrderModel(guid);
        order.kitchenPrintStatus = status;

        return succeeded();
    }

    private KitchenPrintStatus getOldStatus(){
        Cursor c = ProviderAction.query(URI_ORDER)
                .projection(SaleOrderTable.KITCHEN_PRINT_STATUS)
                .where(SaleOrderTable.GUID + " = ?", guid)
                .perform(getContext());

        KitchenPrintStatus status = null;
        if (c.moveToFirst()){
            status = _kitchenPrintStatus(c, 0);
        }
        c.close();
        return status;
    }

    @Override
    protected ArrayList<ContentProviderOperation> createDbOperations() {
        if (order == null)
            return null;

        ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>(1);
        operations.add(ContentProviderOperation.newUpdate(URI_ORDER)
                .withSelection(SaleOrderTable.GUID + " = ?", new String[]{guid})
                .withValue(SaleOrderTable.KITCHEN_PRINT_STATUS, status.ordinal())
                .build());
        return operations;
    }

    @Override
    protected ISqlCommand createSqlCommand() {
        if (order == null)
            return null;

        SaleOrdersJdbcConverter converter = (SaleOrdersJdbcConverter) JdbcFactory.getConverter(SaleOrderTable.TABLE_NAME);
        return converter.updateKitchenPrintStatus(order, getAppCommandContext());
    }

    public SyncResult sync(Context context, String guid, KitchenPrintStatus status, IAppCommandContext appCommandContext){
        this.guid = guid;
        this.status = status;
        return syncDependent(context, appCommandContext);
    }

    /** use in print kitchen command. can be standalone **/
    public boolean syncStandalone(Context context, String guid, KitchenPrintStatus status, IAppCommandContext appCommandContext){
        this.guid = guid;
        this.status = status;
        return !isFailed(syncStandalone(context, appCommandContext));
    }
}
