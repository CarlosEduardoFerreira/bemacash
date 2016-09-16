package com.kaching123.tcr.commands.store.saleorder;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.database.Cursor;

import com.getbase.android.db.provider.ProviderAction;
import com.kaching123.tcr.commands.store.AsyncCommand;
import com.kaching123.tcr.function.OrderTotalPriceCalculator;
import com.kaching123.tcr.function.OrderTotalPriceCalculator.Handler2;
import com.kaching123.tcr.function.OrderTotalPriceCalculator.SaleItemInfo;
import com.kaching123.tcr.function.OrderTotalPriceCalculator.SaleOrderInfo;
import com.kaching123.tcr.function.OrderTotalPriceLoaderCallback;
import com.kaching123.tcr.service.ISqlCommand;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopSchema2.SaleOrderItemsView2.SaleItemTable;
import com.kaching123.tcr.store.ShopStore.SaleOrderItemsView;
import com.kaching123.tcr.util.CalculationUtil;
import com.telly.groundy.TaskResult;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by vkompaniets on 16.09.2016.
 */
public class CoverEBTitemsCommand extends AsyncCommand {

    private static final String ARG_ORDER_ID = "ARG_ORDER_ID";
    private static final String ARG_AMOUNT = "ARG_AMOUNT";

    @Override
    protected TaskResult doCommand() {

        String orderId = getStringArg(ARG_ORDER_ID);
        BigDecimal amount = (BigDecimal) getArgs().getSerializable(ARG_AMOUNT);

        loadSaleOrderCostInfo(getContext(), orderId);

        return succeeded();
    }

    private void loadSaleOrderCostInfo(Context context, String orderId){
        Cursor c = ProviderAction.query(ShopProvider.contentUri(SaleOrderItemsView.URI_CONTENT))
                .projection(OrderTotalPriceLoaderCallback.PROJECTION)
                .where(SaleItemTable.ORDER_GUID + " = ? ", orderId)
                .orderBy(SaleItemTable.SEQUENCE)
                .perform(context);

        SaleOrderInfo saleOrderInfo = OrderTotalPriceLoaderCallback.readCursor(c);
        c.close();

        Map<String, ItemInfo> itemsMap = new HashMap<>();
        OrderTotalPriceCalculator.calculate(saleOrderInfo, new Handler2() {
            @Override
            public void splitItem(SaleItemInfo item) {

            }

            @Override
            public void handleItem(SaleItemInfo i, BigDecimal itemFinalPrice, BigDecimal itemFinalDiscount, BigDecimal itemFinalTax) {
                if (!i.isEbtEligible || i.ebtPayed.compareTo(BigDecimal.ONE) >= 0)
                    return;

                String saleItemId = i.saleItemGuid;
                BigDecimal ebtCovered = i.ebtPayed;
                BigDecimal finalTotalPriceNoTax = CalculationUtil.getSubTotal(i.qty, itemFinalPrice.subtract(itemFinalDiscount));
                BigDecimal finalTotalTax = CalculationUtil.getSubTotal(i.qty, itemFinalTax);

                ItemInfo itemInfo = new ItemInfo(saleItemId, finalTotalPriceNoTax, finalTotalTax, ebtCovered);

            }
        });
    }

    @Override
    protected ISqlCommand createSqlCommand() {
        return null;
    }

    @Override
    protected ArrayList<ContentProviderOperation> createDbOperations() {
        return null;
    }

    private class ItemInfo {
        String saleItemId;
        BigDecimal finalTotalPriceNoTax;
        BigDecimal finalTotalTax;
        BigDecimal ebtCovered;

        public ItemInfo(String saleItemId, BigDecimal finalTotalPriceNoTax, BigDecimal finalTotalTax, BigDecimal ebtCovered) {
            this.saleItemId = saleItemId;
            this.finalTotalPriceNoTax = finalTotalPriceNoTax;
            this.finalTotalTax = finalTotalTax;
            this.ebtCovered = ebtCovered;
        }
    }
}
