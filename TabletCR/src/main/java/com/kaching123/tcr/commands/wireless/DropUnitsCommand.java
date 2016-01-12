package com.kaching123.tcr.commands.wireless;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.getbase.android.db.provider.ProviderAction;
import com.kaching123.tcr.commands.store.AsyncCommand;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.jdbc.converters.JdbcConverter;
import com.kaching123.tcr.model.ItemExModel;
import com.kaching123.tcr.model.ItemMovementModel;
import com.kaching123.tcr.model.ItemMovementModelFactory;
import com.kaching123.tcr.model.Unit;
import com.kaching123.tcr.service.BatchSqlCommand;
import com.kaching123.tcr.service.ISqlCommand;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.store.ShopStore.UnitTable;
import com.kaching123.tcr.util.MovementUtils;
import com.telly.groundy.TaskHandler;
import com.telly.groundy.TaskResult;
import com.telly.groundy.annotations.OnFailure;
import com.telly.groundy.annotations.OnSuccess;
import com.telly.groundy.annotations.Param;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

import static com.kaching123.tcr.model.ContentValuesUtil._decimalQty;

/**
 * @author Ivan v. Rikhmayer
 *         This class is intended to
 */
public class DropUnitsCommand extends AsyncCommand  {

    private static final Uri UNIT_URI = ShopProvider.getContentUri(UnitTable.URI_CONTENT);
    private static final Uri ITEM_URI = ShopProvider.getContentUri(ShopStore.ItemTable.URI_CONTENT);
    private static final Uri ITEM_MOVEMENT_URI = ShopProvider.getContentUri(ShopStore.ItemMovementTable.URI_CONTENT);

    private static final String PARAM_GUILD = "PARAM_ITEM_ID";
    private static final String PARAM_ITEM = "PARAM_ITEM";

    private static final String RESULT_ITEM = "RESULT_ITEM";

    private ArrayList<ContentProviderOperation> ops;
    private BatchSqlCommand sqlCommand;

    @Override
    protected TaskResult doCommand() {
        ops = new ArrayList<ContentProviderOperation>();
        sqlCommand = batchDelete(Unit.class);
        JdbcConverter jdbcConverter = JdbcFactory.getConverter(UnitTable.TABLE_NAME);

        ArrayList<Unit> units = (ArrayList<Unit>) getArgs().getSerializable(PARAM_GUILD);
        for (Unit unit : units) {
            ops.add(ContentProviderOperation.newUpdate(UNIT_URI)
                    .withValues(ShopStore.DELETE_VALUES)
                    .withSelection(UnitTable.ID + " = ?", new String[]{unit.guid})
                    .build());
            sqlCommand.add(jdbcConverter.deleteSQL(unit, this.getAppCommandContext()));
        }

        ItemExModel parent = (ItemExModel) getArgs().getSerializable(PARAM_ITEM);
        parent.availableQty = BigDecimal.ZERO;
        BigDecimal availableQty = null;
        ItemMovementModel movementModel = null;
        Cursor cursor = ProviderAction.query(ITEM_URI)
                .projection(ShopStore.ItemTable.TMP_AVAILABLE_QTY)
                .where(ShopStore.ItemTable.GUID + " = ?", parent.guid)
                .perform(getContext());
        if (cursor.moveToFirst()) {
            availableQty = _decimalQty(cursor, 0);
        }
        cursor.close();
        /*****************************************************************************************************/
        if (parent.isStockTracking && parent.availableQty != null && parent.availableQty.compareTo(availableQty) != 0) {
            parent.updateQtyFlag = UUID.randomUUID().toString();
            movementModel = ItemMovementModelFactory.getNewModel(
                    parent.guid,
                    parent.updateQtyFlag,
                    parent.availableQty,
                    true,
                    new Date()
            );

        }

        jdbcConverter = JdbcFactory.getConverter(ShopStore.ItemTable.TABLE_NAME);
        ops.add(ContentProviderOperation.newUpdate(ITEM_URI)
                .withSelection(ShopStore.ItemTable.GUID + " = ?", new String[]{parent.guid})
                .withValues(parent.toValues()).build());
        sqlCommand.add(jdbcConverter.updateSQL(parent, this.getAppCommandContext()));

        /******************************************************************************************************/
        if (movementModel != null) {
            ops.add(ContentProviderOperation.newInsert(ITEM_MOVEMENT_URI).withValues(movementModel.toValues()).build());
            jdbcConverter = JdbcFactory.getConverter(ShopStore.ItemMovementTable.TABLE_NAME);
            sqlCommand.add(jdbcConverter.insertSQL(movementModel, this.getAppCommandContext()));
        }

        return succeeded().add(RESULT_ITEM, parent);
    }

    @Override
    protected ArrayList<ContentProviderOperation> createDbOperations() {
        return ops;
    }

    @Override
    protected ISqlCommand createSqlCommand() {
        return sqlCommand;
    }

    public static final TaskHandler start(Context context,
                                          ArrayList<Unit> unit,
                                          ItemExModel model,
                                          UnitCallback callback) {
        return  create(DropUnitsCommand.class)
                .arg(PARAM_GUILD, unit)
                .arg(PARAM_ITEM, model)
                .callback(callback)
                .queueUsing(context);
    }

    public static abstract class UnitCallback {

        @OnSuccess(DropUnitsCommand.class)
        public final void onSuccess(@Param(RESULT_ITEM) ItemExModel model) {
            handleSuccess(model);
        }

        protected abstract void handleSuccess(ItemExModel model);

        @OnFailure(DropUnitsCommand.class)
        public final void onFailure() {
            handleError();
        }

        protected abstract void handleError();
    }
}
