package com.kaching123.tcr.commands.loyalty;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.database.Cursor;

import com.getbase.android.db.provider.ProviderAction;
import com.kaching123.tcr.commands.store.AsyncCommand;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.model.LoyaltyIncentiveModel;
import com.kaching123.tcr.model.LoyaltyRewardType;
import com.kaching123.tcr.model.SaleIncentiveModel;
import com.kaching123.tcr.service.BatchSqlCommand;
import com.kaching123.tcr.service.ISqlCommand;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore.LoyaltyIncentiveTable;
import com.kaching123.tcr.store.ShopStore.SaleIncentiveTable;
import com.kaching123.tcr.store.ShopStore.SaleItemTable;
import com.telly.groundy.TaskResult;
import com.telly.groundy.annotations.Param;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by vkompaniets on 18.07.2016.
 */
public class AddSaleIncentiveCommand extends AsyncCommand {

    private static final String ARG_INCENTIVE_ID = "ARG_INCENTIVE_ID";
    private static final String ARG_CUSTOMER_ID = "ARG_CUSTOMER_ID";
    private static final String ARG_ORDER_ID = "ARG_ORDER_ID";

    private static final String EXTRA_SALE_INCENTIVE_ID = "EXTRA_SALE_INCENTIVE_ID";

    private SaleIncentiveModel saleIncentive;
    private SyncResult addPointsMovementResult;

    @Override
    protected TaskResult doCommand() {

        String incentiveId = getStringArg(ARG_INCENTIVE_ID);
        String customerId = getStringArg(ARG_CUSTOMER_ID);
        String orderId = getStringArg(ARG_ORDER_ID);

        Cursor c = ProviderAction.query(ShopProvider.contentUri(LoyaltyIncentiveTable.URI_CONTENT))
                .where(LoyaltyIncentiveTable.GUID + " = ?", incentiveId)
                .perform(getContext());

        if (!c.moveToFirst())
            return failed();

        LoyaltyIncentiveModel incentive = new LoyaltyIncentiveModel(c);
        c.close();

        String saleItemId = null;
        if (incentive.rewardType == LoyaltyRewardType.ITEM)
            saleItemId = getLastAddedItem(orderId);

        saleIncentive = new SaleIncentiveModel(
                UUID.randomUUID().toString(),
                incentive.guid,
                customerId,
                orderId,
                incentive.type,
                incentive.rewardType,
                incentive.rewardValue,
                incentive.rewardValueType,
                saleItemId,
                incentive.pointThreshold
        );

        if (!addPointsMovement(customerId, incentive.pointThreshold))
            return failed();

        return succeeded().add(EXTRA_SALE_INCENTIVE_ID, saleIncentive.guid);
    }

    private boolean addPointsMovement(String customerId, BigDecimal points){
        if (points == null)
            return true;

        addPointsMovementResult = new AddLoyaltyPointsMovementCommand().sync(getContext(), customerId, points, getAppCommandContext());
        return addPointsMovementResult != null;
    }

    private String getLastAddedItem(String orderId){
        Cursor c = ProviderAction.query(ShopProvider.contentUriWithLimit(SaleItemTable.URI_CONTENT, 1))
                .projection(SaleItemTable.SALE_ITEM_GUID)
                .where(SaleItemTable.ORDER_GUID + " = ?", orderId)
                .orderBy(SaleItemTable.SEQUENCE + " DESC")
                .perform(getContext());

        String guid = null;
        if (c.moveToFirst())
            guid = c.getString(0);
        c.close();

        return guid;
    }

    @Override
    protected ISqlCommand createSqlCommand() {
        BatchSqlCommand batch = batchInsert(saleIncentive);
        batch.add(JdbcFactory.insert(saleIncentive, getAppCommandContext()));
        if (addPointsMovementResult != null)
            batch.add(addPointsMovementResult.getSqlCmd());

        return batch;
    }

    @Override
    protected ArrayList<ContentProviderOperation> createDbOperations() {
        ArrayList<ContentProviderOperation> cv = new ArrayList<>();
        cv.add(ContentProviderOperation.newInsert(ShopProvider.contentUri(SaleIncentiveTable.URI_CONTENT))
                .withValues(saleIncentive.toValues()).build());
        if (addPointsMovementResult != null)
            cv.addAll(addPointsMovementResult.getLocalDbOperations());

        return cv;
    }

    public static void start(Context context, String incentiveId, String customerId, String orderId, AddSaleIncentiveCallback callback){
        create(AddSaleIncentiveCommand.class)
                .arg(ARG_INCENTIVE_ID, incentiveId)
                .arg(ARG_CUSTOMER_ID, customerId)
                .arg(ARG_ORDER_ID, orderId)
                .callback(callback)
                .queueUsing(context);
    }

    public static abstract class AddSaleIncentiveCallback {

        public void onSuccess(@Param(EXTRA_SALE_INCENTIVE_ID) String saleIncentiveId){
            onAdded(saleIncentiveId);
        }

        protected abstract void onAdded(String saleIncentiveId);
    }
}
