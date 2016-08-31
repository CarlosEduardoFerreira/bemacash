package com.kaching123.tcr.commands.store.saleorder;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.getbase.android.db.provider.ProviderAction;
import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.commands.store.AsyncCommand;
import com.kaching123.tcr.model.ComposerModel;
import com.kaching123.tcr.model.ItemModel;
import com.kaching123.tcr.model.SaleOrderItemModel;
import com.kaching123.tcr.model.converter.ItemFunction;
import com.kaching123.tcr.model.converter.SaleOrderItemFunction;
import com.kaching123.tcr.service.ISqlCommand;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore;
import com.telly.groundy.TaskResult;
import com.telly.groundy.annotations.OnSuccess;
import com.telly.groundy.annotations.Param;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static com.kaching123.tcr.model.ContentValuesUtil._decimalQty;

/**
 * Created by mboychenko on 31.08.2016.
 */
public class CompositionItemsCalculationCommand extends AsyncCommand {

    private static final Uri URI_SALE_ITEMS = ShopProvider.getContentUri(ShopStore.SaleItemTable.URI_CONTENT);
    private static final Uri COMPOSER_URI = ShopProvider.contentUri(ShopStore.ComposerTable.URI_CONTENT);
    private static final Uri ITEM_URI = ShopProvider.contentUri(ShopStore.ItemTable.TABLE_NAME);


    private static final String ARG_GUID = "ARG_GUID";
    private static final String PARAM_SALE_ITEM_GUID = "PARAM_ANSWER_GUIDS";


    @Override
    protected TaskResult doCommand() {
        String orderGuid = getStringArg(ARG_GUID);

        HashMap<String, BigDecimal> composersItemForProcess = new HashMap<>();                              //item guid, item available qty


        List<SaleOrderItemModel> saleOrderItems = ProviderAction.query(URI_SALE_ITEMS)                      //get all saleOrderItems
                .where(ShopStore.SaleItemTable.ORDER_GUID + " = ?", orderGuid)
                .perform(getContext())
                .toFluentIterable(new SaleOrderItemFunction())
                .toImmutableList();

        List<String> guids = new ArrayList<>();
        for (SaleOrderItemModel item : saleOrderItems) {
            guids.add(item.itemGuid);
        }

        ArrayList<String> ignoringItems = TcrApplication.get().getIgnorComposerList();                      //check if this item can be sold without child composition
        for (Iterator<String> iterator = guids.iterator(); iterator.hasNext(); ) {
            if(ignoringItems.contains(iterator.next())){
                iterator.remove();
            }
        }

        Cursor composerCursor = ProviderAction.query(COMPOSER_URI)
                .whereIn(ShopStore.ComposerTable.ITEM_HOST_ID, guids)
                .perform(getContext());

        List<ComposerModel> composers = new ArrayList<>();

        if(composerCursor != null && composerCursor.moveToFirst()){
            do {
                composers.add(new ComposerModel(composerCursor));
            } while (composerCursor.moveToNext());
            composerCursor.close();
        }


        if(!composers.isEmpty()){                                                                            //first check if this item should be processed
            for (Iterator<ComposerModel> iterator = composers.iterator(); iterator.hasNext(); ) {
                if(!iterator.next().tracked){
                    iterator.remove();
                }
            }

            if(!composers.isEmpty()) {
                Set<String> childItemsGuids = new HashSet<>();
                for (ComposerModel composerToCheck : composers) {
                    childItemsGuids.add(composerToCheck.itemChildId);
                }

                Cursor itemCursor = ProviderAction.query(ITEM_URI)                                          //get childItems from item table
                        .projection(ShopStore.ItemTable.GUID, ShopStore.ItemTable.TMP_AVAILABLE_QTY, ShopStore.ItemTable.STOCK_TRACKING)
                        .whereIn(ShopStore.ItemTable.GUID, childItemsGuids)
                        .perform(getContext());

                if(itemCursor != null && itemCursor.moveToFirst()) {
                    do {
                        boolean isStockTracking = itemCursor.getInt(itemCursor.getColumnIndex(ShopStore.ItemTable.STOCK_TRACKING)) == 1;
                        if(isStockTracking){
                            String guid = itemCursor.getString(itemCursor.getColumnIndex(ShopStore.ItemTable.GUID));
                            BigDecimal qty = _decimalQty(itemCursor.getString(itemCursor.getColumnIndex(ShopStore.ItemTable.TMP_AVAILABLE_QTY)));
                            composersItemForProcess.put(guid, qty);
                        }
                    } while(itemCursor.moveToNext());
                    itemCursor.close();
                } else {
                    return succeeded();
                }

            }
        }

        if(!composersItemForProcess.isEmpty()){
                //composers vs composersItemForProcess //check if composer can be in few parent items
            HashMap<String, BigDecimal> totalNeededComposItems = new HashMap<>();

            for (ComposerModel composer : composers) {
                if(composersItemForProcess.containsKey(composer.itemChildId)){
                    for (SaleOrderItemModel saleOrderItem : saleOrderItems) {
                        if(saleOrderItem.itemGuid.equals(composer.itemHostId)){
                            BigDecimal compsoreQtyForItem = saleOrderItem.qty.multiply(composer.qty);
                            if(totalNeededComposItems.containsKey(composer.itemChildId)){
                                totalNeededComposItems.put(composer.itemChildId, totalNeededComposItems.get(composer.itemChildId).add(compsoreQtyForItem));
                            } else {
                                totalNeededComposItems.put(composer.itemChildId, compsoreQtyForItem);
                            }
                        }
                    }
//                    if(composer.qty.compareTo(composersItemForProcess.get(composer.itemChildId)) > 0){
//
//                    }
                }
            }
        }

        return succeeded().add(PARAM_SALE_ITEM_GUID, "");
    }



    @Override
    protected ArrayList<ContentProviderOperation> createDbOperations() {
        return null;
    }

    @Override
    protected ISqlCommand createSqlCommand() {
        return null;
    }

    public static void start(Context context, String orderGuid, CompositionItemsCalculationCommandCallback callback) {
        create(CompositionItemsCalculationCommand.class)
                .arg(ARG_GUID, orderGuid)
                .callback(callback)
                .queueUsing(context);
    }

    public static abstract class CompositionItemsCalculationCommandCallback {

        @OnSuccess(UpdateQtySaleOrderItemCommand.class)
        public void handleSuccess(@Param(PARAM_SALE_ITEM_GUID) String saleItemGuid) {
            onSuccess(saleItemGuid);
        }

        protected abstract void onSuccess(String saleItemGuid);

    }
}
