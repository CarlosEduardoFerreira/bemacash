package com.kaching123.tcr.commands.wireless;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.getbase.android.db.provider.ProviderAction;
import com.kaching123.tcr.commands.store.AsyncCommand;
import com.kaching123.tcr.model.OrderStatus;
import com.kaching123.tcr.model.SaleOrderModel;
import com.kaching123.tcr.model.Unit;
import com.kaching123.tcr.service.ISqlCommand;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore;
import com.telly.groundy.TaskResult;
import com.telly.groundy.annotations.OnCancel;
import com.telly.groundy.annotations.OnFailure;
import com.telly.groundy.annotations.OnSuccess;

import java.util.ArrayList;

/**
 * Created by mayer
 */
public class UnitOrderDoubleCheckCommand extends AsyncCommand {

    protected static final Uri URI_ORDER = ShopProvider.getContentUri(ShopStore.SaleOrderTable.URI_CONTENT);


    private static final String ARG_ORDER_GUID = "ARG_ORDER_GUID";
    private static final String ARG_UNIT = "ARGUnit";


    public static void start(Context context, Unit unit, String orderGuid, UnitCallback callback) {
        create(UnitOrderDoubleCheckCommand.class).arg(ARG_ORDER_GUID, orderGuid)
                .arg(ARG_UNIT, unit).callback(callback).queueUsing(context);
    }

    @Override
    protected TaskResult doCommand() {
        String guid = getStringArg(ARG_ORDER_GUID);
        Unit unit = (Unit) getArgs().getSerializable(ARG_UNIT);
        Cursor c = ProviderAction.query(URI_ORDER)
                .where(ShopStore.SaleOrderTable.GUID + " = ?", unit.orderId)
                .perform(getContext());
        SaleOrderModel order = null;
        if(c.moveToFirst()){
            order = new SaleOrderModel(c);
        }
        c.close();
        boolean ok = guid == null || !guid.equals(unit.orderId);
        boolean isNotReturned = order.orderStatus.equals(OrderStatus.COMPLETED) && unit.status.equals(Unit.Status.SOLD);
        if (isNotReturned) {
            return failed();
        }
        if (order != null && unit.childOrderId != null) {
            c = ProviderAction.query(URI_ORDER)
                    .where(ShopStore.SaleOrderTable.GUID + " = ?", unit.childOrderId)
                    .perform(getContext());
            SaleOrderModel child = null;
            if (c.moveToFirst()) {
                child = new SaleOrderModel(c);
            }
            c.close();
            if (child != null && child.parentGuid != null && child.parentGuid.equals(unit.orderId)) {
                return cancelled();
            } else {
                return succeeded();
            }
        }
        if (ok) {
            if (order == null) {
                return cancelled();
            } else {
                return succeeded();
            }
        } else {
            return failed();
        }
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

    public static abstract class UnitCallback {

        @OnSuccess(UnitOrderDoubleCheckCommand.class)
        public final void onSuccess() {
            handleSuccess();
        }
        protected abstract void handleSuccess();

        @OnFailure(UnitOrderDoubleCheckCommand.class)
        public final void onFailure() {
            handleError();
        }
        protected abstract void handleError();

        @OnCancel(UnitOrderDoubleCheckCommand.class)
        public final void onCancel() {
            handleFeelFreeToAdd();
        }
        protected abstract void handleFeelFreeToAdd();
    }
}
