package com.kaching123.tcr.commands.wireless;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.getbase.android.db.provider.ProviderAction;
import com.getbase.android.db.provider.Query;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopSchema2;
import com.kaching123.tcr.store.ShopStore;
import com.telly.groundy.PublicGroundyTask;
import com.telly.groundy.TaskResult;
import com.telly.groundy.annotations.OnFailure;
import com.telly.groundy.annotations.OnSuccess;
import com.telly.groundy.annotations.Param;

/**
 * Created by teli.yin on 10/10/2014.
 */
public class WirelessConfirmationCommand extends PublicGroundyTask {

    public static final String RESULT_TOTAL = "RESULT_TOTAL";
    public static final String RESULT_ORDER_NUMBER = "RESULT_ORDER_NUMBER";
    public static final String ARG_ORDER_GUID = "ARG_ORDER_GUID";
    private static final Uri URI_ORDER = ShopProvider.getContentUri(ShopStore.SaleOrderView.URI_CONTENT);

    @Override
    protected TaskResult doInBackground() {

        String orderGuid = getStringArg(ARG_ORDER_GUID);
        PrepaidPrintInfo info = null;

        try {
            info = getPrepaidPrintInfo(orderGuid);
        } catch (Exception e) {

        }
        return succeeded().add(RESULT_ORDER_NUMBER, info.orderNum).add(RESULT_TOTAL, info.total);

    }

    protected PrepaidPrintInfo getPrepaidPrintInfo(String orderGuid) {

        Query query = ProviderAction
                .query(URI_ORDER)
                .projection(
                        ShopSchema2.SaleOrderView2.SaleOrderTable.TML_TOTAL_PRICE,
                        ShopSchema2.SaleOrderView2.RegisterTable.TITLE,
                        ShopSchema2.SaleOrderView2.SaleOrderTable.PRINT_SEQ_NUM)
                .where(ShopSchema2.SaleOrderView2.SaleOrderTable.GUID + " = ?", orderGuid);
        Cursor c = query.perform(getContext());
        String total = null;
        String title = null;
        int seqNum = 0;
        if (c.moveToFirst()) {
            total = c.getString(0);
            title = c.getString(1);
            seqNum = c.getInt(2);
        }
        c.close();


        return new PrepaidPrintInfo(total, title + "-" + seqNum);
    }

    class PrepaidPrintInfo {
        String total;
        String orderNum;

        public PrepaidPrintInfo(String total, String orderNum) {
            this.total = total;
            this.orderNum = orderNum;
        }
    }

    public static void start(Context context, String orderGuid, wirelessConfiramtionCommandCallback callback) {
        create(WirelessConfirmationCommand.class).arg(ARG_ORDER_GUID, orderGuid).callback(callback).queueUsing(context);
    }

    public static abstract class wirelessConfiramtionCommandCallback {

        @OnSuccess(WirelessConfirmationCommand.class)
        public void onSuccess(@Param(RESULT_ORDER_NUMBER) String orderNum, @Param(RESULT_TOTAL) String total) {
            handleSuccess(orderNum, total);
        }

        @OnFailure(WirelessConfirmationCommand.class)
        public void onFailure() {
            handleFailure();
        }

        protected abstract void handleSuccess(String orderNum, String total);

        protected abstract void handleFailure();

    }
}
