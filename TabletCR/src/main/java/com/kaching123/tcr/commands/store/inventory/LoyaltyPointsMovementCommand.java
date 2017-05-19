package com.kaching123.tcr.commands.store.inventory;

import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.kaching123.tcr.commands.store.AsyncCommand;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.model.ItemMovementModel;
import com.kaching123.tcr.model.LoyaltyPointsMovementModel;
import com.kaching123.tcr.service.BatchSqlCommand;
import com.kaching123.tcr.service.ISqlCommand;
import com.kaching123.tcr.service.OfflineCommandsService;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.store.ShopStore.ItemMovementTable;
import com.telly.groundy.Groundy;
import com.telly.groundy.TaskResult;
import com.telly.groundy.annotations.OnFailure;
import com.telly.groundy.annotations.OnSuccess;

import java.util.ArrayList;

import static com.kaching123.tcr.model.SqlCommandHelper.getContentValues;

/**
 * Created by BemaCarl on 02.09.17.
 */
public class LoyaltyPointsMovementCommand extends AsyncCommand {

    private static final Uri LOYALTY_MOVEMENT_URI = ShopProvider.getContentUri(ShopStore.LoyaltyPointsMovementTable.URI_CONTENT);

    private static final String ARG_LOYALTY_MOVEMENT = "arg_loyalty_movement";

    private static final String ARG_START_UPLOAD = "arg_start_upload";

    private LoyaltyPointsMovementModel loyaltyPointsMovementModel;
    private ArrayList<ContentProviderOperation> operations;
    private BatchSqlCommand sql;

    @Override
    protected TaskResult doCommand() {

        if (loyaltyPointsMovementModel == null)
            loyaltyPointsMovementModel = (LoyaltyPointsMovementModel) getArgs().getSerializable(ARG_LOYALTY_MOVEMENT);

        Log.d("BemaCarl22","LoyaltyPointsMovementCommand.doCommand.loyaltyPoints1: " + loyaltyPointsMovementModel.loyaltyPoints);

        operations = new ArrayList<>();
        operations.add(ContentProviderOperation.newInsert(LOYALTY_MOVEMENT_URI)
                .withValues(loyaltyPointsMovementModel.toValues())
                .build());

        Log.d("BemaCarl22","LoyaltyPointsMovementCommand.doCommand.loyaltyPoints2: " + loyaltyPointsMovementModel.loyaltyPoints);
        sql = batchUpdate(loyaltyPointsMovementModel);
        sql.add(JdbcFactory.getConverter(loyaltyPointsMovementModel).insertSQL(loyaltyPointsMovementModel, getAppCommandContext()));
        Log.d("BemaCarl22","LoyaltyPointsMovementCommand.doCommand.loyaltyPoints3: " + loyaltyPointsMovementModel.loyaltyPoints);

        return succeeded();
    }



    @Override
    protected ArrayList<ContentProviderOperation> createDbOperations() {
        return operations;
    }

    @Override
    protected ISqlCommand createSqlCommand() {
        return sql;
    }

    public static void start(Context context, LoyaltyPointsMovementModel loyaltyPointsMovementModel, ReturnLoyaltyPointsCommandCallback callback) {
        create(LoyaltyPointsMovementCommand.class)
                .arg(ARG_LOYALTY_MOVEMENT, loyaltyPointsMovementModel)
                .queueUsing(context);
    }

    public static void startUpload(Context context, boolean startUpload) {
        Groundy.create(LoyaltyPointsMovementCommand.class).arg(ARG_START_UPLOAD, startUpload).queueUsing(context);
    }

    public boolean sync(Context context, LoyaltyPointsMovementModel loyaltyPointsMovementModel, IAppCommandContext appCommandContext) {
        this.loyaltyPointsMovementModel = loyaltyPointsMovementModel;
        TaskResult result = syncStandalone(context, appCommandContext);
        return !isFailed(result);
    }

    public SyncResult syncDependent(Context context, LoyaltyPointsMovementModel loyaltyPointsMovementModel, IAppCommandContext appCommandContext) {
        this.loyaltyPointsMovementModel = loyaltyPointsMovementModel;
        return syncDependent(context, appCommandContext);
    }

    public static abstract class ReturnLoyaltyPointsCommandCallback {

        @OnSuccess(LoyaltyPointsMovementCommand.class)
        public void onSuccess() {
            handleSuccess();
        }

        @OnFailure(LoyaltyPointsMovementCommand.class)
        public void onFailure() {
            handleFailure();
        }

        protected abstract void handleSuccess();

        protected abstract void handleFailure();

    }
}
