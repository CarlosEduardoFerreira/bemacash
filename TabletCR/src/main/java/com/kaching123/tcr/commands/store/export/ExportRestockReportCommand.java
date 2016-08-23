package com.kaching123.tcr.commands.store.export;

import android.content.Context;
import android.database.Cursor;

import com.getbase.android.db.provider.Query;
import com.kaching123.tcr.store.ShopSchema2.ExportItemView2.ItemTable;
import com.telly.groundy.annotations.OnFailure;
import com.telly.groundy.annotations.OnSuccess;
import com.telly.groundy.annotations.Param;

import org.supercsv.cellprocessor.ift.CellProcessor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static com.kaching123.tcr.model.ContentValuesUtil._decimal;
import static com.kaching123.tcr.model.ContentValuesUtil._decimalQty;

/**
 * Created by gdubina on 12.02.14.
 */
public class ExportRestockReportCommand extends ExportInventoryCommand {

    @Override
    protected String getFileName() {
        return "Re-Stock_Export";
    }

    @Override
    protected Query createQuery() {
        Query query = super.createQuery();
        query.where(ItemTable.ACTIVE_STATUS + " = ?", 1);
        query.where(ItemTable.STOCK_TRACKING + " = ?", 1);
        query.where(ItemTable.TMP_AVAILABLE_QTY + " <= " + ItemTable.MINIMUM_QTY);
        query.where(ItemTable.TMP_AVAILABLE_QTY + " < " + ItemTable.RECOMMENDED_QTY);
        return query;
    }

    @Override
    protected String[] getHeader() {
        return new String[]{
                "Description",
                "EAN\\UPC Code",
                "Product Code",
                "Cost",
                "Quantity on Hand",
                "Minimum Quantity",
                "Recommended Quantity",
                "Reorder Suggestion",
                "Cost Reorder"
        };
    }

    @Override
    protected CellProcessor[] getColumns() {
        return new CellProcessor[]{
                null, //"Description",
                null, //"UPC Code",
                null, //"Product Code",
                null, //"Cost",
                null, //"Quantity on Hand",
                null, //"Minimum Quantity",
                null, //"Recommended Quantity",
                null, //"Reorder Suggestion",
                null, //"Cost Reorder"
        };
    }

    @Override
    protected List<Object> readRow(Cursor c) {
        ArrayList<Object> columns = new ArrayList<Object>(9);

        columns.add(c.getString(c.getColumnIndex(ItemTable.DESCRIPTION)));

        columns.add(c.getString(c.getColumnIndex(ItemTable.EAN_CODE)));

        columns.add(c.getString(c.getColumnIndex(ItemTable.PRODUCT_CODE)));

        BigDecimal cost = _decimal(c, c.getColumnIndex(ItemTable.COST), BigDecimal.ZERO);
        columns.add(_decimal(cost));

        String onHand = c.getString(c.getColumnIndex(ItemTable.TMP_AVAILABLE_QTY));
        columns.add(onHand);

        BigDecimal minQty = _decimalQty(c, c.getColumnIndex(ItemTable.MINIMUM_QTY), BigDecimal.ZERO);
        columns.add(_decimalQty(minQty));

        BigDecimal recommended = _decimalQty(c, c.getColumnIndex(ItemTable.RECOMMENDED_QTY), BigDecimal.ZERO);
        columns.add(_decimalQty(recommended));

        BigDecimal reorder = recommended.subtract(_decimalQty(onHand));
        columns.add(_decimalQty(reorder));

        BigDecimal reorderCost = reorder.multiply(cost);
        columns.add(_decimal(reorderCost));

        return columns;
    }

    public static void start(Context context, String fileName, ExportCommandBaseCallback callback) {
        create(ExportRestockReportCommand.class).arg(ARG_FILENAME, fileName).callback(callback).queueUsing(context);
    }

    public static abstract class ExportCommandBaseCallback {

        @OnSuccess(ExportRestockReportCommand.class)
        public void onSuccess(@Param(RESULT_COUNT) int count) {
            handleSuccess(count);
        }

        protected abstract void handleSuccess(int count);

        @OnFailure(ExportRestockReportCommand.class)
        public void onFailure() {
            handleFailure();
        }

        protected abstract void handleFailure();
    }
}
