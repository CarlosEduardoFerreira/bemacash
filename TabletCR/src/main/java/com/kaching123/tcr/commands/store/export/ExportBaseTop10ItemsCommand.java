package com.kaching123.tcr.commands.store.export;

import android.database.Cursor;
import android.net.Uri;

import com.getbase.android.db.provider.ProviderAction;
import com.getbase.android.db.provider.Query;
import com.kaching123.tcr.model.OrderStatus;
import com.kaching123.tcr.model.OrderType;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopSchema2.ExportTopItemsView2.ItemTable;
import com.kaching123.tcr.store.ShopSchema2.ExportTopItemsView2.SaleItemTable;
import com.kaching123.tcr.store.ShopSchema2.ExportTopItemsView2.SaleOrderTable;
import com.kaching123.tcr.store.ShopStore.ExportTopItemsView;
import com.telly.groundy.TaskResult;

import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.ICsvListWriter;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import static com.kaching123.tcr.model.ContentValuesUtil._decimal;
import static com.kaching123.tcr.model.ContentValuesUtil._decimalQty;
import static com.kaching123.tcr.model.ContentValuesUtil._orderStatus;
import static com.kaching123.tcr.util.CalculationUtil.divide;
import static com.kaching123.tcr.util.CalculationUtil.getSubTotal;

/**
 * Created by gdubina on 29.01.14.
 */
public abstract class ExportBaseTop10ItemsCommand extends ExportToFileCommand {

    private static final Uri SALE_ITEM_URI = ShopProvider.getContentUri(ExportTopItemsView.URI_CONTENT);

    private long startTime;
    private long endTime;
    private long registerId;
    protected OrderType orderType;

    @Override
    protected TaskResult doInBackground() {
        startTime = getLongArg(ReportArgs.ARG_START_TIME);
        endTime = getLongArg(ReportArgs.ARG_END_TIME);
        registerId = getLongArg(ReportArgs.ARG_REGISTER_GUID);
        orderType = (OrderType) getArgs().getSerializable(ReportArgs.ARG_ORDER_TYPE);
        return super.doInBackground();
    }

    @Override
    protected String[] getHeader() {
        return new String[]{
                "Description",
                "Current Price",
                "Last Cost",
                "On Hand",
                "Qty. Sold",
                "Qty. Returned",
                "Total Sales Net of Returns",
                "Discount",
                "Net Sales",
                "Gross Profit"
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
                null
        };
    }

    @Override
    protected int writeBody(ICsvListWriter writer, CellProcessor[] processors) throws IOException {
        HashMap<String, ReportItem> items = loadData();
        ArrayList<ReportItem> result = new ArrayList<ReportItem>(items.values());
        Collections.sort(result, getComparator());
        List<ReportItem> exportItems = result.subList(0, Math.min(10, result.size()));
        for(ReportItem i : exportItems){
            List<Object> row = readRow(i);
            writer.write(row, processors);
        }
        return exportItems.size();
    }

    protected List<Object> readRow(ReportItem i) {
        BigDecimal qty = i.qtySold.add(i.qtyReturn);

        BigDecimal curPrice = divide(i.grossSold.add(i.grossReturn), qty);
        BigDecimal total = getSubTotal(qty, curPrice);
        BigDecimal discount = i.discountSold.add(i.discountReturn);
        BigDecimal net = total.subtract(discount);

        ArrayList<Object> result = new ArrayList<Object>(10);
        result.add(i.description);
        result.add(_decimal(curPrice));
        result.add(_decimal(i.cost));
        result.add(_decimalQty(i.onHand));
        result.add(_decimalQty(i.qtySold));
        result.add(_decimalQty(i.qtyReturn));
        result.add(_decimal(total));
        result.add(_decimal(discount));
        result.add(_decimal(net));
        result.add(_decimal(net.subtract(getSubTotal(qty, i.cost))));
        return result;
    }

    protected abstract Comparator<ReportItem> getComparator();

