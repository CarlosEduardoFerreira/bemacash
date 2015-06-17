package com.kaching123.tcr.commands.wireless;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.getbase.android.db.provider.ProviderAction;
import com.getbase.android.db.provider.Query;
import com.kaching123.tcr.commands.store.AsyncCommand;
import com.kaching123.tcr.function.UnitWrapFunction;
import com.kaching123.tcr.model.SaleOrderViewModel;
import com.kaching123.tcr.model.Unit;
import com.kaching123.tcr.model.converter.SaleOrderViewFunction;
import com.kaching123.tcr.service.ISqlCommand;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopSchema2;
import com.kaching123.tcr.store.ShopSchema2.SaleOrderView2;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.store.ShopStore.SaleOrderTipsQuery;
import com.kaching123.tcr.store.ShopStore.UnitTable;
import com.telly.groundy.TaskHandler;
import com.telly.groundy.TaskResult;
import com.telly.groundy.annotations.OnFailure;
import com.telly.groundy.annotations.OnSuccess;
import com.telly.groundy.annotations.Param;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static com.kaching123.tcr.util.CursorUtil._wrap;

/**
 * @author Ivan v. Rikhmayer
 *         This class is intended to
 */
public class SearchUnitCommand extends AsyncCommand  {

    private static final Uri UNIT_URI = ShopProvider.getContentUri(UnitTable.URI_CONTENT);

    private static final Uri SALE_ORDER_URI = ShopProvider.getContentUri(SaleOrderTipsQuery.URI_CONTENT);

    private static final String PARAM_GUID = "PARAM_GUID";
    private static final String PARAM_ITEM = "PARAM_ITEM";
    private static final String PARAM_ORDERID = "PARAM_ORDERID";
    private static final String PARAM_ONLY_NEW = "PARAM_ONLY_NEW";
    private static final String RESULT_UNIT = "RESULT_UNIT";
    private static final String RESULT_ORDER = "RESULT_ORDER";
    private static final String RESULT_DESC = "RESULT_DESC";

    @Override
    protected TaskResult doCommand() {

        String serial = getArgs().getString(PARAM_GUID);
        String orderId = getArgs().getString(PARAM_ORDERID);
        String itemId = getArgs().getString(PARAM_ITEM);
        boolean onlyNew = getBooleanArg(PARAM_ONLY_NEW);

        Date minCreateTime = null;
        if (onlyNew) {
            minCreateTime = getApp().getMinSalesHistoryLimitDateDayRounded(Calendar.getInstance());
        }

        List<Unit> items = _wrap(syncQuery(serial, orderId, itemId).perform(getContext()), new UnitWrapFunction());
        if (items == null || items.size() == 0) {
            return failed().add(RESULT_DESC, "Nothing found");
        }

        ArrayList<SaleOrderViewModel> orders = new ArrayList<SaleOrderViewModel>();
        ArrayList<Unit> serials = new ArrayList<Unit>();
        SaleOrderViewFunction transform = new SaleOrderViewFunction();
        for (Unit unit : items) {
            Query query = ProviderAction.query(SALE_ORDER_URI)
                    .where(SaleOrderView2.SaleOrderTable.GUID + " = ?", unit.orderId);
            if (onlyNew && minCreateTime != null) {
                query.where("(" + SaleOrderView2.SaleOrderTable.CREATE_TIME + " >= " + minCreateTime.getTime() + " OR " + SaleOrderView2.TipsTable.CREATE_TIME + " >= " + minCreateTime.getTime() + " OR " + SaleOrderTipsQuery.MAX_REFUND_CREATE_TIME + " >= " + minCreateTime.getTime() + ")");
            }
            Cursor cursor = query.perform(getContext());

            if (cursor.moveToFirst()) {
                orders.add(transform.apply(cursor));
                serials.add(unit);
            }
            cursor.close();
        }
        if (serials.size() == 0) {
            return failed().add(RESULT_DESC, "Nothing found");
        }

        return succeeded().add(RESULT_UNIT, serials).add(RESULT_ORDER, orders);
    }

    @Override
    protected ArrayList<ContentProviderOperation> createDbOperations() {
        return null;
    }

    @Override
    protected boolean validateAppCommandContext() {
        return false;
    }

    @Override
    protected ISqlCommand createSqlCommand() {
        return null;
    }

    public static Query syncQuery(String serial, String orderId, String itemId) {
        Query query = ProviderAction.query(UNIT_URI);
        query.where(UnitTable.STATUS + " = ?", Unit.Status.SOLD.ordinal());
        if (serial != null) {
            query = query.where(UnitTable.SERIAL_CODE + " = ?", serial);
        }
        if (orderId != null) {
            query = query.where(UnitTable.SALE_ORDER_ID + " = ?", orderId);
        } else {
            query.where(UnitTable.SALE_ORDER_ID + " is not null");
        }
        if (itemId != null) {
            query = query.where(UnitTable.ITEM_ID + " = ?", itemId);
        }

        query = query.where(UnitTable.IS_DELETED + " = ?", 0);
        return query;
    }

    public static final TaskHandler start(Context context,
                                          String unit,
                                          String item,
                                          String orderId,
                                          UnitCallback callback) {
        return  start(context, unit, item, orderId, false, callback);
    }

    public static final TaskHandler start(Context context,
                                           String unit,
                                           String item,
                                           String orderId,
                                           boolean onlyNew,
                                           UnitCallback callback) {
        return  create(SearchUnitCommand.class)
                .arg(PARAM_GUID, unit)
                .arg(PARAM_ITEM, item)
                .arg(PARAM_ORDERID, orderId)
                .arg(PARAM_ONLY_NEW, onlyNew)
                .callback(callback)
                .queueUsing(context);
    }

    public static abstract class UnitCallback {

        @OnSuccess(SearchUnitCommand.class)
        public final void onSuccess(@Param(RESULT_UNIT) ArrayList<Unit> unit, @Param(RESULT_ORDER)ArrayList<SaleOrderViewModel> order
        ) {
            handleSuccess(unit, order);
        }

        protected abstract void handleSuccess(ArrayList<Unit> unit, ArrayList<SaleOrderViewModel> order);

        @OnFailure(SearchUnitCommand.class)
        public final void onFailure(@Param(RESULT_DESC) String message) {
            handleError(message);
        }

        protected abstract void handleError(String message);
    }
}
