package com.kaching123.tcr.commands.store.export;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.getbase.android.db.provider.ProviderAction;
import com.getbase.android.db.provider.Query;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore.ItemTable;
import com.telly.groundy.annotations.OnFailure;
import com.telly.groundy.annotations.OnSuccess;
import com.telly.groundy.annotations.Param;

import org.supercsv.cellprocessor.ift.CellProcessor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static com.kaching123.tcr.model.ContentValuesUtil._bool;
import static com.kaching123.tcr.model.ContentValuesUtil._decimal;
import static com.kaching123.tcr.model.ContentValuesUtil._decimalQty;

/**
 * Created by pkabakov on 06.03.14.
 */
public class ExportQuickbooksInventoryCommand extends ExportCursorToFileCommand {

    private static final Uri ITEMS_URI = ShopProvider.getContentUri(ItemTable.URI_CONTENT);

    private static final String TYPE_INVENTORY_PART = "Inventory Part";
    private static final String TYPE_NON_INVENTORY_PART = "Non-Inventory Part";
    private static final String INCOME_ACCOUNT_SALES = "Sales";
    private static final String COGS_ACCOUNT_COGS = "COGS";
    private static final String INACTIVE_STATUS_YES = "Yes";
    private static final String INACTIVE_STATUS_NO = "No";

    @Override
    protected String getFileName() {
        return "Stock_Items_Quickbooks_Export";
    }

    @Override
    protected Cursor query() {
        return createQuery()
                .perform(getContext());
    }

    protected Query createQuery() {
        return ProviderAction
                .query(ITEMS_URI)
                .projection(
                        ItemTable.STOCK_TRACKING,
                        ItemTable.DESCRIPTION,
                        ItemTable.TMP_AVAILABLE_QTY,
                        ItemTable.COST,
                        ItemTable.SALE_PRICE,
                        ItemTable.ACTIVE_STATUS,
                        ItemTable.MINIMUM_QTY,
                        ItemTable.EAN_CODE,
                        ItemTable.PRODUCT_CODE
                );
    }

    @Override
    protected List<Object> readRow(Cursor c) {
        ArrayList<Object> columns = new ArrayList<Object>(13);

        boolean stockTracking = _bool(c, c.getColumnIndex(ItemTable.STOCK_TRACKING));
        boolean isActive = _bool(c, c.getColumnIndex(ItemTable.ACTIVE_STATUS));
        BigDecimal cost = _decimal(c, c.getColumnIndex(ItemTable.COST));
        BigDecimal availableQty = _decimalQty(c, c.getColumnIndex(ItemTable.TMP_AVAILABLE_QTY));

        columns.add(stockTracking ? TYPE_INVENTORY_PART : TYPE_NON_INVENTORY_PART);
        columns.add(c.getString(c.getColumnIndex(ItemTable.DESCRIPTION)));
        columns.add(INCOME_ACCOUNT_SALES);
        columns.add(COGS_ACCOUNT_COGS);
        columns.add(_decimalQty(availableQty));
        columns.add(_decimal(cost));
        //TODO: PREFERRED VENDOR will be added in the future
        columns.add(null);
        columns.add(c.getString(c.getColumnIndex(ItemTable.SALE_PRICE)));
        columns.add(isActive ? INACTIVE_STATUS_NO : INACTIVE_STATUS_YES);
        columns.add(c.getString(c.getColumnIndex(ItemTable.MINIMUM_QTY)));
        columns.add(_decimal(cost.multiply(availableQty)));
        columns.add(c.getString(c.getColumnIndex(ItemTable.EAN_CODE)));
        columns.add(c.getString(c.getColumnIndex(ItemTable.PRODUCT_CODE)));

        return columns;
    }

    @Override
    protected String[] getHeader() {
        return new String[]{
                "TYPE",
                "DESCRIPTION",
                "Income Account",
                "EXPENSE/COGS Account",
                "ON HAND",
                "COST",
                "PREFERRED VENDOR",
                "Price",
                "ITEM IS INACTIVE",
                "REORDER POINT",
                "TOTAL VALUE",
                "PRODUCT CODE",
        };
    }

    @Override
    protected CellProcessor[] getColumns() {
        return new CellProcessor[]{
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
        };
    }

    public static void start(Context context, String fileName, ExportCommandBaseCallback callback) {
        create(ExportQuickbooksInventoryCommand.class).arg(ARG_FILENAME, fileName).callback(callback).queueUsing(context);
    }

    public static abstract class ExportCommandBaseCallback {
        @OnSuccess(ExportQuickbooksInventoryCommand.class)
        public void onSuccess(@Param(RESULT_COUNT) int count) {
            handleSuccess(count);
        }

        protected abstract void handleSuccess(int count);

        @OnFailure(ExportQuickbooksInventoryCommand.class)
        public void onFailure() {
            handleFailure();
        }

        protected abstract void handleFailure();
    }

}
