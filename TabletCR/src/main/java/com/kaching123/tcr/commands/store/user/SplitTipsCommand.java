package com.kaching123.tcr.commands.store.user;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.getbase.android.db.provider.ProviderAction;
import com.kaching123.tcr.commands.store.AsyncCommand;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.jdbc.converters.JdbcConverter;
import com.kaching123.tcr.model.EmployeeStatus;
import com.kaching123.tcr.model.TipsModel;
import com.kaching123.tcr.model.TipsModel.PaymentType;
import com.kaching123.tcr.service.BatchSqlCommand;
import com.kaching123.tcr.service.ISqlCommand;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore.EmployeeTable;
import com.kaching123.tcr.store.ShopStore.EmployeeTipsTable;
import com.kaching123.tcr.util.CalculationUtil;
import com.telly.groundy.TaskResult;
import com.telly.groundy.annotations.OnFailure;
import com.telly.groundy.annotations.OnSuccess;
import com.telly.groundy.annotations.Param;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

/**
 * Created by vkompaniets on 01.06.2014.
 */
public class SplitTipsCommand extends AsyncCommand {

    private static final Uri URI_TIPS = ShopProvider.getContentUri(EmployeeTipsTable.URI_CONTENT);
    private static final Uri URI_EMPLOYEE = ShopProvider.getContentUri(EmployeeTable.URI_CONTENT);

    private static final String ARG_EMPLOYEE_GUIDS = "ARG_EMPLOYEE_GUIDS";
    private static final String ARG_AMOUNT = "ARG_AMOUNT";
    private static final String ARG_NOTES = "ARG_NOTES";

    private static final String EXTRA_COUNT = "EXRTRA_COUNT";

    private ArrayList<TipsModel> models;

    @Override
    protected TaskResult doCommand() {
        ArrayList<String> guids = getArgs().getStringArrayList(ARG_EMPLOYEE_GUIDS);
        final BigDecimal amount = (BigDecimal) getArgs().getSerializable(ARG_AMOUNT);
        final String notes = getStringArg(ARG_NOTES);

        if (guids == null)
            guids = loadTipsableGuids();

        final int count = guids.size();
        final BigDecimal splittedAmount = CalculationUtil.splitAmount(amount, count);

        models = new ArrayList<TipsModel>(count);
        for (String guid : guids){
            TipsModel tipsModel = new TipsModel(
                    UUID.randomUUID().toString(),
                    null,
                    guid,
                    getAppCommandContext().getShiftGuid(),
                    null,
                    null,
                    new Date(),
                    splittedAmount,
                    notes,
                    PaymentType.CASH
            );
            models.add(tipsModel);
        }

        return succeeded().add(EXTRA_COUNT, count);
    }

    private ArrayList<String> loadTipsableGuids() {
        Cursor c = ProviderAction.query(URI_EMPLOYEE)
                .projection(EmployeeTable.GUID)
                .where(EmployeeTable.TIPS_ELIGIBLE + " = ?", 1)
                .where(EmployeeTable.STATUS + " = ?", EmployeeStatus.ACTIVE.ordinal())
                .perform(getContext());

        ArrayList<String> guids = new ArrayList<String>(c.getCount());
        while (c.moveToNext()){
            guids.add(c.getString(0));
        }
        c.close();

        return guids;
    }

    @Override
    protected ArrayList<ContentProviderOperation> createDbOperations() {
        ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();
        for (TipsModel model : models){
            operations.add(ContentProviderOperation.newInsert(URI_TIPS)
                    .withValues(model.toValues())
                    .build());
        }
        return operations;
    }

    @Override
    protected ISqlCommand createSqlCommand() {
        JdbcConverter converter = JdbcFactory.getConverter(EmployeeTipsTable.TABLE_NAME);
        BatchSqlCommand commands = batchInsert(TipsModel.class);
        for (TipsModel model : models){
            commands.add(converter.insertSQL(model, getAppCommandContext()));
        }
        return commands;
    }

    public static void start(Context context, ArrayList<String> employeeGuids, BigDecimal amount, String notes, BaseSplitTipsCallback callback) {
        create(SplitTipsCommand.class)
                .arg(ARG_EMPLOYEE_GUIDS, employeeGuids)
                .arg(ARG_AMOUNT, amount)
                .arg(ARG_NOTES, notes)
                .callback(callback)
                .queueUsing(context);
    }

    public static abstract class BaseSplitTipsCallback {

        @OnSuccess(SplitTipsCommand.class)
        public void handleSuccess(@Param(EXTRA_COUNT) int count){
            onSplitSuccess(count);
        }

        @OnFailure(SplitTipsCommand.class)
        public void handleFailure(){
            onSplitFailure();
        }

        protected abstract void onSplitSuccess(int count);
        protected abstract void onSplitFailure();
    }
}
