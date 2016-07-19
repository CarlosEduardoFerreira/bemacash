package com.kaching123.tcr.commands.loyalty;

import android.content.ContentProviderOperation;
import android.database.Cursor;

import com.getbase.android.db.provider.ProviderAction;
import com.kaching123.tcr.commands.store.AsyncCommand;
import com.kaching123.tcr.model.LoyaltyIncentiveModel;
import com.kaching123.tcr.model.SaleIncentiveModel;
import com.kaching123.tcr.service.ISqlCommand;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore.LoyaltyIncentiveTable;
import com.telly.groundy.TaskResult;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by vkompaniets on 18.07.2016.
 */
public class AddSaleIncentiveCommand extends AsyncCommand {

    private static final String ARG_INCENTIVE_ID = "ARG_INCENTIVE_ID";
    private static final String ARG_CUSTOMER_ID = "ARG_CUSTOMER_ID";
    private static final String ARG_ORDER_ID = "ARG_ORDER_ID";
    private static final String ARG_SALE_ITEM_ID = "ARG_SALE_ITEM_ID";

    private SaleIncentiveModel saleIncentive;
    private SyncResult addPointsMovementResult;

    @Override
    protected TaskResult doCommand() {

        String incentiveId = getStringArg(ARG_INCENTIVE_ID);
        String customerId = getStringArg(ARG_CUSTOMER_ID);
        String orderId = getStringArg(ARG_ORDER_ID);
        String saleItemId = getStringArg(ARG_SALE_ITEM_ID);

        Cursor c = ProviderAction.query(ShopProvider.contentUri(LoyaltyIncentiveTable.URI_CONTENT))
                .where(LoyaltyIncentiveTable.GUID + " = ?", incentiveId)
                .perform(getContext());

        if (!c.moveToFirst())
            return failed();

        LoyaltyIncentiveModel incentive = new LoyaltyIncentiveModel(c);
        c.close();

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

        return succeeded();
    }

    @Override
    protected ISqlCommand createSqlCommand() {
        return null;
    }

    @Override
    protected ArrayList<ContentProviderOperation> createDbOperations() {
        return null;
    }
}