    private HashMap<String, ReportItem> loadData() {
        Query query = ProviderAction
                .query(SALE_ITEM_URI)
                .projection(
                        SaleItemTable.ITEM_GUID,
                        SaleOrderTable.STATUS,
                        SaleItemTable.QUANTITY,
                        SaleItemTable.FINAL_GROSS_PRICE,
                        SaleItemTable.FINAL_TAX,
                        SaleItemTable.FINAL_DISCOUNT,

                        ItemTable.DESCRIPTION,
                        ItemTable.COST,
                        ItemTable.TMP_AVAILABLE_QTY
                )
                .where("(" + SaleOrderTable.STATUS + " = ? or " + SaleOrderTable.STATUS + " = ?)", OrderStatus.COMPLETED.ordinal(), OrderStatus.RETURN.ordinal())
                .where(SaleOrderTable.CREATE_TIME + " >= ? and " + SaleOrderTable.CREATE_TIME + " <= ?", startTime, endTime);
        if (registerId > 0) {
            query.where(SaleOrderTable.REGISTER_ID + " = ?", registerId);
        }
        if (orderType != null) {
            query.where(SaleOrderTable.ORDER_TYPE + " = ?", orderType.ordinal());
        }
        HashMap<String, ReportItem> result = new HashMap<String, ReportItem>();
        Cursor c = query.perform(getContext());
        while (c.moveToNext()) {
            String itemGuid = c.getString(0);
            ReportItem item = result.get(itemGuid);
            if (item == null) {
                result.put(itemGuid, item = new ReportItem(
                        c.getString(c.getColumnIndex(ItemTable.DESCRIPTION)),
                        _decimal(c, c.getColumnIndex(ItemTable.COST), BigDecimal.ZERO),
                        _decimalQty(c, c.getColumnIndex(ItemTable.TMP_AVAILABLE_QTY), BigDecimal.ZERO)));
            }
            if (_orderStatus(c, c.getColumnIndex(SaleOrderTable.STATUS)) == OrderStatus.RETURN) {
                item.addReturn(
                        _decimalQty(c, c.getColumnIndex(SaleItemTable.QUANTITY), BigDecimal.ZERO),
                        _decimal(c, c.getColumnIndex(SaleItemTable.FINAL_GROSS_PRICE), BigDecimal.ZERO),
                        _decimal(c, c.getColumnIndex(SaleItemTable.FINAL_TAX), BigDecimal.ZERO),
                        _decimal(c, c.getColumnIndex(SaleItemTable.FINAL_DISCOUNT), BigDecimal.ZERO)
                );
            } else {
                item.addSale(
                        _decimalQty(c, c.getColumnIndex(SaleItemTable.QUANTITY), BigDecimal.ZERO),
                        _decimal(c, c.getColumnIndex(SaleItemTable.FINAL_GROSS_PRICE), BigDecimal.ZERO),
                        _decimal(c, c.getColumnIndex(SaleItemTable.FINAL_TAX), BigDecimal.ZERO),
                        _decimal(c, c.getColumnIndex(SaleItemTable.FINAL_DISCOUNT), BigDecimal.ZERO)
                );
            }

        }
        c.close();
        return result;
    }

    protected class ReportItem {
        String description;
        BigDecimal cost;
        BigDecimal onHand;

        BigDecimal qtySold = BigDecimal.ZERO;
        BigDecimal qtyReturn = BigDecimal.ZERO;

        BigDecimal grossSold = BigDecimal.ZERO;
        BigDecimal grossReturn = BigDecimal.ZERO;

        BigDecimal discountSold = BigDecimal.ZERO;
        BigDecimal discountReturn = BigDecimal.ZERO;

        BigDecimal taxSold = BigDecimal.ZERO;
        BigDecimal taxReturn = BigDecimal.ZERO;

        private ReportItem(String description, BigDecimal cost, BigDecimal onHand) {
            this.description = description;
            this.cost = cost;
            this.onHand = onHand;
        }

        public void addReturn(BigDecimal qty, BigDecimal grossPrice, BigDecimal tax, BigDecimal discount) {
            qtyReturn = qtyReturn.add(qty);
            grossReturn = grossReturn.add(getSubTotal(qty, grossPrice));
            discountReturn = discountReturn.add(getSubTotal(qty, discount));
            taxReturn = taxReturn.add(getSubTotal(qty, tax));
        }

