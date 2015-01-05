package com.kaching123.tcr.commands.store.user;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.net.Uri;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.commands.store.AsyncCommand;
import com.kaching123.tcr.function.CommissionsQuery;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.jdbc.converters.JdbcConverter;
import com.kaching123.tcr.model.CommissionsModel;
import com.kaching123.tcr.model.SaleOrderItemModel;
import com.kaching123.tcr.service.BatchSqlCommand;
import com.kaching123.tcr.service.ISqlCommand;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore;
import com.telly.groundy.TaskResult;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

/**
 * Created by pkabakov on 09.07.2014.
 */
public class AddCommissionsCommand extends AsyncCommand {

    private static final Uri URI_COMMISSIONS = ShopProvider.getContentUri(ShopStore.EmployeeCommissionsTable.URI_CONTENT);

    private String[] employeeIds;
    private ArrayList<SaleOrderItemModel> saleItems;
    private String orderId;

    private ArrayList<CommissionsModel> commissionsModels;

    public SyncResult sync(Context context, String[] employeeIds, ArrayList<SaleOrderItemModel> saleItems, String orderId, IAppCommandContext appCommandContext){
        this.employeeIds = employeeIds;
        this.saleItems = saleItems;
        this.orderId = orderId;
        return super.syncDependent(context, appCommandContext);
    }

    @Override
    protected TaskResult doCommand() {

        Map<String, BigDecimal> employeeCommissionAmounts = CommissionsQuery.loadSync(getContext(), employeeIds, saleItems, getApp().getDefaultStoreCommission());
        if (employeeCommissionAmounts == null) {
            Logger.e("AddCommissionsCommand: failed, no commissions to add!");
            return failed();
        }

        int employeesCount = employeeCommissionAmounts.size();
        commissionsModels = new ArrayList<CommissionsModel>(employeesCount);
        for (Map.Entry<String, BigDecimal> entry: employeeCommissionAmounts.entrySet()) {
            String employeeId = entry.getKey();
            BigDecimal commissionsAmount = entry.getValue();
            CommissionsModel commissionsModel = new CommissionsModel(UUID.randomUUID().toString(), employeeId, getAppCommandContext().getShiftGuid(), orderId, new Date(), commissionsAmount);
            commissionsModels.add(commissionsModel);
        }

        return succeeded();
    }

    @Override
    protected ArrayList<ContentProviderOperation> createDbOperations() {
        ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();
        for (CommissionsModel model : commissionsModels){
            operations.add(ContentProviderOperation.newInsert(URI_COMMISSIONS).withValues(model.toValues()).build());
        }
        return operations;
    }

    @Override
    protected ISqlCommand createSqlCommand() {
        JdbcConverter converter = JdbcFactory.getConverter(ShopStore.EmployeeCommissionsTable.TABLE_NAME);
        BatchSqlCommand commands = batchInsert(CommissionsModel.class);
        for (CommissionsModel model : commissionsModels){
            commands.add(converter.insertSQL(model, getAppCommandContext()));
        }
        return commands;
    }
}
