package com.kaching123.tcr.commands.wireless;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;

import com.getbase.android.db.provider.ProviderAction;
import com.getbase.android.db.provider.Query;
import com.kaching123.tcr.commands.store.AsyncCommand;
import com.kaching123.tcr.function.UnitWrapFunction;
import com.kaching123.tcr.model.Unit;
import com.kaching123.tcr.model.Unit.Status;
import com.kaching123.tcr.service.ISqlCommand;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore.UnitTable;
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
public class CollectUnitsCommand extends AsyncCommand  {

    private static final Uri UNIT_URI = ShopProvider.getContentUri(UnitTable.URI_CONTENT);

    private static final String PARAM_GUID = "PARAM_GUID";
    private static final String PARAM_ALLBUTSOLD = "allButSold";
    private static final String PARAM_CLEAN = "PARAM_CLEAN";
    private static final String PARAM_GUILD = "PARAM_ITEM_ID";
    private static final String PARAM_SALE_ORDER_ITEM_ID = "PARAM_SALE_ORDER_ITEM_ID";
    private static final String PARAM_SERIAL_CODE = "PARAM_SERIAL_CODE";
    private static final String PARAM_STATUS = "PARAM_STATUS";

    private static final String RESULT_ITEM = "RESULT_ITEM";
    private String guid;
    private String itemId;
    private String orderId;
    private String serial;
    private Status status;
    private boolean clean;

    protected ArrayList<Unit> list;

    @Override
    protected TaskResult doCommand() {
        if (guid == null) {
            guid = getArgs().getString(PARAM_GUID);
        }
        if (itemId == null) {
            itemId = getArgs().getString(PARAM_GUILD);
        }
        if (orderId == null) {
            orderId = getArgs().getString(PARAM_SALE_ORDER_ITEM_ID);
        }
        if (serial == null) {
            serial = getArgs().getString(PARAM_SERIAL_CODE);
        }
        if (status == null) {
            Object value = getArgs().getSerializable(PARAM_STATUS);
            if (value != null ) {
                status = (Status) value;
            }
        }
        if (!clean) {
            clean = getArgs().getBoolean(PARAM_CLEAN);
        }
        List<Unit> items = _wrap(syncQuery(guid, itemId, orderId, serial, status, getArgs().getBoolean(PARAM_ALLBUTSOLD))
                .perform(getContext()), new UnitWrapFunction());
        list = new ArrayList<Unit>();
        for (Unit item : items) {
            if (orderId == null && item.orderId == null) {
                list.add(item);
                continue;
            }
            if (clean && item.orderId == null || orderId == null || !item.orderId.equals(orderId)) {
                list.add(item);
            } else if (!clean && item.orderId == null || orderId == null || item.orderId.equals(orderId)) {
                list.add(item);
            }
        }
        return succeeded().add(RESULT_ITEM, list);
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

    public static Query syncQuery(String guid, String itemId, String orderId, String serial, Status status, boolean allbutSold) {
        Query query = ProviderAction.query(UNIT_URI);
        if (guid != null) {
            query = query.where(UnitTable.ID + " = ?", guid);
        }
        if (itemId != null) {
            query = query.where(UnitTable.ITEM_ID + " = ?", itemId);
        }
        if (!TextUtils.isEmpty(orderId)) {
            query = query.where(UnitTable.SALE_ORDER_ID + " = ?", orderId);
        }
        if (serial != null) {
            query = query.where(UnitTable.SERIAL_CODE + " = ?", serial);
        }
        if (status != null) {
            query = query.where(UnitTable.STATUS + " = ?", status.ordinal());
        }
        if (allbutSold) {
            query = query.where(UnitTable.STATUS + " <> ?", Status.SOLD.ordinal());
        }
        return query;
    }

    public ArrayList<Unit> sync(Context context,
                                String guid,
                                String itemId,
                                String orderId,
                                String serial,
                                Status status,
                                boolean clean, IAppCommandContext appCommandContext) {
        this.guid = guid;
        this.itemId = itemId;
        this.orderId = orderId;
        this.serial = serial;
        this.status = status;
        this.clean = clean;
        super.sync(context, null, appCommandContext);
        return list;
    }

    public static final TaskHandler start(Context context,
                                          String guid,
                                          String itemId,
                                          String orderId,
                                          String serial,
                                          Status status,
                                          boolean allButSold,
                                          boolean clean,
                                          UnitCallback callback) {
        return  create(CollectUnitsCommand.class)
                .arg(PARAM_GUID, guid)
                .arg(PARAM_GUILD, itemId)
                .arg(PARAM_ALLBUTSOLD, allButSold)
                .arg(PARAM_CLEAN, clean)
                .arg(PARAM_SALE_ORDER_ITEM_ID, orderId)
                .arg(PARAM_SERIAL_CODE, serial)
                .arg(PARAM_STATUS, status)
                .callback(callback)
                .queueUsing(context);
    }

    public static abstract class UnitCallback {

        @OnSuccess(CollectUnitsCommand.class)
        public final void onSuccess(@Param(RESULT_ITEM) List<Unit> result) {
            handleSuccess(result);
        }

        protected abstract void handleSuccess(List<Unit> unit);

        @OnFailure(CollectUnitsCommand.class)
        public final void onFailure() {
            handleError();
        }

        protected abstract void handleError();
    }
}
