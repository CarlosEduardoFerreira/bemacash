package com.kaching123.tcr.commands.store.history;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.getbase.android.db.provider.ProviderAction;
import com.kaching123.tcr.Logger;
import com.kaching123.tcr.commands.payment.PaymentGateway;
import com.kaching123.tcr.commands.store.AsyncCommand;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.model.PaymentTransactionModel;
import com.kaching123.tcr.model.SaleOrderModel;
import com.kaching123.tcr.model.TipsModel;
import com.kaching123.tcr.service.ISqlCommand;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore.EmployeeTipsTable;
import com.kaching123.tcr.util.CalculationUtil;
import com.telly.groundy.TaskResult;

import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

/**
 * Created by vkompaniets on 17.06.2014.
 */
public class AddRefundedTipsCommand extends AsyncCommand {

    private static final Uri URI_TIPS = ShopProvider.getContentUri(EmployeeTipsTable.URI_CONTENT);

    private SaleOrderModel returnOrder;
    private PaymentTransactionModel refundTransaction;

    private TipsModel tipsModel;

    @Override
    protected TaskResult doCommand() {
        Cursor c = ProviderAction.query(URI_TIPS)
                .where(EmployeeTipsTable.ORDER_ID + " = ?", returnOrder.parentGuid)
                .perform(getContext());

        if (c.moveToFirst()) {
            tipsModel = new TipsModel(c);
            tipsModel.parentId = tipsModel.id;
            tipsModel.id = UUID.randomUUID().toString();
            tipsModel.orderId = returnOrder.guid;
            tipsModel.shiftId = getAppCommandContext().getShiftGuid();
            tipsModel.createTime = new Date();
            tipsModel.amount = CalculationUtil.negative(tipsModel.amount);
            tipsModel.paymentTransactionId = refundTransaction.guid;
            tipsModel.paymentType = getPaymentType(refundTransaction.gateway);
        }
        c.close();

        if (tipsModel == null) {
            Logger.e("AddRefundedTipsCommand failed: order has no tips!");
            return failed();
        }

        return succeeded();
    }

    private static TipsModel.PaymentType getPaymentType(PaymentGateway gateway) {
        if (gateway == null)
            return null;

        if (gateway.isCreditCard()){
            return TipsModel.PaymentType.CREDIT;
        }else{
            return TipsModel.PaymentType.CASH;
        }
    }

    @Override
    protected ArrayList<ContentProviderOperation> createDbOperations() {
        ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>(1);
        operations.add(ContentProviderOperation.newInsert(URI_TIPS).withValues(tipsModel.toValues()).build());
        return operations;
    }

    @Override
    protected ISqlCommand createSqlCommand() {
        return JdbcFactory.getConverter(tipsModel).insertSQL(tipsModel, getAppCommandContext());
    }

    public SyncResult sync(Context context, SaleOrderModel returnOrder, PaymentTransactionModel refundTransaction, IAppCommandContext appCommandContext){
        this.returnOrder = returnOrder;
        this.refundTransaction = refundTransaction;
        return super.syncDependent(context, appCommandContext);
    }
}
