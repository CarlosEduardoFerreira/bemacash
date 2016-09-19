package com.kaching123.tcr.commands.store.saleorder;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

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
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.store.ShopStore.SaleOrderItemsView;
import com.kaching123.tcr.util.CalculationUtil;
import com.telly.groundy.TaskResult;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

/**
 * Created by vkompaniets on 16.09.2016.
 */
public class CoverEBTitemsCommand extends AsyncCommand {

    private static final Uri SALE_ITEM_URI = ShopProvider.contentUri(ShopStore.SaleItemTable.URI_CONTENT);

    private static final String ARG_ORDER_ID = "ARG_ORDER_ID";
    private static final String ARG_AMOUNT = "ARG_AMOUNT";

    private ArrayList<ContentProviderOperation> ops = new ArrayList<>();

    @Override
    protected TaskResult doCommand() {

        String orderId = getStringArg(ARG_ORDER_ID);
        BigDecimal amount = (BigDecimal) getArgs().getSerializable(ARG_AMOUNT);

        if (orderId == null || amount == null || amount.compareTo(BigDecimal.ZERO) <= 0)
            return succeeded();

        List<ItemInfo> items = loadEbtItems(getContext(), orderId);
        HashMap<String, CoverInfo> covers = new HashMap<>();
        for (ItemInfo item : items){
            BigDecimal amount2Cover = BigDecimal.ONE.subtract(item.ebtCovered).multiply(item.finalTotalPriceNoTax);
            if (amount.compareTo(amount2Cover) < 0)
                amount2Cover = amount;
            CoverInfo cover = covers.get(item.saleItemId);
            if (cover == null){
                cover = new CoverInfo(BigDecimal.ZERO, BigDecimal.ZERO);
                covers.put(item.saleItemId, cover);
            }
            cover.totalAmount = cover.totalAmount.add(item.finalTotalPriceNoTax);
            cover.coveredAmount = cover.coveredAmount.add(amount2Cover);

            amount = amount.subtract(amount2Cover);
            if (amount.compareTo(BigDecimal.ZERO) <=0)
                break;
        }

        for (Entry<String, CoverInfo> entry : covers.entrySet()){
            String saleItemId = entry.getKey();
            BigDecimal coverCoefficient = entry.getValue().getCoverCoefficient();
            ops.add(ContentProviderOperation.newUpdate(SALE_ITEM_URI)
                    .withSelection(ShopStore.SaleItemTable.SALE_ITEM_GUID + " = ?", new String[]{saleItemId})
                    .withValue(ShopStore.SaleItemTable.TMP_EBT_PAYED, coverCoefficient.toString())
                    .build()
            );
        }

        return succeeded();
    }

    private static List<ItemInfo> loadEbtItems(Context context, String orderId){
        Cursor c = ProviderAction.query(ShopProvider.contentUri(SaleOrderItemsView.URI_CONTENT))
                .projection(OrderTotalPriceLoaderCallback.PROJECTION)
                .where(SaleItemTable.ORDER_GUID + " = ? ", orderId)
                .orderBy(SaleItemTable.SEQUENCE)
                .perform(context);

        SaleOrderInfo saleOrderInfo = OrderTotalPriceLoaderCallback.readCursor(c);
        c.close();

        final ArrayList<ItemInfo> items = new ArrayList<>();
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
                BigDecimal finalTotalPriceNoTax = CalculationUtil.getSubTotal(i.qty, itemFinalPrice.subtract(itemFinalTax));
                BigDecimal finalTotalTax = CalculationUtil.getSubTotal(i.qty, itemFinalTax);

                ItemInfo itemInfo = new ItemInfo(saleItemId, finalTotalPriceNoTax, finalTotalTax, ebtCovered);
                items.add(itemInfo);
            }
        });

        Collections.sort(items, new Comparator<ItemInfo>() {
            @Override
            public int compare(ItemInfo lhs, ItemInfo rhs) {
                return -1 * lhs.finalTotalTax.compareTo(rhs.finalTotalTax);
            }
        });

        return items;
    }

    @Override
    protected ISqlCommand createSqlCommand() {
        return null;
    }

    @Override
    protected ArrayList<ContentProviderOperation> createDbOperations() {
        return ops;
    }

    private static class ItemInfo {
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

    private static class CoverInfo {
        BigDecimal totalAmount;
        BigDecimal coveredAmount;

        public CoverInfo(BigDecimal totalAmount, BigDecimal coveredAmount) {
            this.totalAmount = totalAmount;
            this.coveredAmount = coveredAmount;
        }

        BigDecimal getCoverCoefficient(){
            return coveredAmount.divide(totalAmount, 6, BigDecimal.ROUND_HALF_EVEN);
        }
    }

    public static void start(Context context, String orderId, BigDecimal amount){
        create(CoverEBTitemsCommand.class).arg(ARG_ORDER_ID, orderId).arg(ARG_AMOUNT, amount).queueUsing(context);
    }
}
