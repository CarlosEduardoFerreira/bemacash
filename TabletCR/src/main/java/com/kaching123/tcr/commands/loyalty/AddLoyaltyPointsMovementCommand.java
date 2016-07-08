package com.kaching123.tcr.commands.loyalty;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import com.kaching123.tcr.commands.store.AsyncCommand;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.model.LoyaltyPointsMovementModel;
import com.kaching123.tcr.service.ISqlCommand;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore.LoyaltyPointsMovementTable;
import com.telly.groundy.TaskResult;
import com.telly.groundy.annotations.OnSuccess;
import com.telly.groundy.annotations.Param;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by vkompaniets on 07.07.2016.
 */
public class AddLoyaltyPointsMovementCommand extends AsyncCommand {

    private static final Uri URI_MOVEMENT = ShopProvider.contentUri(LoyaltyPointsMovementTable.URI_CONTENT);

    private static final String ARG_CUSTOMER = "ARG_CUSTOMER";
    private static final String ARG_POINTS = "ARG_POINTS";
    private static final String EXTRA_POINTS_APPLYED = "EXTRA_POINTS_APPLYED";


    private LoyaltyPointsMovementModel movement;

    @Override
    protected TaskResult doCommand() {
        String customerId = getStringArg(ARG_CUSTOMER);
        BigDecimal points = (BigDecimal)getArgs().getSerializable(ARG_POINTS);
        movement = new LoyaltyPointsMovementModel(UUID.randomUUID().toString(), customerId, points);
        return succeeded().add(EXTRA_POINTS_APPLYED, points);
    }

    @Override
    protected ISqlCommand createSqlCommand() {
        return JdbcFactory.insert(movement, getAppCommandContext());
    }

    @Override
    protected ArrayList<ContentProviderOperation> createDbOperations() {
        ArrayList<ContentProviderOperation> ops = new ArrayList<>(1);
        ops.add(ContentProviderOperation.newInsert(URI_MOVEMENT)
                .withValues(movement.toValues())
                .build()
        );
        return ops;
    }

    public static void start(Context context, String customerId, BigDecimal points, AddLoyaltyPointsMovementCallback callback){
        create(AddLoyaltyPointsMovementCommand.class).arg(ARG_CUSTOMER, customerId).arg(ARG_POINTS, points).callback(callback).queueUsing(context);
    }

    public SyncResult sync(Context context, String customerId, BigDecimal points, IAppCommandContext appCommandContext){
        Bundle args = new Bundle(2);
        args.putString(ARG_CUSTOMER, customerId);
        args.putSerializable(ARG_POINTS, points);
        return syncDependent(context, args, appCommandContext);
    }

    public static abstract class AddLoyaltyPointsMovementCallback {

        @OnSuccess(AddLoyaltyPointsMovementCommand.class)
        public void onSuccess(@Param(EXTRA_POINTS_APPLYED) BigDecimal points){
            onPointsApplied(points);
        }

        protected abstract void onPointsApplied(BigDecimal points);
    }
}
