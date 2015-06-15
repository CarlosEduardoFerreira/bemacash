package com.kaching123.tcr.commands.local;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.getbase.android.db.provider.ProviderAction;
import com.kaching123.tcr.model.Unit.Status;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopProviderExt;
import com.kaching123.tcr.store.ShopProviderExt.Method;
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

import java.util.concurrent.TimeUnit;

import static com.kaching123.tcr.model.ContentValuesUtil._enum;

/**
 * Created by pkabakov on 24.12.2014.
 */
public class ClearSalesHistoryCommand extends PublicGroundyTask {

    private static final Uri OLD_SALE_ORDERS_URI = ShopProvider.contentUri(OldSaleOrdersQuery.CONTENT_PATH);
    private static final Uri OLD_MOVEMENT_GROUPS_URI = ShopProvider.contentUri(OldMovementGroupsQuery.CONTENT_PATH);

    private static final Uri SALE_ORDERS_NO_NOTIFY_URI = ShopProvider.contentUriNoNotify(SaleOrderTable.URI_CONTENT);
    private static final Uri UNITS_NO_NOTIFY_URI = ShopProvider.contentUriNoNotify(UnitTable.URI_CONTENT);
    private static final Uri ITEM_MOVEMENTS_NO_NOTIFY_URI = ShopProvider.contentUriNoNotify(ItemMovementTable.URI_CONTENT);
    private static final Uri SALE_ORDERS_URI = ShopProvider.contentUri(SaleOrderTable.URI_CONTENT);
    private static final Uri UNITS_URI = ShopProvider.contentUri(UnitTable.URI_CONTENT);
    private static final Uri ITEM_MOVEMENTS_URI = ShopProvider.contentUri(ItemMovementTable.URI_CONTENT);

    @Override
    protected TaskResult doInBackground() {
        getApp().lockOnSalesHistory();
        try {
            //TODO: refactor?
            int limitDays = getApp().getSalesHistoryLimit();
            long minCreateTime = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(limitDays);

            ShopProviderExt.callMethod(getContext(), Method.TRANSACTION_START, null, null);
            try {
                ProviderAction.update(UNITS_NO_NOTIFY_URI)
                        .value(UnitTable.SALE_ORDER_ID, null)
                        .where(UnitTable.SALE_ORDER_ID + " IS NOT NULL AND " + UnitTable.STATUS + " != ?", _enum(Status.SOLD));

                Cursor c = ProviderAction.query(OLD_SALE_ORDERS_URI)
                        .where("",
                                minCreateTime,
                                minCreateTime,
                                minCreateTime)
                        .perform(getContext());

                while (c.moveToNext()) {
                    String orderGuid = c.getString(0);

                    ProviderAction.update(UNITS_NO_NOTIFY_URI)
                            .value(UnitTable.CHILD_ORDER_ID, null)
                            .where(UnitTable.CHILD_ORDER_ID + " = ? AND " + UnitTable.STATUS + " != ?", orderGuid, _enum(Status.SOLD));

                    ProviderAction.delete(SALE_ORDERS_NO_NOTIFY_URI)
                            .where(SaleOrderTable.GUID + " = ?", orderGuid)
                            .perform(getContext());
                }
                c.close();

                c = ProviderAction.query(OLD_MOVEMENT_GROUPS_URI)
                        .where("",
                                minCreateTime,
                                minCreateTime)
                        .perform(getContext());

                while (c.moveToNext()) {
                    String updateQtyFlag = c.getString(0);

                    ProviderAction.delete(ITEM_MOVEMENTS_NO_NOTIFY_URI)
                            .where(ItemMovementTable.ITEM_UPDATE_QTY_FLAG + " = ?", updateQtyFlag)
                            .perform(getContext());
                }
                c.close();

                ShopProviderExt.callMethod(getContext(), Method.TRANSACTION_COMMIT, null, null);
            } finally {
                ShopProviderExt.callMethod(getContext(), Method.TRANSACTION_END, null, null);
            }

            getContext().getContentResolver().notifyChange(UNITS_URI, null);
            getContext().getContentResolver().notifyChange(SALE_ORDERS_URI, null);
            getContext().getContentResolver().notifyChange(ITEM_MOVEMENTS_URI, null);
        } finally {
            getApp().unlockOnSalesHistory();
        }

        return succeeded();
    }

    private static void start(Context context, BaseClearSalesHistoryCallback callback) {
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
