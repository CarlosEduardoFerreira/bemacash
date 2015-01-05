package com.kaching123.tcr.commands.store.saleorder;

import android.content.Context;
import android.database.Cursor;

import com.getbase.android.db.provider.ProviderAction;
import com.kaching123.tcr.model.SaleOrderModel;
import com.kaching123.tcr.store.ShopStore;
import com.telly.groundy.annotations.OnSuccess;

/**
 * Created by gdubina on 11/11/13.
 */
public class HoldOrderCommand extends UpdateSaleOrderCommand {

    private static final String ARG_ORDER_GUID = "ARG_ORDER_GUID";
    private static final String ARG_TITLE = "ARG_ORDER_TITLE";

    @Override
    protected SaleOrderModel readOrder() {
        String guid = getStringArg(ARG_ORDER_GUID);
        String title = getStringArg(ARG_TITLE);

        Cursor c = ProviderAction.query(URI_ORDER)
            .where(ShopStore.SaleOrderTable.GUID + " = ?", guid)
            .perform(getContext());
        try {
            SaleOrderModel order = null;
            if (c.moveToFirst()) {
                order = new SaleOrderModel(c);
            }
            if (order == null)
                return null;
            order.setHoldName(title);
            return order;
        } finally {
            c.close();
        }
    }

    public static void start(Context context, BaseHoldOrderCallback callback, String orderGuid, String title) {
        create(HoldOrderCommand.class)
                .arg(ARG_ORDER_GUID, orderGuid)
                .arg(ARG_TITLE, title).callback(callback).queueUsing(context);
    }

    public static abstract class BaseHoldOrderCallback {

        @OnSuccess(HoldOrderCommand.class)
        public void handleSuccess(){
            onSuccess();
        }

        protected abstract void onSuccess();

    }
}