package com.kaching123.tcr.commands.store.saleorder;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.getbase.android.db.provider.ProviderAction;
import com.kaching123.tcr.commands.store.AsyncCommand;
import com.kaching123.tcr.model.ItemMovementModel;
import com.kaching123.tcr.model.ItemMovementModelFactory;
import com.kaching123.tcr.service.ISqlCommand;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.util.MovementUtils;
import com.telly.groundy.TaskResult;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by mboychenko on 4/3/2017.
 */

public class UpdateSaleOrderItemMovementsCommand extends AsyncCommand {

    private static final String ARG_ORDER_GUID = "ARG_ORDER_GUID";
    private static final String ARG_IS_RETURN = "ARG_IS_RETURN";

    private static final Uri ITEM_MOVEMENT_URI = ShopProvider.getContentUri(ShopStore.ItemMovementTable.URI_CONTENT);
    private SyncResult addItemsMovementResult;
    private String orderGuid;
    private boolean isReturn;

    @Override
    protected TaskResult doCommand() {
        if(orderGuid == null && !isReturn) {
            orderGuid = getStringArg(ARG_ORDER_GUID);
            isReturn = getBooleanArg(ARG_IS_RETURN, false);
        }

        if (orderGuid == null) {
            return failed();
        }

        ArrayList<ItemMovementModel> orderItemMovements = new ArrayList<>();
        MovementUtils.processAll(
                getContext(),
                getAppCommandContext(),
                orderGuid, null,
                false,
                orderItemMovements);

        if (orderItemMovements.isEmpty())
            return succeeded();

        ArrayList<ItemMovementModel> copyMovementsToRemove = new ArrayList<>(orderItemMovements.size());

        Cursor itemMovementsCursor = ProviderAction.query(ITEM_MOVEMENT_URI)
                .where(ShopStore.ItemMovementTable.ORDER_GUID + " = ?", orderGuid)
                .perform(getContext());

        if (itemMovementsCursor != null) {
            ArrayList<ItemMovementModel> historyMovementModels = new ArrayList<>(itemMovementsCursor.getCount());
            if (itemMovementsCursor.moveToFirst()) {
                historyMovementModels.add(new ItemMovementModel(itemMovementsCursor));
                while(itemMovementsCursor.moveToNext()) {
                    historyMovementModels.add(new ItemMovementModel(itemMovementsCursor));
                }
            }
            itemMovementsCursor.close();
            if (historyMovementModels.size() > 0) {
                HashMap<String, BigDecimal> itemQtyMovementHistory = new HashMap<>(historyMovementModels.size());
                for (ItemMovementModel historyMovementModel : historyMovementModels) {
                    if (itemQtyMovementHistory.containsKey(historyMovementModel.itemGuid)) {
                        itemQtyMovementHistory.put(historyMovementModel.itemGuid,
                                itemQtyMovementHistory.get(historyMovementModel.itemGuid).add(historyMovementModel.qty));
                    } else {
                        itemQtyMovementHistory.put(historyMovementModel.itemGuid, historyMovementModel.qty);
                    }
                }

                if (isReturn) {
                    refund(orderItemMovements, historyMovementModels, itemQtyMovementHistory);
                } else {
                    findQtyDifference(orderItemMovements, itemQtyMovementHistory, copyMovementsToRemove);
                    removeItems(orderItemMovements, itemQtyMovementHistory, historyMovementModels);
                }
            }

        }

        if(copyMovementsToRemove.size() > 0) {
            orderItemMovements.removeAll(copyMovementsToRemove);
        }

        addItemsMovementResult = new AddItemsMovementCommand().syncNow(getContext(), orderGuid, orderItemMovements, getAppCommandContext());
        if(addItemsMovementResult != null) {
            return succeeded();
        } else {
            return failed();
        }
    }

