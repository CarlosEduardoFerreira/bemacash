package com.kaching123.tcr.commands.store.saleorder;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.getbase.android.db.provider.ProviderAction;
import com.kaching123.tcr.processor.MoneybackProcessor.RefundSaleItemInfo;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopSchema2.SaleItemExView2.SaleItemTable;
import com.kaching123.tcr.store.ShopStore.SaleItemExView;
import com.telly.groundy.PublicGroundyTask;
import com.telly.groundy.TaskResult;
import com.telly.groundy.annotations.OnFailure;
import com.telly.groundy.annotations.OnSuccess;
import com.telly.groundy.annotations.Param;

import java.util.ArrayList;

import static com.kaching123.tcr.model.ContentValuesUtil._decimalQty;

/**
 * Created by gdubina on 17/03/14.
 */
public class GetItemsForFakeVoidCommand extends PublicGroundyTask{

    private static final Uri URI_SALE_ITEMS = ShopProvider.getContentUri(SaleItemExView.URI_CONTENT);
    private static final String ARG_ORDER_GUID = "ARG_ORDER_GUID";
    private static final String EXTRA_RESULT = "EXTRA_RESULT";

    @Override
    protected TaskResult doInBackground() {
        String orderGuid = getStringArg(ARG_ORDER_GUID);
        Cursor c = ProviderAction
                .query(URI_SALE_ITEMS)
                .projection(SaleItemTable.SALE_ITEM_GUID, SaleItemTable.QUANTITY)
                .where(SaleItemTable.ORDER_GUID + " = ?", orderGuid)
                .perform(getContext());
        ArrayList<RefundSaleItemInfo> result = new ArrayList<RefundSaleItemInfo>();
        while(c.moveToNext()){
            result.add(new RefundSaleItemInfo(c.getString(0), _decimalQty(c, 1)));
        }
        c.close();
        return succeeded().add(EXTRA_RESULT, result);
    }

    public static void start(Context context, String orderGuid, BaseGetItemsForFaickVoidCallback callback){
        create(GetItemsForFakeVoidCommand.class)
                .arg(ARG_ORDER_GUID, orderGuid)
                .callback(callback)
                .queueUsing(context);
    }

    public static abstract class BaseGetItemsForFaickVoidCallback{

        @OnSuccess(GetItemsForFakeVoidCommand.class)
        public final void onSuccess(@Param(EXTRA_RESULT) ArrayList<RefundSaleItemInfo> result){
            handleSuccess(result);
        }

        @OnFailure(GetItemsForFakeVoidCommand.class)
        public final void onFailure(){
            handleFailure();
        }

        protected abstract void handleSuccess(ArrayList<RefundSaleItemInfo> result);

        protected abstract void handleFailure();
    }
}
