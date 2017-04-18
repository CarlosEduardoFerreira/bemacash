package com.kaching123.tcr.commands.wireless;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.kaching123.tcr.commands.AtomicUpload;
import com.kaching123.tcr.commands.store.AsyncCommand;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.model.Unit;
import com.kaching123.tcr.service.BatchSqlCommand;
import com.kaching123.tcr.service.ISqlCommand;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore.UnitTable;
import com.telly.groundy.TaskHandler;
import com.telly.groundy.TaskResult;
import com.telly.groundy.annotations.OnFailure;
import com.telly.groundy.annotations.OnSuccess;
import com.telly.groundy.annotations.Param;

import java.util.ArrayList;

/**
 * @author Ivan v. Rikhmayer
 *         This class is intended to
 */
public class EditUnitCommand extends AsyncCommand  {

    private static final Uri UNIT_URI = ShopProvider.getContentUri(UnitTable.URI_CONTENT);

    private static final String PARAM_GUID = "PARAM_GUID";
    private static final String PARAM_EDITSERIAL = "editSerial";
    private static final String RESULT_DESC = "RESULT_DESC";

    private Unit unit;

    static BatchSqlCommand sql;

    @Override
    protected TaskResult doCommand() {
        if (unit == null)
            unit = (Unit) getArgs().getSerializable(PARAM_GUID);
        String serialToSet = getArgs().getString(PARAM_EDITSERIAL);

        boolean changingSerial = !TextUtils.isEmpty(serialToSet) && !unit.serialCode.equals(serialToSet);

        if (!TextUtils.isEmpty(serialToSet) && !unit.serialCode.equals(serialToSet)) {
            ArrayList<Unit> units = new CollectUnitsCommand().sync(getContext(), null, unit.itemId, null, serialToSet, null, false, getAppCommandContext());
            if (units.size() > 0) {
                return failed().add(RESULT_DESC, "Item with the same serial already exists");
            }
        }
        if (changingSerial) {
            unit.serialCode = serialToSet;
        }


        return succeeded();
    }

    @Override
    protected ArrayList<ContentProviderOperation> createDbOperations() {
        ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>(1);
        operations.add(ContentProviderOperation.newUpdate(UNIT_URI)
                .withValues(unit.toUpdateValues())
                .withSelection(UnitTable.ID + " = ?", new String[]{unit.guid})
                .build());
        return operations;
    }

    @Override
    protected ISqlCommand createSqlCommand() {
        sql = batchUpdate(unit);
        sql.add(JdbcFactory.getConverter(unit).updateSQL(unit, getAppCommandContext()));
        new AtomicUpload().upload(sql, AtomicUpload.UploadType.WEB);
        return sql;
    }

    public SyncResult sync(Context context, Unit unit, IAppCommandContext appCommandContext) {
        this.unit = unit;
        return syncDependent(context, appCommandContext);
    }

    public static final TaskHandler start(Context context,
                                           Unit unit,
                                           String editSerial,
                                           UnitCallback callback) {
        return  create(EditUnitCommand.class)
                .arg(PARAM_GUID, unit)
                .arg(PARAM_EDITSERIAL, editSerial)
                .callback(callback)
                .queueUsing(context);
    }

    public static abstract class UnitCallback {

        @OnSuccess(EditUnitCommand.class)
        public final void onSuccess() {
            handleSuccess();
        }

        protected abstract void handleSuccess();

        @OnFailure(EditUnitCommand.class)
        public final void onFailure(@Param(RESULT_DESC) String message) {
            handleError(message);
        }

        protected abstract void handleError(String message);
    }
}
