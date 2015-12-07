package com.kaching123.tcr.commands.unitlabel;

import android.content.ContentProviderOperation;
import android.content.Context;

import com.kaching123.tcr.commands.store.AsyncCommand;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.model.UnitLabelModel;
import com.kaching123.tcr.service.ISqlCommand;
import com.kaching123.tcr.store.ShopStore;
import com.telly.groundy.TaskResult;
import com.telly.groundy.annotations.OnFailure;
import com.telly.groundy.annotations.OnSuccess;

import java.util.ArrayList;

/**
 * Created by alboyko 07.12.2015
 */
public class RemoveUnitLabelCommand extends AsyncCommand {

    public static final String ARG_MODEL = "arg_model";

    private UnitLabelModel model;

    @Override
    protected TaskResult doCommand() {
        model = (UnitLabelModel) getArgs().getSerializable(ARG_MODEL);
        return succeeded();
    }

    @Override
    protected ISqlCommand createSqlCommand() {
        return JdbcFactory.delete(model, getAppCommandContext());
    }

    @Override
    protected ArrayList<ContentProviderOperation> createDbOperations() {
        ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>(1);
        operations.add(ContentProviderOperation
                .newDelete(UnitLabelCommandUtils.UNIT_LABEL_URI)
                .withSelection(ShopStore.UnitLabelTable.GUID + " = ?", new String[]{model.guid})
                .build());
        return operations;
    }

    public static void start(Context context, UnitLabelModel model, UnitLabelCallback callback) {
        create(RemoveUnitLabelCommand.class)
                .arg(ARG_MODEL, model)
                .callback(callback)
                .queueUsing(context);
    }

    public static abstract class UnitLabelCallback {

        @OnSuccess(RemoveUnitLabelCommand.class)
        public final void onSuccess() {
            handleSuccess();
        }

        protected abstract void handleSuccess();

        @OnFailure(RemoveUnitLabelCommand.class)
        public final void onFailure() {
            handleError();
        }

        protected abstract void handleError();
    }
}
