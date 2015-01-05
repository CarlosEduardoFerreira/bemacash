package com.kaching123.tcr.reports;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.getbase.android.db.provider.ProviderAction;
import com.kaching123.tcr.R;
import com.kaching123.tcr.fragment.reports.InventoryValueFragment.Info;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore.ItemTable;
import com.kaching123.tcr.util.CalculationUtil;

import java.math.BigDecimal;
import java.util.ArrayList;

import static com.kaching123.tcr.model.ContentValuesUtil._decimal;
import static com.kaching123.tcr.model.ContentValuesUtil._decimalQty;

/**
 * Created by vkompaniets on 06.02.14.
 */
public class InventoryValueQuery {

    private static final Uri URI_ITEMS = ShopProvider.getContentUri(ItemTable.URI_CONTENT);

    public static ArrayList<Info> getItems(Context context){

        Cursor c = ProviderAction.query(URI_ITEMS)
                .projection(ItemTable.TMP_AVAILABLE_QTY, ItemTable.COST)
                .perform(context);

        int items = 0;
        BigDecimal qty = BigDecimal.ZERO;
        BigDecimal cost = BigDecimal.ZERO;
        while (c.moveToNext()) {
            BigDecimal q = _decimalQty(c.getString(0));
            BigDecimal cc = _decimal(c.getString(1));

            qty = qty.add(q);
            cost = cost.add(CalculationUtil.getSubTotal(q, cc));
            items++;
        }
        c.close();

        ArrayList<Info> info = new ArrayList<Info>(3);
        info.add(new Info(R.string.report_inventory_value_count, new BigDecimal(items)));
        info.add(new Info(R.string.report_inventory_value_qty, qty));
        info.add(new Info(R.string.report_inventory_value_value, cost));

        return info;
    }
}
