package com.kaching123.tcr.commands.store.inventory;

import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.getbase.android.db.provider.ProviderAction;
import com.kaching123.tcr.commands.store.AsyncCommand;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.model.ItemMatrixModel;
import com.kaching123.tcr.model.ItemModel;
import com.kaching123.tcr.model.ItemMovementModel;
import com.kaching123.tcr.model.ItemMovementModelFactory;
import com.kaching123.tcr.model.converter.ItemMatrixFunction;
import com.kaching123.tcr.service.BatchSqlCommand;
import com.kaching123.tcr.service.ISqlCommand;
import com.kaching123.tcr.service.OfflineCommandsService;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.store.ShopStore.ItemMovementTable;
import com.kaching123.tcr.store.ShopStore.ItemTable;
import com.kaching123.tcr.util.CursorUtil;
import com.kaching123.tcr.util.InventoryUtils;
import com.telly.groundy.Groundy;
import com.telly.groundy.TaskResult;
import com.telly.groundy.annotations.OnFailure;
import com.telly.groundy.annotations.OnSuccess;

import java.util.ArrayList;
import java.util.Date;

import static com.kaching123.tcr.model.SqlCommandHelper.getContentValues;

/**
 * Created by BemaCarl on 02.09.17.
 */
public class ReturnItemMovementCommand extends AsyncCommand {

    private static final Uri ITEM_MOVEMENT_URI = ShopProvider.getContentUri(ItemMovementTable.URI_CONTENT);

    private static final String ARG_ITEM_MOVEMENT = "arg_item_movement";

    private static final String ARG_START_UPLOAD = "arg_start_upload";

    private ItemMovementModel itemMovementModel;
    private ArrayList<ContentProviderOperation> operations;
    private BatchSqlCommand sql;

    @Override
    protected TaskResult doCommand() {

        if (itemMovementModel == null)
            itemMovementModel = (ItemMovementModel) getArgs().getSerializable(ARG_ITEM_MOVEMENT);

        Log.d("BemaCarl7","ReturnItemMovementCommand...doCommand.item.codeType: " + itemMovementModel.qty);

        operations = new ArrayList<>();
        operations.add(ContentProviderOperation.newInsert(ITEM_MOVEMENT_URI)
                .withValues(itemMovementModel.toValues())
                .build());

        sql = batchUpdate(itemMovementModel);
        sql.add(JdbcFactory.getConverter(itemMovementModel).insertSQL(itemMovementModel, getAppCommandContext()));

        /** Upload Item to Return ***********************************************/
        ContentValues values = getContentValues(sql, System.currentTimeMillis(), false);
        Log.d("BemaCarl6","ReturnItemMovementCommand...doCommand.sql: " + sql.toJson());

        getContext().getContentResolver().insert(ShopProvider.contentUri(ShopStore.SqlCommandTable.URI_CONTENT), values);

        OfflineCommandsService.startUpload(getContext());
        /*********************************************** Upload Item to Return **/

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

    public static void start(Context context, ItemMovementModel itemMovementModel, ReturnItemCommandCallback callback) {
        create(ReturnItemMovementCommand.class)
                .arg(ARG_ITEM_MOVEMENT, itemMovementModel)
                .callback(callback)
                .queueUsing(context);
    }

    public static void startUpload(Context context, boolean startUpload) {
        Groundy.create(ReturnItemMovementCommand.class).arg(ARG_START_UPLOAD, startUpload).queueUsing(context);
    }

    /**
     * use in import. can be standalone
     **/
    public boolean sync(Context context, ItemMovementModel itemMovementModel, IAppCommandContext appCommandContext) {
        this.itemMovementModel = itemMovementModel;
        TaskResult result = syncStandalone(context, appCommandContext);
        return !isFailed(result);
    }

    public SyncResult syncDependent(Context context, ItemMovementModel itemMovementModel, IAppCommandContext appCommandContext) {
        this.itemMovementModel = itemMovementModel;
        return syncDependent(context, appCommandContext);
    }

    public static abstract class ReturnItemCommandCallback {

        @OnSuccess(ReturnItemMovementCommand.class)
        public void onSuccess() {
            handleSuccess();
        }

        @OnFailure(ReturnItemMovementCommand.class)
        public void onFailure() {
            handleFailure();
        }

        protected abstract void handleSuccess();

        protected abstract void handleFailure();

    }
}
