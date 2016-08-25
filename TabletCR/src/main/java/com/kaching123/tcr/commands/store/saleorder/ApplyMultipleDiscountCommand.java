package com.kaching123.tcr.commands.store.saleorder;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.database.Cursor;

import com.getbase.android.db.provider.ProviderAction;
import com.kaching123.tcr.commands.store.AsyncCommand;
import com.kaching123.tcr.function.MultipleDiscountWrapFunction;
import com.kaching123.tcr.model.DiscountBundle;
import com.kaching123.tcr.model.DiscountType;
import com.kaching123.tcr.model.MultipleDiscountModel;
import com.kaching123.tcr.model.SaleOrderItemModel;
import com.kaching123.tcr.model.converter.SaleOrderItemFunction;
import com.kaching123.tcr.service.BatchSqlCommand;
import com.kaching123.tcr.service.ISqlCommand;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore.MultipleDiscountTable;
import com.kaching123.tcr.store.ShopStore.SaleItemTable;
import com.telly.groundy.TaskResult;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.kaching123.tcr.model.ContentValuesUtil._castAsReal;

/**
 * Created by vkompaniets on 24.08.2016.
 */
public class ApplyMultipleDiscountCommand extends AsyncCommand {

    private static final String ARG_ORDER_ID = "ARG_ORDER_ID";
    private static final String ARG_DISCOUNT_BUNDLES = "ARG_DISCOUNT_BUNDLES";

    ArrayList<ContentProviderOperation> ops;
    BatchSqlCommand sql;

    @Override
    protected TaskResult doCommand() {

        ops = new ArrayList<>();
        sql = batchUpdate(SaleOrderItemModel.class);

        String orderId = getStringArg(ARG_ORDER_ID);
        List<DiscountBundle> discountBundles = (ArrayList<DiscountBundle>) getArgs().getSerializable(ARG_DISCOUNT_BUNDLES);
        if (discountBundles == null)
            discountBundles = loadDiscountBundles(getContext());

        List<SaleOrderItemModel> items = loadItems(getContext(), orderId);
        Map<String, SaleOrderItemModel> itemsMap = sortItems(items);
        DiscountBundle winner = getWinner(itemsMap, discountBundles);
        if (winner == null)
            return failed();

        BigDecimal bundleCount = calcBundleCount(itemsMap, winner);
        List<ItemInfo> itemInfos = collectItemInfo(itemsMap, winner, bundleCount);

        SplitSaleItemCommand splitCmd = new SplitSaleItemCommand();
        DiscountSaleOrderItemCommand discountCmd = new DiscountSaleOrderItemCommand();
        for (ItemInfo itemInfo : itemInfos){
            String saleItemId = itemsMap.get(itemInfo.id).saleItemGuid;
            if (itemInfo.splitQty.compareTo(BigDecimal.ZERO) == 1){
                SyncResult splitResult = splitCmd.sync(getContext(),  saleItemId, itemInfo.splitQty, getAppCommandContext());
                if (splitResult == null){
                    return failed();
                }else{
                    ops.addAll(splitResult.getLocalDbOperations());
                    sql.add(splitResult.getSqlCmd());
                }
            }
            SyncResult discountResult = discountCmd.syncDependent(getContext(), saleItemId, itemInfo.discount, DiscountType.PERCENT, getAppCommandContext());
            if (discountResult == null){
                return failed();
            }else{
                ops.addAll(discountResult.getLocalDbOperations());
                sql.add(discountResult.getSqlCmd());
            }
        }

        return succeeded();
    }

    private static List<DiscountBundle> loadDiscountBundles(Context context) {
        Cursor c = ProviderAction.query(ShopProvider.contentUri(MultipleDiscountTable.URI_CONTENT))
                .orderBy(MultipleDiscountTable.BUNDLE_ID)
                .perform(context);

        List<DiscountBundle> discountBundles = new MultipleDiscountWrapFunction().apply(c);
        c.close();

        return discountBundles;
    }

    private static List<SaleOrderItemModel> loadItems(Context context, String orderId){
        return ProviderAction.query(ShopProvider.contentUri(SaleItemTable.URI_CONTENT))
                .where(SaleItemTable.ORDER_GUID + " = ?", orderId)
                .where("(" + SaleItemTable.DISCOUNT + " IS NULL OR " + _castAsReal(SaleItemTable.DISCOUNT) + " = ?)", 0)
                .perform(context)
                .toFluentIterable(new SaleOrderItemFunction()).toImmutableList();
    }


    private static Map<String, SaleOrderItemModel> sortItems(List<SaleOrderItemModel> items){
        HashMap<String, SaleOrderItemModel> map = new HashMap<>();
        for (SaleOrderItemModel item : items){
            map.put(item.itemGuid, item);
        }
        return map;
    }

    private static DiscountBundle getWinner(Map<String, SaleOrderItemModel> itemsMap, List<DiscountBundle> discountBundles) {
        DiscountBundle winner = null;
        for (DiscountBundle bundle : discountBundles){
            boolean matches = true;
            for (MultipleDiscountModel bundleItem : bundle.bundleItems){
                SaleOrderItemModel item = itemsMap.get(bundleItem.itemId);
                if (item == null || item.qty.compareTo(bundleItem.qty) < 0){
                    matches = false;
                    break;
                }
            }
            if (matches){
                winner = bundle;
                break;
            }
        }
        return winner;
    }

    /**
     * if there are 42 items in the order list, and 4 items in the discount bundle
     * we should apply discount to
     * 42 / 4 = 10 items
     * and 2 items split into separate SaleItem
    * */
    private static BigDecimal calcBundleCount(Map<String, SaleOrderItemModel> itemsMap, DiscountBundle bundle){
        BigDecimal count = new BigDecimal(100500);
        for (MultipleDiscountModel bundleItem : bundle.bundleItems){
            SaleOrderItemModel item = itemsMap.get(bundleItem.itemId);
            if (item == null)
                continue;

            BigDecimal count2 = item.qty.divide(bundleItem.qty, 0, BigDecimal.ROUND_FLOOR);
            if (count2.compareTo(count) == -1){
                count = count2;
            }
        }
        return count;
    }

    private static List<ItemInfo> collectItemInfo(Map<String, SaleOrderItemModel> itemsMap, DiscountBundle bundle, BigDecimal bundleCount){
        ArrayList<ItemInfo> result = new ArrayList<>();
        for (MultipleDiscountModel bundleItem : bundle.bundleItems){
            String itemId = bundleItem.itemId;
            BigDecimal qty = bundleItem.qty.multiply(bundleCount);
            BigDecimal splitQty = itemsMap.get(itemId).qty.subtract(qty);
            BigDecimal discount = bundleItem.discount;
            result.add(new ItemInfo(itemId, qty, splitQty, discount));
        }

        return result;
    }

    @Override
    protected ISqlCommand createSqlCommand() {
        return sql;
    }

    @Override
    protected ArrayList<ContentProviderOperation> createDbOperations() {
        return ops;
    }

    private static class ItemInfo {
        String id;
        BigDecimal qty;
        BigDecimal splitQty;
        BigDecimal discount;

        public ItemInfo(String id, BigDecimal qty, BigDecimal splitQty, BigDecimal discount) {
            this.id = id;
            this.qty = qty;
            this.splitQty = splitQty;
            this.discount = discount;
        }
    }

    public static void start(Context context, String orderId, ArrayList<DiscountBundle> discountBundles){
        create(ApplyMultipleDiscountCommand.class).arg(ARG_ORDER_ID, orderId).arg(ARG_DISCOUNT_BUNDLES, discountBundles).queueUsing(context);
    }
}
