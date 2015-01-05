package com.kaching123.tcr.commands.store.export;

import android.database.Cursor;
import android.net.Uri;

import com.getbase.android.db.provider.ProviderAction;
import com.getbase.android.db.provider.Query;
import com.kaching123.tcr.model.OrderStatus;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopSchema2.ExportSoldItemsView2.CategoryTable;
import com.kaching123.tcr.store.ShopSchema2.ExportSoldItemsView2.DepartmentTable;
import com.kaching123.tcr.store.ShopSchema2.ExportSoldItemsView2.ItemTable;
import com.kaching123.tcr.store.ShopSchema2.ExportSoldItemsView2.SaleItemTable;
import com.kaching123.tcr.store.ShopSchema2.ExportSoldItemsView2.SaleOrderTable;
import com.kaching123.tcr.store.ShopSchema2.ExportTopItemsView2;
import com.kaching123.tcr.store.ShopStore.ExportSoldItemsView;
import com.kaching123.tcr.util.CalculationUtil;
import com.telly.groundy.TaskResult;

import org.supercsv.cellprocessor.FmtDate;
import org.supercsv.cellprocessor.ift.CellProcessor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.kaching123.tcr.model.ContentValuesUtil._decimal;
import static com.kaching123.tcr.model.ContentValuesUtil._decimalQty;

/**
 * Created by gdubina on 30.01.14.
 */
public class ExportReturnedtemsCommand extends ExportCursorToFileCommand {

    private static final Uri EXPORT_ITEM_URI = ShopProvider.getContentUri(ExportSoldItemsView.URI_CONTENT);

    private long startTime;
    private long endTime;
    private long registerId;

    @Override
    protected TaskResult doInBackground() {
        startTime = getLongArg(ReportArgs.ARG_START_TIME);
        endTime = getLongArg(ReportArgs.ARG_END_TIME);
        registerId = getLongArg(ReportArgs.ARG_REGISTER_GUID);
        return super.doInBackground();
    }

    @Override
    protected String getFileName() {
        return "Returned_Items_Export";
    }

    @Override
    protected String[] getHeader() {
        return new String[]{
                "Return Record ID",
                "Return Date",
                "Customer Record ID",
                "Item Description",
                "Department",
                "Category",
                "EAN\\UPC Code",
                "Product Code",
                "Item ID",
                "Unit Price",
                "Quantity",
                "Total Price",
                "Discount",
                "Total",
                "COGS"
        };
    }

    @Override
    protected CellProcessor[] getColumns() {
        return new CellProcessor[]{
                null,
                new FmtDate("MM/dd/yyyy HH:mm"),
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
                null
        };
    }

    @Override
    protected Cursor query() {
        Query query = ProviderAction
                .query(EXPORT_ITEM_URI)
                .projection(
                        SaleItemTable.SALE_ITEM_GUID,
                        SaleOrderTable.CREATE_TIME,
                        SaleOrderTable.CUSTOMER_GUID,
                        ItemTable.DESCRIPTION,
                        DepartmentTable.TITLE,
                        CategoryTable.TITLE,
                        ItemTable.EAN_CODE,
                        ItemTable.PRODUCT_CODE,
                        ItemTable.GUID,
                        SaleItemTable.FINAL_GROSS_PRICE,
                        SaleItemTable.QUANTITY,
                        //nothing for total price
                        SaleItemTable.FINAL_DISCOUNT,
                        //nothing for total
                        ItemTable.COST
                )
                .where(SaleOrderTable.STATUS + " = ?", OrderStatus.RETURN.ordinal())
                .where(SaleOrderTable.CREATE_TIME + " >= ? and " + SaleOrderTable.CREATE_TIME + " <= ?", startTime, endTime)
                .orderBy(ItemTable.DESCRIPTION);
        if (registerId > 0) {
            query.where(ExportTopItemsView2.SaleOrderTable.REGISTER_ID + " = ?", registerId);
        }
        return query.perform(getContext());
    }

    @Override
    protected List<Object> readRow(Cursor c) {
        ArrayList<Object> result = new ArrayList<Object>(14);
        result.add(c.getString(0));
        result.add(new Date(c.getLong(1)));
        result.add(c.getString(2));
        result.add(c.getString(3));
        result.add(c.getString(4));
        result.add(c.getString(5));
        result.add(c.getString(6));//ean
        result.add(c.getString(7));//product code
        result.add(c.getString(8));//item guid

        BigDecimal grossPrice = _decimal(c.getString(9));
        BigDecimal qty = _decimalQty(c.getString(10));

        result.add(_decimal(grossPrice));//final gross
        result.add(_decimalQty(qty));
        result.add(_decimal(CalculationUtil.getSubTotal(qty, grossPrice)));

        BigDecimal finalDiscount = _decimal(c.getString(11));
        result.add(_decimal(finalDiscount));

        BigDecimal total = CalculationUtil.getSubTotal(qty, grossPrice.subtract(finalDiscount));
        result.add(_decimal(total));

        BigDecimal cost = _decimal(c.getString(12));
        result.add(_decimal(CalculationUtil.getSubTotal(qty, cost)));
        return result;
    }
}
