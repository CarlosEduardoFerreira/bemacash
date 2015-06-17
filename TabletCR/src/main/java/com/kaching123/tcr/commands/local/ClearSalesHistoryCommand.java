package com.kaching123.tcr.commands.local;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.getbase.android.db.provider.ProviderAction;
import com.kaching123.tcr.Logger;
import com.kaching123.tcr.model.Unit;
import com.kaching123.tcr.model.Unit.Status;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopProviderExt;
import com.kaching123.tcr.store.ShopProviderExt.Method;
import com.kaching123.tcr.store.ShopStore.BillPaymentDescriptionTable;
import com.kaching123.tcr.store.ShopStore.EmployeeTipsTable;
import com.kaching123.tcr.store.ShopStore.ItemMovementTable;
import com.kaching123.tcr.store.ShopStore.OldMovementGroupsQuery;
import com.kaching123.tcr.store.ShopStore.OldSaleOrdersQuery;
import com.kaching123.tcr.store.ShopStore.SaleOrderTable;
import com.kaching123.tcr.store.ShopStore.UnitTable;
import com.telly.groundy.Groundy;
import com.telly.groundy.PublicGroundyTask;
import com.telly.groundy.TaskResult;
import com.telly.groundy.annotations.OnFailure;
import com.telly.groundy.annotations.OnSuccess;

import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static com.kaching123.tcr.model.ContentValuesUtil._enum;

/**
 * Created by pkabakov on 24.12.2014.
 */
public class ClearSalesHistoryCommand extends PublicGroundyTask {

    private static final int MAX_PARAMETERS_COUNT = 999;

    private static final Uri OLD_SALE_ORDERS_URI = ShopProvider.contentUri(OldSaleOrdersQuery.CONTENT_PATH);
    private static final Uri OLD_MOVEMENT_GROUPS_URI = ShopProvider.contentUri(OldMovementGroupsQuery.CONTENT_PATH);

    private static final Uri SALE_ORDERS_NO_NOTIFY_URI = ShopProvider.contentUriNoNotify(SaleOrderTable.URI_CONTENT);
    private static final Uri TIPS_NO_NOTIFY_URI = ShopProvider.contentUriNoNotify(EmployeeTipsTable.URI_CONTENT);
    private static final Uri BILL_PAYMENTS_NO_NOTIFY_URI = ShopProvider.contentUriNoNotify(BillPaymentDescriptionTable.URI_CONTENT);
    private static final Uri ITEM_MOVEMENTS_NO_NOTIFY_URI = ShopProvider.contentUriNoNotify(ItemMovementTable.URI_CONTENT);
    private static final Uri UNITS_NO_NOTIFY_URI = ShopProvider.contentUriNoNotify(UnitTable.URI_CONTENT);

    private static final Uri SALE_ORDERS_URI = ShopProvider.contentUri(SaleOrderTable.URI_CONTENT);
    private static final Uri UNITS_URI = ShopProvider.contentUri(UnitTable.URI_CONTENT);
    private static final Uri TIPS_URI = ShopProvider.contentUri(EmployeeTipsTable.URI_CONTENT);
    private static final Uri BILL_PAYMENTS_URI = ShopProvider.contentUri(BillPaymentDescriptionTable.URI_CONTENT);
    private static final Uri ITEM_MOVEMENTS_URI = ShopProvider.contentUri(ItemMovementTable.URI_CONTENT);

