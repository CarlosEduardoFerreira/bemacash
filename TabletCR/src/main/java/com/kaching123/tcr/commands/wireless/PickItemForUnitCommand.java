package com.kaching123.tcr.commands.wireless;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.getbase.android.db.provider.ProviderAction;
import com.kaching123.tcr.commands.store.AsyncCommand;
import com.kaching123.tcr.model.ItemExModel;
import com.kaching123.tcr.model.Unit;
import com.kaching123.tcr.model.converter.ItemExFunction;
import com.kaching123.tcr.service.ISqlCommand;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopSchema2.ItemExtView2.ItemTable;
import com.kaching123.tcr.store.ShopStore;
import com.telly.groundy.TaskHandler;
import com.telly.groundy.TaskResult;
import com.telly.groundy.annotations.OnFailure;
import com.telly.groundy.annotations.OnSuccess;
import com.telly.groundy.annotations.Param;

import java.util.ArrayList;

import static com.kaching123.tcr.util.CursorUtil._wrap;

/**
 * @author Ivan v. Rikhmayer
 *         This class is intended to
 */
public class PickItemForUnitCommand extends AsyncCommand  {

    private static final Uri URI_ITEMS = ShopProvider.getContentUriGroupBy(ShopStore.ItemExtView.URI_CONTENT, ItemTable.GUID);

    private static final String PARAM_GUID = "PARAM_GUID";

    private static final String RESULT_ITEM = "RESULT_ITEM";

    @Override
    protected TaskResult doCommand() {
        Unit unit = (Unit) getArgs().getSerializable(PARAM_GUID);
        Cursor cursor = ProviderAction.query(URI_ITEMS)
                .where(ItemTable.GUID + " = ?", unit.itemId)
                .where(ItemTable.IS_DELETED + " = ?", 0)
                .projection(ItemExFunction.PROJECTION)
                .perform(getContext());
        ItemExModel parent = _wrap(cursor, new  ItemExFunction.Wrap()).get();
        parent.tmpUnit.add(unit);
        cursor.close();
        return succeeded().add(RESULT_ITEM, parent);
    }

    @Override
    protected boolean validateAppCommandContext() {
        return false;
    }

    @Override
    protected ArrayList<ContentProviderOperation> createDbOperations() {
        return null;
    }

    @Override
    protected ISqlCommand createSqlCommand() {
        return null;
    }


    public static final TaskHandler start(Context context,
                                          Unit unit,
                                          UnitCallback callback) {
        return  create(PickItemForUnitCommand.class)
                .arg(PARAM_GUID, unit)
                .callback(callback)
                .queueUsing(context);
    }

    public static abstract class UnitCallback {

        @OnSuccess(PickItemForUnitCommand.class)
        public final void onSuccess(@Param(RESULT_ITEM)ItemExModel result) {
            handleSuccess(result);
        }

        protected abstract void handleSuccess(ItemExModel unit);

        @OnFailure(PickItemForUnitCommand.class)
        public final void onFailure() {
            handleError();
        }

        protected abstract void handleError();
    }
}
