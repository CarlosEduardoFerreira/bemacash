package com.kaching123.tcr.commands.store.export;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.getbase.android.db.provider.ProviderAction;
import com.getbase.android.db.provider.Query;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopSchema2.ExportItemView2.CategoryTable;
import com.kaching123.tcr.store.ShopSchema2.ExportItemView2.DepartmentTable;
import com.kaching123.tcr.store.ShopSchema2.ExportItemView2.ItemMatrixTable;
import com.kaching123.tcr.store.ShopSchema2.ExportItemView2.ItemTable;
import com.kaching123.tcr.store.ShopSchema2.ExportItemView2.SaleOrderTable;
import com.kaching123.tcr.store.ShopSchema2.ExportItemView2.UnitLabelTable;
import com.kaching123.tcr.store.ShopStore.ExportItemView;
import com.kaching123.tcr.util.DateUtils;
import com.telly.groundy.annotations.OnFailure;
import com.telly.groundy.annotations.OnSuccess;
import com.telly.groundy.annotations.Param;

import org.supercsv.cellprocessor.ift.CellProcessor;

import java.util.ArrayList;
import java.util.List;

import static com.kaching123.tcr.model.ContentValuesUtil._bool;

/**
 * Created by gdubina on 20/01/14.
 */
public class ExportInventoryCommand extends ExportCursorToFileCommand {

    private static final Uri EXPORT_ITEM_URI = ShopProvider.getContentUriGroupBy(ExportItemView.URI_CONTENT, ItemTable.GUID);

    @Override
    protected String getFileName() {
        return "Stock_Items_Export";
    }

    @Override
    protected Cursor query() {
        return createQuery()
                .perform(getContext());
    }

    protected Query createQuery() {
        return ProviderAction
                .query(EXPORT_ITEM_URI)
                .projection(
                        ItemTable.GUID,
                        ItemTable.DESCRIPTION,
                        DepartmentTable.TITLE,
                        CategoryTable.TITLE,
                        UnitLabelTable.SHORTCUT,
                        ItemTable.EAN_CODE,
                        ItemTable.PRODUCT_CODE,
                        ItemTable.SALE_PRICE,
                        ItemMatrixTable.PARENT_GUID,
                        ItemTable.DISCOUNTABLE,
                        ItemTable.SALABLE,
                        ItemTable.TAXABLE,
                        ItemTable.STOCK_TRACKING,
                        ItemTable.LIMIT_QTY,
                        ItemTable.COST,
                        ItemTable.TMP_AVAILABLE_QTY,
                        ItemTable.MINIMUM_QTY,
                        ItemTable.RECOMMENDED_QTY,
                        ItemTable.ITEM_REF_TYPE,
                        ItemTable.REFERENCE_ITEM_ID,
                        "max(" + SaleOrderTable.CREATE_TIME + ") as " + SaleOrderTable.CREATE_TIME
                );
    }

    @Override
    protected List<Object> readRow(Cursor c) {
        ArrayList<Object> columns = new ArrayList<>();
        columns.add(c.getString(c.getColumnIndex(ItemTable.GUID)));
        columns.add(c.getString(c.getColumnIndex(ItemTable.DESCRIPTION)));
        columns.add(c.getString(c.getColumnIndex(DepartmentTable.TITLE)));
        columns.add(c.getString(c.getColumnIndex(CategoryTable.TITLE)));
        columns.add(c.getString(c.getColumnIndex(UnitLabelTable.SHORTCUT)));
        columns.add(c.getString(c.getColumnIndex(ItemTable.EAN_CODE)));
        columns.add(c.getString(c.getColumnIndex(ItemTable.PRODUCT_CODE)));
        columns.add(c.getString(c.getColumnIndex(ItemTable.SALE_PRICE)));
        String referenceItemGuid = c.isNull(c.getColumnIndex(ItemMatrixTable.PARENT_GUID)) ?
                c.getString(c.getColumnIndex(ItemTable.REFERENCE_ITEM_ID))
                : c.getString(c.getColumnIndex(ItemMatrixTable.PARENT_GUID));
        columns.add(referenceItemGuid);
        columns.add(_bool(c, c.getColumnIndex(ItemTable.DISCOUNTABLE)));
        columns.add(_bool(c, c.getColumnIndex(ItemTable.SALABLE)));
        columns.add(_bool(c, c.getColumnIndex(ItemTable.TAXABLE)));
        columns.add(_bool(c, c.getColumnIndex(ItemTable.STOCK_TRACKING)));
        columns.add(_bool(c, c.getColumnIndex(ItemTable.LIMIT_QTY)));
        columns.add(c.getString(c.getColumnIndex(ItemTable.COST)));
        columns.add(c.getString(c.getColumnIndex(ItemTable.TMP_AVAILABLE_QTY)));
        columns.add(c.getString(c.getColumnIndex(ItemTable.MINIMUM_QTY)));
        columns.add(c.getString(c.getColumnIndex(ItemTable.RECOMMENDED_QTY)));
        columns.add(String.valueOf(_bool(c, c.getColumnIndex(ItemTable.ITEM_REF_TYPE))));
        columns.add(DateUtils.dateOnlyFormat(c.getLong(c.getColumnIndex(SaleOrderTable.CREATE_TIME))));
        return columns;
    }

    @Override
    protected String[] getHeader() {
        return new String[]{
                "Item ID",
                "Description",
                "Department",
                "Category",
                "Units Label",
                "EAN\\UPC Code",
                "Product Code",
                "Price",
                "Reference Product",
                "Discountable",
                "Salable",
                "Taxable",
                "STOCK_TRACK",
                "Limit Qty",
                "Cost",
                "Quantity on Hand",
                "Order Trigger",
                "Recommended Order",
                "Is Reference",
                "Last Sold Date"
        };
    }

    @Override
    protected CellProcessor[] getColumns() {
        return new CellProcessor[]{
                null, //"Item ID",
                null, //"Description",
                null, //"Department",
                null, //"Category",
                null, //"Label",
                null, //"UPC Code",
                null, //"Product Code",
                null, //"Price",
                null, //"Reference Product",
                null, //"Discountable",
                null, //"Salable",
                null, //"Taxable",
                null, //"STOCK_TRACK",
                null, //"Limit qty",
                null, //"Cost",
                null, //"Quantity on Hand",
                null, //"Order Trigger",
                null, //"Recommended Order",
                null, //"Is Reference",
                null //"Last Sold Date",

        };
    }

    public static void start(Context context, String fileName, ExportCommandBaseCallback callback) {
        create(ExportInventoryCommand.class).arg(ARG_FILENAME, fileName).callback(callback).queueUsing(context);
    }

    public static abstract class ExportCommandBaseCallback {
        @OnSuccess(ExportInventoryCommand.class)
        public void onSuccess(@Param(RESULT_COUNT) int count) {
            handleSuccess(count);
        }

        protected abstract void handleSuccess(int count);

        @OnFailure(ExportInventoryCommand.class)
        public void onFailure() {
            handleFailure();
        }

        protected abstract void handleFailure();
    }

}