    @Override
    protected TaskResult doInBackground() {
        if (getApp().isTrainingMode())
            return succeeded();

        Logger.d("ClearSalesHistoryCommand: start");
        getApp().lockOnSalesHistory();
        Logger.d("ClearSalesHistoryCommand: locked");
        try {
            Integer limitDays = getApp().getSalesHistoryLimit();
            Logger.d("ClearSalesHistoryCommand: limit: " + limitDays + " days");
            if (limitDays == null) {
                Logger.w("ClearSalesHistoryCommand: limit not set - command skipped");
                return succeeded();
            }

            long minCreateTime = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(limitDays);
            Logger.d("ClearSalesHistoryCommand: min date: " + new Date(minCreateTime));

            ShopProviderExt.callMethod(getContext(), Method.TRANSACTION_START, null, null);
            Logger.d("ClearSalesHistoryCommand: start transaction");
            try {
                Cursor c;
                //TODO: improve - get size of cursor
                ArrayList<String> orderGuids = new ArrayList<String>();
                StringBuilder inBuilder = new StringBuilder();
                do {
                    c = ProviderAction.query(OLD_SALE_ORDERS_URI)
                            .where("",
                                    minCreateTime,
                                    minCreateTime,
                                    minCreateTime,
                                    MAX_PARAMETERS_COUNT)
                            .perform(getContext());
                    Logger.d("ClearSalesHistoryCommand: old sale orders loaded, count: " + c.getCount());

                    if (c.getCount() == 0) {
                        c.close();
                        break;
                    }

                    while (c.moveToNext()) {
                        orderGuids.add(c.getString(0));
                    }
                    c.close();

                    int deleted = ProviderAction.delete(SALE_ORDERS_NO_NOTIFY_URI)
                            .where(SaleOrderTable.GUID + getWhereIn(orderGuids.size(), inBuilder), orderGuids.toArray(new String[orderGuids.size()]))
                            .perform(getContext());
                    Logger.d("ClearSalesHistoryCommand: orders removed: " + deleted);

                    orderGuids.clear();
                } while (c.getCount() > 0);
                inBuilder.setLength(0);

                Logger.d("ClearSalesHistoryCommand: trying to remove tips without orders");
                int count = ProviderAction.delete(TIPS_NO_NOTIFY_URI)
                        .where(EmployeeTipsTable.ORDER_ID + " is null ")
                        .where(EmployeeTipsTable.CREATE_TIME + " < ? ", minCreateTime)
                        .perform(getContext());
                Logger.d("ClearSalesHistoryCommand: tips without orders removed: " + count);

                Logger.d("ClearSalesHistoryCommand: trying to remove prepaids without orders");
                count = ProviderAction.delete(BILL_PAYMENTS_NO_NOTIFY_URI)
                        .where(BillPaymentDescriptionTable.ORDER_ID + " is null ")
                        .perform(getContext());
                Logger.d("ClearSalesHistoryCommand: prepaids without orders removed: " + count);

                Logger.d("ClearSalesHistoryCommand: trying to remove sold units without orders");
                count = ProviderAction.delete(UNITS_NO_NOTIFY_URI)
                        .where(UnitTable.STATUS + " = ?", Status.SOLD.ordinal())
                        .where(UnitTable.SALE_ORDER_ID + " is null")
                        .perform(getContext());
                Logger.d("ClearSalesHistoryCommand: units without orders removed: " + count);

                int totalCount = 0;
				//TODO: use one collection?
                ArrayList<String> updateQtyFlags = new ArrayList<String>();
                do {
                    c = ProviderAction.query(OLD_MOVEMENT_GROUPS_URI)
                            .where("", minCreateTime, MAX_PARAMETERS_COUNT)
                            .perform(getContext());
                    Logger.d("ClearSalesHistoryCommand: old item movement groups loaded, count: " + c.getCount());

                    if (c.getCount() == 0) {
                        c.close();
                        break;
                    }

                    while (c.moveToNext()) {
                        updateQtyFlags.add(c.getString(0));
                    }
                    c.close();

                    int deleted = ProviderAction.delete(ITEM_MOVEMENTS_NO_NOTIFY_URI)
                            .where(ItemMovementTable.ITEM_UPDATE_QTY_FLAG + getWhereIn(updateQtyFlags.size(), inBuilder), updateQtyFlags.toArray(new String[updateQtyFlags.size()]))
                            .perform(getContext());
                    totalCount += deleted;
                    Logger.d("ClearSalesHistoryCommand: group movements removed: " + count);

                    updateQtyFlags.clear();
                } while (c.getCount() > 0);

                Logger.d("ClearSalesHistoryCommand: totally movements removed: " + totalCount);

                ShopProviderExt.callMethod(getContext(), Method.TRANSACTION_COMMIT, null, null);
                Logger.d("ClearSalesHistoryCommand: commit transaction");
            } finally {
                ShopProviderExt.callMethod(getContext(), Method.TRANSACTION_END, null, null);
                Logger.d("ClearSalesHistoryCommand: end transaction");
            }

            if (getApp().isInvalidOrdersFound()) {
                getApp().setInvalidOrdersFound(false);
                Logger.w("[INVALID ORDERS] ClearSalesHistoryCommand: invalid old active unit orders flag cleared");
            }

            getContext().getContentResolver().notifyChange(UNITS_URI, null);
            getContext().getContentResolver().notifyChange(SALE_ORDERS_URI, null);
            getContext().getContentResolver().notifyChange(ITEM_MOVEMENTS_URI, null);
            getContext().getContentResolver().notifyChange(TIPS_URI, null);
            getContext().getContentResolver().notifyChange(BILL_PAYMENTS_URI, null);
            Logger.d("ClearSalesHistoryCommand: ui notified");
        } finally {
            getApp().unlockOnSalesHistory();
            Logger.d("ClearSalesHistoryCommand: unlocked");
        }

        Logger.d("ClearSalesHistoryCommand: end");
        return succeeded();
    }

    private static String getWhereIn(int count, StringBuilder inBuilder) {
        inBuilder.setLength(0);
        inBuilder.append(" in (");
        for (int j = 0; j < count; j++) {
            if (j > 0)
                inBuilder.append(',');
            inBuilder.append('?');
        }
        inBuilder.append(")");
        return inBuilder.toString();
    }

    public static void start(Context context, BaseClearSalesHistoryCallback callback) {
        Groundy.create(ClearSalesHistoryCommand.class).callback(callback).queueUsing(context);
    }

    public static abstract class BaseClearSalesHistoryCallback {

        @OnSuccess(ClearSalesHistoryCommand.class)
        public void handleSuccess() {
            onSuccess();
        }

        @OnFailure(ClearSalesHistoryCommand.class)
        public void handleFailure() {
            onFailure();
        }

        protected abstract void onSuccess();

        protected abstract void onFailure();

    }
}
