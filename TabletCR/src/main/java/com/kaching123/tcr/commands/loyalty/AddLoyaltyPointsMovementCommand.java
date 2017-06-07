package com.kaching123.tcr.commands.loyalty;

import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.getbase.android.db.provider.ProviderAction;
import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.commands.AtomicUpload;
import com.kaching123.tcr.commands.store.AsyncCommand;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.jdbc.converters.CustomerJdbcConverter;
import com.kaching123.tcr.model.CustomerModel;
import com.kaching123.tcr.model.LoyaltyPointsMovementModel;
import com.kaching123.tcr.service.BatchSqlCommand;
import com.kaching123.tcr.service.ISqlCommand;
import com.kaching123.tcr.service.OfflineCommandsService;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.store.ShopStore.LoyaltyPointsMovementTable;
import com.telly.groundy.TaskResult;
import com.telly.groundy.annotations.OnSuccess;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.UUID;

import static com.kaching123.tcr.model.ContentValuesUtil._decimal;
import static com.kaching123.tcr.model.SqlCommandHelper.getContentValues;

/**
 * Created by vkompaniets on 07.07.2016.
 */
public class AddLoyaltyPointsMovementCommand extends AsyncCommand {

    private static final Uri URI_MOVEMENT = ShopProvider.contentUri(LoyaltyPointsMovementTable.URI_CONTENT);
    private static final Uri CUSTOMER_URI = ShopProvider.contentUri(ShopStore.CustomerTable.URI_CONTENT);

    private static final String ARG_CUSTOMER = "ARG_CUSTOMER";
    private static final String ARG_POINTS = "ARG_POINTS";

    private LoyaltyPointsMovementModel movement;
    private ArrayList<ContentProviderOperation> operations;
    protected BatchSqlCommand sql;

    @Override
    protected TaskResult doCommand() {
        String customerId = getStringArg(ARG_CUSTOMER);
        BigDecimal points = (BigDecimal)getArgs().getSerializable(ARG_POINTS);

        long shopId = TcrApplication.get().getShopId();

        movement = new LoyaltyPointsMovementModel(UUID.randomUUID().toString(), customerId, points, shopId);

        operations = new ArrayList<>();
        operations.add(ContentProviderOperation.newInsert(URI_MOVEMENT)
                .withValues(movement.toValues())
                .build());

        sql = batchInsert(movement);
        sql.add(JdbcFactory.getConverter(movement).insertSQL(movement, getAppCommandContext()));

        Cursor cursor = ProviderAction.query(CUSTOMER_URI)
                .where(ShopStore.CustomerTable.GUID + " = ?", customerId)
                .perform(getContext());

        if(cursor.moveToNext()) {
            CustomerModel customerModel = new CustomerModel(cursor);
            BigDecimal newPoints = customerModel.loyaltyPoints.add(points);
            customerModel.loyaltyPoints = newPoints;

            Log.d("BemaCarl23","AddLoyaltyPointsMovementCommand.doCommand.birthdayRewardApplyDate: " + customerModel.birthdayRewardApplyDate);

            CustomerJdbcConverter customerJdbcConverter = (CustomerJdbcConverter) JdbcFactory.getConverter(ShopStore.CustomerTable.TABLE_NAME);
            sql.add(customerJdbcConverter.updateSQL(customerModel, getAppCommandContext()));
        }

        new AtomicUpload().upload(sql, AtomicUpload.UploadType.WEB);

        return succeeded();
    }

    @Override
    protected ISqlCommand createSqlCommand() {
        return sql;
    }

    @Override
    protected ArrayList<ContentProviderOperation> createDbOperations() {
        return operations;
    }

    public static void start(Context context, String customerId, BigDecimal points, AddLoyaltyPointsMovementCallback callback){
        create(AddLoyaltyPointsMovementCommand.class)
                .arg(ARG_CUSTOMER, customerId)
                .arg(ARG_POINTS, points)
                .callback(callback)
                .queueUsing(context);
    }

    public SyncResult sync(Context context, String customerId, BigDecimal points, IAppCommandContext appCommandContext){
        Bundle args = new Bundle(3);
        args.putString(ARG_CUSTOMER, customerId);
        args.putSerializable(ARG_POINTS, points);
        return syncDependent(context, args, appCommandContext);
    }

    public static abstract class AddLoyaltyPointsMovementCallback {

        @OnSuccess(AddLoyaltyPointsMovementCommand.class)
        public void onSuccess(){
            onPointsApplied();
        }

        protected abstract void onPointsApplied();
    }
}
