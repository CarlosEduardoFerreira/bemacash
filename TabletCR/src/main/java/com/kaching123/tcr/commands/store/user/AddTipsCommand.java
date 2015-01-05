package com.kaching123.tcr.commands.store.user;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;

import com.kaching123.tcr.commands.store.AsyncCommand;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.jdbc.converters.SaleOrdersJdbcConverter;
import com.kaching123.tcr.model.SaleOrderModel;
import com.kaching123.tcr.model.TipsModel;
import com.kaching123.tcr.service.BatchSqlCommand;
import com.kaching123.tcr.service.ISqlCommand;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore.EmployeeTipsTable;
import com.kaching123.tcr.store.ShopStore.SaleOrderTable;
import com.telly.groundy.TaskResult;
import com.telly.groundy.annotations.OnFailure;
import com.telly.groundy.annotations.OnSuccess;

import java.util.ArrayList;

/**
 * Created by pkabakov on 19.05.2014.
 */
public class AddTipsCommand extends AsyncCommand {

    private static final Uri URI_TIPS = ShopProvider.getContentUri(EmployeeTipsTable.URI_CONTENT);
    private static final Uri URI_ORDER = ShopProvider.getContentUri(SaleOrderTable.URI_CONTENT);

    public static final String ARG_TIPS = "ARG_TIPS";

    private TipsModel tipsModel;
    private SaleOrderModel orderModel;

    public SyncResult sync(Context context, TipsModel tipsModel, IAppCommandContext appCommandContext){
        this.tipsModel = tipsModel;
        return super.syncDependent(context, appCommandContext);
    }

    @Override
    protected TaskResult doCommand() {
        if (tipsModel == null)
            tipsModel = (TipsModel) getArgs().getSerializable(ARG_TIPS);

        if (!TextUtils.isEmpty(tipsModel.orderId)) {
            orderModel = new SaleOrderModel(tipsModel.orderId);
            orderModel.isTipped = true;
        }

        return succeeded();
    }

    @Override
    protected ArrayList<ContentProviderOperation> createDbOperations() {
        ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();

        //TODO: check is order tipped?
        operations.add(ContentProviderOperation.newInsert(URI_TIPS).withValues(tipsModel.toValues()).build());

        if (orderModel != null)
            operations.add(ContentProviderOperation.newUpdate(URI_ORDER)
                    .withValues(orderModel.toSetTippedValues())
                    .withSelection(SaleOrderTable.GUID + " = ?", new String[]{orderModel.guid})
                    .build());

        return operations;
    }

    @Override
    protected ISqlCommand createSqlCommand() {
        BatchSqlCommand commands = batchInsert(tipsModel);
        commands.add(JdbcFactory.getConverter(tipsModel).insertSQL(tipsModel, getAppCommandContext()));
        if (orderModel != null)
            commands.add(((SaleOrdersJdbcConverter)JdbcFactory.getConverter(orderModel)).updateIsTipped(orderModel, getAppCommandContext()));
        return commands;
    }

    public static void start(Context context, TipsModel model, BaseAddTipsCallback callback){
        create(AddTipsCommand.class).arg(ARG_TIPS, model).callback(callback).queueUsing(context);
    }

    public static abstract class BaseAddTipsCallback {

        @OnSuccess(AddTipsCommand.class)
        public void handleSuccess(){
            onTipsAddSuccess();
        }

        @OnFailure(AddTipsCommand.class)
        public void handleFailure(){
            onTipsAddFailure();
        }

        protected abstract void onTipsAddSuccess();
        protected abstract void onTipsAddFailure();
    }

}
