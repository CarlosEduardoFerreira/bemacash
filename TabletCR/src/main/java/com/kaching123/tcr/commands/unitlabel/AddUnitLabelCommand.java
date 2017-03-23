package com.kaching123.tcr.commands.unitlabel;

import android.content.ContentProviderOperation;
import android.content.Context;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.R;
import com.kaching123.tcr.commands.AtomicUpload;
import com.kaching123.tcr.commands.store.AsyncCommand;
import com.kaching123.tcr.function.UnitLabelWrapFunction;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.model.UnitLabelModel;
import com.kaching123.tcr.model.UnitLabelModelFactory;
import com.kaching123.tcr.service.BatchSqlCommand;
import com.kaching123.tcr.service.ISqlCommand;
import com.kaching123.tcr.store.ShopStore;
import com.telly.groundy.PublicGroundyTask;
import com.telly.groundy.TaskResult;
import com.telly.groundy.annotations.OnFailure;
import com.telly.groundy.annotations.OnSuccess;
import com.telly.groundy.annotations.Param;

import java.util.ArrayList;
import java.util.List;

import static com.kaching123.tcr.util.CursorUtil._wrap;


/**
 * Created by alboyko 07.12.2015
 */
public class AddUnitLabelCommand extends AsyncCommand {

    public static final String ARG_MODEL = "arg_model";
    private static final String RESULT_DESC = "RESULT_DESC";

    private UnitLabelModel model;

    static BatchSqlCommand sql;

    @Override
    protected TaskResult doCommand() {
        Logger.d("AddUnitLabelCommand doCommand");

        if (model == null)
            model = (UnitLabelModel) getArgs().getSerializable(ARG_MODEL);

        List<UnitLabelModel> items = _wrap(UnitLabelCommandUtils.unitQuery(model.shortcut).perform(getContext()), new UnitLabelWrapFunction());
        if (items.isEmpty()) {
            return succeeded();
        } else {
            return failed().add(RESULT_DESC, getApp().getString(R.string.unit_label_shortcut_already_exists));
        }
    }

    @Override
    protected ArrayList<ContentProviderOperation> createDbOperations() {
        ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>(1);
        operations.add(ContentProviderOperation
                .newInsert(UnitLabelCommandUtils.UNIT_LABEL_URI)
                .withValues(model.toValues())
                .build());
        return operations;
    }

    @Override
    protected ISqlCommand createSqlCommand() {
        sql = batchInsert(model);
        sql.add(JdbcFactory.insert(model, getAppCommandContext()));
        return sql;
    }

    public static void start(Context context, UnitLabelModel model, UnitLabelCallback callback){
        create(AddUnitLabelCommand.class)
                .arg(ARG_MODEL, model)
                .callback(callback)
                .queueUsing(context);
    }

    /** will be used in import. can be standalone **/
    public String sync(Context context, String shortcut, PublicGroundyTask.IAppCommandContext appCommandContext) {
        UnitLabelModel model = UnitLabelModelFactory.getNewModel(shortcut, shortcut);
        return sync(context, model, appCommandContext);
    }

    /** will be used in import. can be standalone **/
    private String sync(Context context, UnitLabelModel model, PublicGroundyTask.IAppCommandContext appCommandContext){
        this.model = model;
        TaskResult result = super.syncStandalone(context, appCommandContext);
        return isFailed(result) ? null : model.guid;
    }


    public static abstract class UnitLabelCallback {

        @OnSuccess(AddUnitLabelCommand.class)
        public final void onSuccess() {
            new AtomicUpload().upload(sql, AtomicUpload.UploadType.WEB);
            handleSuccess();
        }

        protected abstract void handleSuccess();

        @OnFailure(AddUnitLabelCommand.class)
        public final void onFailure(@Param(RESULT_DESC) String message) {
            handleError(message);
        }

        protected abstract void handleError(String message);
    }
}
