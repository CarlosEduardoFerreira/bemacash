package com.kaching123.tcr.commands.store.export;

import android.database.Cursor;

import com.kaching123.tcr.reports.InventoryStatusReportQuery;
import com.kaching123.tcr.store.ShopSchema2.InventoryStatusReportView2.CategoryTable;
import com.kaching123.tcr.store.ShopSchema2.InventoryStatusReportView2.DepartmentTable;
import com.kaching123.tcr.store.ShopSchema2.SaleOrderItemsView2.ItemTable;
import com.telly.groundy.TaskResult;

import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.ICsvListWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by vkompaniets on 06.03.14.
 */
public class ExportInventoryStatusCommand extends ExportToFileCommand {

    private String depGuid;

    @Override
    protected TaskResult doInBackground() {
        depGuid = getStringArg(ReportArgs.ARG_DEPARTMENT_GUID);
        return super.doInBackground();
    }

    @Override
    protected int writeBody(ICsvListWriter writer, CellProcessor[] processors) throws IOException {
        /*List<DepInfo> depInfos = _wrap(InventoryStatusReportQuery
                .unitQuery(depGuid)
                .perform(getContext()),
                new DepInfoWrapFunction());
        int count = 0;
        for (DepInfo depInfo : depInfos){
            writer.write(depInfo.title, null, null, null, null);
            BigDecimal totalCost = BigDecimal.ZERO;
            for (ItemInfo item : depInfo.items){
                writer.write(item.title, _decimal(item.onHand), _decimal(item.unitCost), _decimal(item.totalCost), item.active ? "+" : "-");
                totalCost = totalCost.add(item.totalCost);
                count++;
            }
            writer.write(null, null, "TOTAL COST", _decimal(totalCost));
        }*/

        Cursor c = InventoryStatusReportQuery
                .syncQuery(depGuid)
                .perform(getContext());

        int count = c.getCount();
        while (c.moveToNext()){
            writer.write(readRow(c));
        }
        c.close();

        return count;
    }

    private List<String> readRow(Cursor c){
        ArrayList<String> columns = new ArrayList<String>(9);
        columns.add(c.getString(c.getColumnIndex(ItemTable.GUID)));
        columns.add(c.getString(c.getColumnIndex(ItemTable.DESCRIPTION)));
        columns.add(c.getString(c.getColumnIndex(DepartmentTable.TITLE)));
        columns.add(c.getString(c.getColumnIndex(CategoryTable.TITLE)));
        columns.add(c.getString(c.getColumnIndex(ItemTable.EAN_CODE)));
        columns.add(c.getString(c.getColumnIndex(ItemTable.PRODUCT_CODE)));
        columns.add(c.getString(c.getColumnIndex(ItemTable.TMP_AVAILABLE_QTY)));
        columns.add(c.getString(c.getColumnIndex(ItemTable.COST)));
        columns.add(c.getInt(c.getColumnIndex(ItemTable.ACTIVE_STATUS)) == 1 ? "YES" : "NO");
        return columns;
    }



    @Override
    protected String getFileName() {
        return "Inventory_Status_Report";
    }

    @Override
    protected String[] getHeader() {
        /*Item ID,Description,Department,Category,ON-HAND,UNIT-COST,ACTIVE*/
        return new String[]{
                "Item ID",
                "Description",
                "Department",
                "Category",
                "EAN//UPC Code",
                "Product Code",
                "ON-HAND",
                "UNIT-COST",
                "ACTIVE"
        };
    }

    @Override
    protected CellProcessor[] getColumns() {
        return null;
    }
}