    private void refund(ArrayList<ItemMovementModel> orderItemMovements, ArrayList<ItemMovementModel> historyMovementModels, HashMap<String, BigDecimal> itemQtyMovementHistory) {
        ArrayList<ItemMovementModel> changedModels = new ArrayList<>(historyMovementModels.size());

        for (Map.Entry<String, BigDecimal> entry : itemQtyMovementHistory.entrySet()) {
            String updateFlag = null;
            for (ItemMovementModel historyMovementModel : historyMovementModels) {
                if (historyMovementModel.itemGuid.equals(entry.getKey())) {
                    updateFlag = historyMovementModel.itemUpdateFlag;
                    break;
                }
            }
            changedModels.add(ItemMovementModelFactory.getNewModel(entry.getKey(),
                    updateFlag,
                    entry.getValue().multiply(BigDecimal.ONE.negate()),
                    false,
                    new Date()));
        }


        orderItemMovements.clear();
        orderItemMovements.addAll(changedModels);
    }

    private void removeItems(ArrayList<ItemMovementModel> orderItemMovements, HashMap<String, BigDecimal> itemQtyMovementHistory, ArrayList<ItemMovementModel> historyMovementModels) {
        ArrayList<ItemMovementModel> changedModels = new ArrayList<>(orderItemMovements.size());
        for (Map.Entry<String, BigDecimal> entry : itemQtyMovementHistory.entrySet()) {
            boolean contain = false;
            for (ItemMovementModel orderItemMovement : orderItemMovements) {
                if(orderItemMovement.itemGuid.equals(entry.getKey())) {
                    contain = true;
                    break;
                }
            }
            if (!contain) {
                String updateFlag = null;
                for (ItemMovementModel orderItemMovement : historyMovementModels) {
                    if(orderItemMovement.itemGuid.equals(entry.getKey())) {
                        updateFlag = orderItemMovement.itemUpdateFlag;
                    }
                }
                changedModels.add(ItemMovementModelFactory.getNewModel(entry.getKey(),
                        updateFlag,
                        entry.getValue().multiply(BigDecimal.ONE.negate()),
                        false,
                        new Date()));
            }
        }
        orderItemMovements.addAll(changedModels);
        changedModels.clear();
    }

    private void findQtyDifference(ArrayList<ItemMovementModel> orderItemMovements, HashMap<String, BigDecimal> itemQtyMovementHistory, ArrayList<ItemMovementModel> itemMovementsToRemove) {
        ArrayList<ItemMovementModel> changedModels = new ArrayList<>(orderItemMovements.size());
        for (ItemMovementModel orderItemMovement : orderItemMovements) {
            if (itemQtyMovementHistory.containsKey(orderItemMovement.itemGuid)) {
                itemMovementsToRemove.add(orderItemMovement);
                BigDecimal diff = orderItemMovement.qty.subtract(itemQtyMovementHistory.get(orderItemMovement.itemGuid));
                if(diff.compareTo(BigDecimal.ZERO) != 0) {
                    changedModels.add(ItemMovementModelFactory.getNewModel(orderItemMovement.itemGuid,
                            orderItemMovement.itemUpdateFlag,
                            diff,
                            false,
                            new Date()));
                }
            }
        }
        orderItemMovements.addAll(changedModels);
        changedModels.clear();
    }

    @Override
    protected ISqlCommand createSqlCommand() {
        if (addItemsMovementResult != null) {
            return addItemsMovementResult.getSqlCmd();
        } else {
            return null;
        }
    }

    @Override
    protected ArrayList<ContentProviderOperation> createDbOperations() {
        ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();
        if (addItemsMovementResult != null && addItemsMovementResult.getLocalDbOperations() != null)
            operations.addAll(addItemsMovementResult.getLocalDbOperations());
        return operations;
    }

    public static void start(Context context, String orderGuid) {
        create(UpdateSaleOrderItemMovementsCommand.class)
                .arg(ARG_ORDER_GUID, orderGuid)
                .arg(ARG_IS_RETURN, false)
                .queueUsing(context);
    }

    public TaskResult startSync(Context context, String orderGuid, boolean isReturn, IAppCommandContext appCommandContext) {
        this.orderGuid = orderGuid;
        this.isReturn = isReturn;
        return syncStandalone(context, appCommandContext);
    }
}