        public void addSale(BigDecimal qty, BigDecimal grossPrice, BigDecimal tax, BigDecimal discount) {
            qtySold = qtySold.add(qty);
            grossSold = grossSold.add(getSubTotal(qty, grossPrice));
            discountSold = discountSold.add(getSubTotal(qty, discount));
            taxSold = taxSold.add(getSubTotal(qty, tax));
        }
    }
/*
    @Override
    protected Cursor query() {
        Query query = ProviderAction
                .query(SOLD_ITEM_GROUP_URI)
                .projection(
                        SaleItemTable.ITEM_GUID,
                        "sum(" + SaleItemTable.QUANTITY + ")")
                .where("(" + SaleOrderTable.STATUS + " = ? or " + SaleOrderTable.STATUS + " = ?)", OrderStatus.COMPLETED.ordinal(), OrderStatus.RETURN.ordinal())
                .where(SaleOrderTable.CREATE_TIME + " >= ? and " + SaleOrderTable.CREATE_TIME + " <= ?", startTime, endTime)
                .orderBy(orderField() + " DESC");
        if (registerId > 0) {
            query.where(SaleOrderTable.REGISTER_ID + " = ?", registerId);
        }
        return query.perform(getContext());
    }

    protected abstract String orderField();

    @Override
    protected List<Object> readRow(Cursor c) {
        ItemInfo info = readeItemInfo(c.getString(0));
        ArrayList<Object> result = new ArrayList<Object>(10);
        result.add(info.description);
        result.add(_decimal(info.curPrice));
        result.add(_decimal(info.cost));
        result.add(_decimal(info.available));
        result.add(_decimal(info.soldQty));
        result.add(_decimal(info.returnedQty));
        result.add(_decimal(info.returnedNet));
        result.add(_decimal(info.discount));
        result.add(_decimal(info.soldNet));
        result.add(_decimal(info.profit));
        return result;
    }

    private ItemInfo readeItemInfo(String itemGuid) {
        Cursor c = ProviderAction
                .query(ITEM_URI)
                .projection(
                        ItemTable.DESCRIPTION,
                        //ItemTable.SALE_PRICE,
                        ItemTable.COST,
                        ItemTable.TMP_AVAILABLE_QTY
                )
                .where(ItemTable.GUID + " = ?", itemGuid)
                .perform(getContext());

        ItemInfo info = new ItemInfo();
        if (c.moveToFirst()) {
            info.description = c.getString(0);
            //info.curPrice = _decimal(c.getString(1));
            info.cost = _decimal(c.getString(1));
            info.available = _decimal(c.getString(2));
        }
        c.close();

        Query query = ProviderAction
                .query(SOLD_ITEM_URI)
                .projection(
                        SaleItemTable.QUANTITY,
                        SaleItemTable.TMP_REFUND_QUANTITY,
                        SaleItemTable.FINAL_GROSS_PRICE,
                        SaleItemTable.FINAL_TAX,
                        SaleItemTable.FINAL_DISCOUNT
                )
                .where(SaleOrderTable.STATUS + " = ?", OrderStatus.COMPLETED.ordinal())
                .where(SaleItemTable.ITEM_GUID + " = ?", itemGuid)
                .where(SaleOrderTable.CREATE_TIME + " >= ? and " + SaleOrderTable.CREATE_TIME + " <= ?", startTime, endTime);
        if (registerId > 0) {
            query.where(SaleOrderTable.REGISTER_ID + " = ?", registerId);
        }
        c = query.perform(getContext());

        BigDecimal totalQty = BigDecimal.ZERO;
        BigDecimal totalRetQty = BigDecimal.ZERO;
        BigDecimal totalDiscount = BigDecimal.ZERO;

        //BigDecimal totalNetSale = BigDecimal.ZERO;
        BigDecimal totalReturnNetSale = BigDecimal.ZERO;

        //BigDecimal totalSaleSum = BigDecimal.ZERO;

        while (c.moveToNext()) {
            BigDecimal recordQty = _decimal(c.getString(0));
            BigDecimal recordReturns = _decimal(c.getString(1));
            BigDecimal diffQty = recordQty.add(recordReturns);

            BigDecimal recordGrossPrice = _decimal(c.getString(2));
            BigDecimal recordTax = _decimal(c.getString(3));
            BigDecimal recordDiscount = _decimal(c.getString(4));

            *//*BigDecimal netSalePrice = grossPrice.subtract(discount);
            BigDecimal priceAndTax = netSalePrice.add(tax);*//*

            *//*BigDecimal netSale = qty.multiply(netSalePrice);
            BigDecimal returnNetSale = returns.multiply(netSalePrice);
            netSale = netSale.subtract(returnNetSale);*//*

            //BigDecimal totalSum = qty.multiply(priceAndTax).subtract(returns.multiply(priceAndTax));


            BigDecimal returnNetSale = getSubTotal(diffQty, recordGrossPrice);
            BigDecimal discount = getSubTotal(diffQty, recordDiscount);

            totalQty = totalQty.add(recordQty);
            totalRetQty = totalRetQty.add(recordReturns);
            totalDiscount = totalDiscount.add(discount);
            //totalNetSale = totalNetSale.add(netSale);
            totalReturnNetSale = totalReturnNetSale.add(returnNetSale);
            //totalSaleSum = totalSaleSum.add(totalSum);
        }
        c.close();

        BigDecimal diffQty = totalQty.add(totalRetQty);
        info.curPrice = CalculationUtil.divide(totalReturnNetSale, diffQty);
        info.soldQty = totalQty;
        info.returnedQty = totalRetQty;

        info.returnedNet = totalReturnNetSale;
        info.discount = totalDiscount;

        info.soldNet = totalReturnNetSale.subtract(totalDiscount);
        info.profit = info.soldNet.subtract(getSubTotal(diffQty, info.cost));
        return info;
    }

    private static class ItemInfo {
        String description;
        BigDecimal curPrice;
        BigDecimal cost;
        BigDecimal available;

        BigDecimal soldQty;
        BigDecimal returnedQty;

        BigDecimal soldNet;
        BigDecimal returnedNet;

        BigDecimal discount;
        BigDecimal profit;
    }*/

}
