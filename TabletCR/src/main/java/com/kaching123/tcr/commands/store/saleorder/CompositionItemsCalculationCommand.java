package com.kaching123.tcr.commands.store.saleorder;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.getbase.android.db.provider.ProviderAction;
import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.commands.store.AsyncCommand;
import com.kaching123.tcr.model.ComposerModel;
import com.kaching123.tcr.model.SaleOrderItemModel;
import com.kaching123.tcr.model.converter.SaleOrderItemFunction;
import com.kaching123.tcr.service.ISqlCommand;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore;
import com.telly.groundy.TaskResult;
import com.telly.groundy.annotations.OnSuccess;
import com.telly.groundy.annotations.Param;

import java.io.Serializable;
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
                .toList();

        List<String> guids = new ArrayList<>();
        for (SaleOrderItemModel item : saleOrderItems) {
            guids.add(item.itemGuid);
        }

        Set<String> ignoringItems = TcrApplication.get().getIgnorComposerList();                      //check if this item can be sold without child composition
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
        } else {
            return succeeded();
        }

        Cursor itemCursor = null;
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

                itemCursor = ProviderAction.query(ITEM_URI)                                                                          //get childItems from item table
                        .projection(ShopStore.ItemTable.GUID, ShopStore.ItemTable.TMP_AVAILABLE_QTY, ShopStore.ItemTable.STOCK_TRACKING, ShopStore.ItemTable.DESCRIPTION)
                        .whereIn(ShopStore.ItemTable.GUID, childItemsGuids)
                        .perform(getContext());

                if(itemCursor != null && itemCursor.moveToFirst()) {
                    do {
                        boolean isStockTracking = itemCursor.getInt(itemCursor.getColumnIndex(ShopStore.ItemTable.STOCK_TRACKING)) == 1;
                        if(isStockTracking){                                                                                                //second check if this item should be processed
                            String guid = itemCursor.getString(itemCursor.getColumnIndex(ShopStore.ItemTable.GUID));
                            BigDecimal qty = _decimalQty(itemCursor.getString(itemCursor.getColumnIndex(ShopStore.ItemTable.TMP_AVAILABLE_QTY)));
                            composersItemForProcess.put(guid, qty);                                                                         //final items that should be processed
                        }
                    } while(itemCursor.moveToNext());
                } else {
                    return succeeded();
                }
            }
        } else {
            return succeeded();
        }

        HashMap<String, List<CantSaleComposerModel>> itemCantBeSoldInfoList = new HashMap<>();

        if(!composersItemForProcess.isEmpty()){

            for (SaleOrderItemModel saleOrderItemModel : saleOrderItems) {                                                  //if composersItemForProcess also exist as standalone item in sale order
                if(composersItemForProcess.containsKey(saleOrderItemModel.itemGuid)){
                    composersItemForProcess.put(saleOrderItemModel.itemGuid, composersItemForProcess.get(saleOrderItemModel.itemGuid).subtract(saleOrderItemModel.qty));
                }
            }

            for (ComposerModel composer : composers) {
                if(composersItemForProcess.containsKey(composer.itemChildId)){
                    for (SaleOrderItemModel saleOrderItem : saleOrderItems) {
                        if(saleOrderItem.itemGuid.equals(composer.itemHostId)){
                            BigDecimal compsoreQtyForItem = saleOrderItem.qty.multiply(composer.qty);
                            if(compsoreQtyForItem.compareTo(composersItemForProcess.get(composer.itemChildId)) > 0){

                                CantSaleComposerModel cantSaleComposerModel = new CantSaleComposerModel(composer);
                                if(itemCursor != null && itemCursor.moveToFirst()){
                                    do{
                                        if(itemCursor.getString(itemCursor.getColumnIndex(ShopStore.ItemTable.GUID)).equals(composer.itemChildId)){
                                            cantSaleComposerModel.composerChildName = itemCursor.getString(itemCursor.getColumnIndex(ShopStore.ItemTable.DESCRIPTION));
                                            cantSaleComposerModel.totalNeededQty = compsoreQtyForItem;
                                            cantSaleComposerModel.availableSourceItemQty = composersItemForProcess.get(composer.itemChildId);
                                        }
                                    }while (itemCursor.moveToNext());
                                }

                                if(itemCantBeSoldInfoList.containsKey(composer.itemHostId)){
                                    List<CantSaleComposerModel> composerModels = itemCantBeSoldInfoList.get(composer.itemHostId);
                                    composerModels.add(cantSaleComposerModel);
                                    itemCantBeSoldInfoList.put(composer.itemHostId, composerModels);
                                } else {
                                    ArrayList<CantSaleComposerModel> composerModels = new ArrayList<>();
                                    composerModels.add(cantSaleComposerModel);
                                    itemCantBeSoldInfoList.put(composer.itemHostId, composerModels);
                                }
                            } else {
                                composersItemForProcess.put(composer.itemChildId, composersItemForProcess.get(composer.itemChildId).subtract(compsoreQtyForItem));
                            }
                        }
                    }
                }
            }
        }

        if(itemCursor != null)
            itemCursor.close();

        return succeeded().add(PARAM_SALE_ITEM_GUID, itemCantBeSoldInfoList);
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

        @OnSuccess(CompositionItemsCalculationCommand.class)
        public void handleSuccess(@Param(PARAM_SALE_ITEM_GUID) HashMap<String, List<CantSaleComposerModel>> itemCantBeSoldInfoList) {
            onSuccess(itemCantBeSoldInfoList);
        }

        protected abstract void onSuccess(HashMap<String, List<CantSaleComposerModel>> itemCantBeSoldInfoList);

    }

    public class CantSaleComposerModel extends ComposerModel implements Serializable{
        public BigDecimal totalNeededQty;
        public BigDecimal availableSourceItemQty;
        public String composerHostName;
        public String composerChildName;

        public CantSaleComposerModel(ComposerModel composer){
            this.guid = composer.guid;
            this.itemHostId = composer.itemHostId;
            this.itemChildId = composer.itemChildId;
            this.qty = composer.qty;
            this.tracked = composer.tracked;
            this.restricted = composer.restricted;
        }
    }
}
