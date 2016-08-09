package com.kaching123.tcr.commands.wireless;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.net.Uri;

import com.getbase.android.db.provider.ProviderAction;
import com.getbase.android.db.provider.Query;
import com.kaching123.tcr.commands.store.AsyncCommand;
import com.kaching123.tcr.function.UnitWrapFunction;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.jdbc.converters.JdbcConverter;
import com.kaching123.tcr.model.ItemExModel;
import com.kaching123.tcr.model.Unit;
import com.kaching123.tcr.service.BatchSqlCommand;
import com.kaching123.tcr.service.ISqlCommand;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.store.ShopStore.UnitTable;
import com.kaching123.tcr.util.InventoryUtils;
import com.telly.groundy.TaskHandler;
import com.telly.groundy.TaskResult;
import com.telly.groundy.annotations.OnFailure;
import com.telly.groundy.annotations.OnSuccess;
import com.telly.groundy.annotations.Param;

import java.util.ArrayList;
import java.util.List;

import static com.kaching123.tcr.util.CursorUtil._wrap;

/**
 * @author Ivan v. Rikhmayer
 *         This class is intended to
 */
public class AddUnitsCommand extends AsyncCommand {

    private static final Uri UNIT_URI = ShopProvider.getContentUri(UnitTable.URI_CONTENT);
    private static final Uri ITEM_URI = ShopProvider.getContentUri(ShopStore.ItemTable.URI_CONTENT);
    private static final Uri ITEM_MOVEMENT_URI = ShopProvider.getContentUri(ShopStore.ItemMovementTable.URI_CONTENT);

    private static final String PARAM_GUID = "PARAM_GUID";
    private static final String PARAM_GUILD = "PARAM_ITEM_ID";

    private static final String RESULT_ITEM = "RESULT_ITEM";
    private static final String RESULT_DESC = "RESULT_DESCR";
    private static final String PARAM_PURPOSE = "PURPOSE";

    ArrayList<ContentProviderOperation> ops;
    private BatchSqlCommand sqlCommand;

    @Override
    protected TaskResult doCommand() {

        ops = new ArrayList<>();
        sqlCommand = batchInsert(Unit.class);
        JdbcConverter jdbcConverter = JdbcFactory.getConverter(UnitTable.TABLE_NAME);

        Unit unit = (Unit) getArgs().getSerializable(PARAM_GUID);
        List<Unit> items = _wrap(unitQuery(unit.serialCode, unit.itemId).perform(getContext()), new UnitWrapFunction());

        ItemExModel parent = (ItemExModel) getArgs().getSerializable(PARAM_GUILD);

        boolean shouldAdd = getArgs().getBoolean(PARAM_PURPOSE);

        int itemUnitCount = items.size();
        if (itemUnitCount == 0) { // OK
            if (shouldAdd) {
                ops.add(ContentProviderOperation.newInsert(UNIT_URI).withValues(unit.toValues()).build());
                sqlCommand.add(jdbcConverter.insertSQL(unit, this.getAppCommandContext()));
            } else {
                return failed().add(RESULT_DESC, "Item with the provided serial is missing.");
            }
        } else {
            if (itemUnitCount == 1 && !shouldAdd) { // OK
                unit = items.get(0);
                ops.add(ContentProviderOperation.newUpdate(UNIT_URI)
                        .withValues(ShopStore.DELETE_VALUES)
                        .withSelection(UnitTable.ID + " = ?", new String[]{unit.guid})
                        .build());
                sqlCommand.add(jdbcConverter.deleteSQL(unit, this.getAppCommandContext()));
            } else {
                return failed().add(RESULT_DESC, "The item with the same serial already exists.");
            }
        }

        /*Cursor c = ProviderAction.query(UNIT_URI)
                .projection("1")
                .where(UnitTable.ITEM_ID + " = ?", parent.guid)
                .where(UnitTable.STATUS + " != ?", Status.SOLD.ordinal())
                .perform(getContext());

        parent.availableQty = new BigDecimal(c.getCount() + (shouldAdd ? 1 : -1));
        c.close();

        jdbcConverter = JdbcFactory.getConverter(ShopStore.ItemTable.TABLE_NAME);
        ops.add(ContentProviderOperation.newUpdate(ITEM_URI)
                .withSelection(ShopStore.ItemTable.GUID + " = ?", new String[]{parent.guid})
                .withValues(parent.toQtyValues()).build());
        sqlCommand.add(jdbcConverter.updateSQL(parent, this.getAppCommandContext()));

        ops.add(ContentProviderOperation.newInsert(ITEM_MOVEMENT_URI).withValues(movementModel.toValues()).build());
        jdbcConverter = JdbcFactory.getConverter(ShopStore.ItemMovementTable.TABLE_NAME);
        sqlCommand.add(jdbcConverter.insertSQL(movementModel, this.getAppCommandContext()));*/

        if (!InventoryUtils.removeComposers(parent.guid, getContext(), getAppCommandContext(), ops, sqlCommand)) {
            return failed().add(RESULT_DESC, "Database has responded with a failure code!");
        }

        return succeeded().add(RESULT_ITEM, parent);
    }

    @Override
    protected ArrayList<ContentProviderOperation> createDbOperations () {
        return ops;
    }

    @Override
    protected ISqlCommand createSqlCommand () {
        return sqlCommand;
    }

    public static Query unitQuery(String SN, String ItemId) {
        Query query = ProviderAction.query(UNIT_URI);
        if (SN != null) {
            query = query.where(UnitTable.SERIAL_CODE + " = ?", SN);
        }
        if (ItemId != null) {
            query = query.where(UnitTable.ITEM_ID + " = ?", ItemId);
        }
        query = query.where(UnitTable.IS_DELETED + " = ?", 0);
        return query;
    }


    public static final TaskHandler start(Context context,
                                          boolean add,
                                          Unit unit,
                                          ItemExModel model,
                                          UnitCallback callback) {
        return create(AddUnitsCommand.class)
                .arg(PARAM_GUID, unit)
                .arg(PARAM_GUILD, model)
                .arg(PARAM_PURPOSE, add)
                .callback(callback)
                .queueUsing(context);
    }

    public static abstract class UnitCallback {

        @OnSuccess(AddUnitsCommand.class)
        public final void onSuccess(@Param(RESULT_ITEM) ItemExModel model) {
            handleSuccess(model);
        }

        protected abstract void handleSuccess(ItemExModel model);

        @OnFailure(AddUnitsCommand.class)
        public final void onFailure(@Param(RESULT_DESC) String message) {
            handleError(message);
        }

        protected abstract void handleError(String message);
    }
}
