package com.kaching123.tcr.reports;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.content.Loader;
import android.text.TextUtils;

import com.getbase.android.db.loaders.CursorLoaderBuilder;
import com.getbase.android.db.provider.ProviderAction;
import com.getbase.android.db.provider.Query;
import com.kaching123.tcr.model.converter.ListConverterFunction;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopSchema2.InventoryStatusReportView2.CategoryTable;
import com.kaching123.tcr.store.ShopSchema2.InventoryStatusReportView2.DepartmentTable;
import com.kaching123.tcr.store.ShopSchema2.InventoryStatusReportView2.ItemTable;
import com.kaching123.tcr.store.ShopStore.InventoryStatusReportView;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import static com.kaching123.tcr.model.ContentValuesUtil._decimal;
import static com.kaching123.tcr.model.ContentValuesUtil._decimalQty;
import static com.kaching123.tcr.util.CalculationUtil.getSubTotal;

/**
 * Created by vkompaniets on 04.03.14.
 */
public class InventoryStatusReportQuery {

    private static final Uri URI_ITEMS = ShopProvider.getContentUri(InventoryStatusReportView.URI_CONTENT);

    public static Loader<List<DepInfo>> query(Context context, String depGuid){
        CursorLoaderBuilder loader = CursorLoaderBuilder.forUri(URI_ITEMS);
        if (!TextUtils.isEmpty(depGuid)){
            loader.where(CategoryTable.DEPARTMENT_GUID + " = ?", depGuid);
        }else{
            loader.orderBy(CategoryTable.DEPARTMENT_GUID);
        }

        return loader.wrap(new DepInfoWrapFunction()).build(context);
    }

    public static Query syncQuery(String depGuid){
        Query query = ProviderAction.query(URI_ITEMS);
        if (!TextUtils.isEmpty(depGuid)){
            query.where(CategoryTable.DEPARTMENT_GUID + " = ?", depGuid);
        }else{
            query.orderBy(CategoryTable.DEPARTMENT_GUID);
        }

        return query;
    }

    public static class DepInfoWrapFunction extends ListConverterFunction<List<DepInfo>> {

        private ItemInfoFunction itemInfoFunction = new ItemInfoFunction();

        @Override
        public List<DepInfo> apply(Cursor c) {
            if (!c.moveToFirst()){
                return new ArrayList<DepInfo>();
            }

            LinkedHashMap<String, DepInfo> inventoryStatusMap = new LinkedHashMap<String, DepInfo>();
            do {
                String depGuid = c.getString(c.getColumnIndex(CategoryTable.DEPARTMENT_GUID));
                DepInfo dep = inventoryStatusMap.get(depGuid);
                if (dep == null){
                    dep = new DepInfo(c.getString(c.getColumnIndex(DepartmentTable.TITLE)));
                    inventoryStatusMap.put(depGuid, dep);
                }
                ItemInfo item = itemInfoFunction.apply(c);
                dep.items.add(item);
                if (item.active){
                    dep.totalCost = dep.totalCost.add(item.totalCost);
                }
            }while (c.moveToNext());

            return new ArrayList<DepInfo>(inventoryStatusMap.values());
        }

    }

    private static class ItemInfoFunction extends ListConverterFunction<ItemInfo> {

        @Override
        public ItemInfo apply(Cursor c) {
            super.apply(c);
            return new ItemInfo(
                    c.getString(c.getColumnIndex(ItemTable.DESCRIPTION)),
                    c.getString(c.getColumnIndex(ItemTable.EAN_CODE)),
                    c.getString(c.getColumnIndex(ItemTable.PRODUCT_CODE)),
                    _decimalQty(c.getString(c.getColumnIndex(ItemTable.TMP_AVAILABLE_QTY))),
                    _decimal(c.getString(c.getColumnIndex(ItemTable.COST)), BigDecimal.ZERO),
                    c.getInt(c.getColumnIndex(ItemTable.ACTIVE_STATUS)) == 1
            );
        }
    }

    public static class ItemInfo {
        public String title;
        public String ean;
        public String productCode;
        public BigDecimal onHand;
        public BigDecimal unitCost;
        public BigDecimal totalCost;
        public boolean active;

        public ItemInfo(String title, String ean, String productCode, BigDecimal onHand, BigDecimal unitCost, boolean active) {
            this.title = title;
            this.ean = ean;
            this.productCode = productCode;
            this.onHand = onHand;
            this.unitCost = unitCost;
            this.active = active;
            this.totalCost = getSubTotal(onHand, unitCost);
        }
    }

    public static class DepInfo {
        public String title;
        public BigDecimal totalCost = BigDecimal.ZERO;

        public ArrayList<ItemInfo> items = new ArrayList<ItemInfo>();

        public DepInfo(String title) {
            this.title = title;
        }
    }

}
