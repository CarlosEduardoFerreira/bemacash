package com.kaching123.tcr.commands.payment.blackstone.prepaid;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.net.Uri;

import com.kaching123.tcr.commands.store.AsyncCommand;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.jdbc.converters.BillPaymentDescriptionJdbcConverter;
import com.kaching123.tcr.service.ISqlCommand;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore.BillPaymentDescriptionTable;
import com.telly.groundy.TaskResult;

import java.util.ArrayList;

/**
 * Created by vkompaniets on 23.05.2014.
 */
public class UpdateBillPaymentFailedStatusCommand extends AsyncCommand {

    private final static Uri URI_BILL_PAYMENT_DESCRIPTION = ShopProvider.getContentUri(BillPaymentDescriptionTable.URI_CONTENT);

    private long prepaidOrderId;

    @Override
    protected TaskResult doCommand() {
        return succeeded();
    }

    @Override
    protected ArrayList<ContentProviderOperation> createDbOperations() {
        ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>(1);
        operations.add(ContentProviderOperation.newUpdate(URI_BILL_PAYMENT_DESCRIPTION)
                .withSelection(BillPaymentDescriptionTable.PREPAID_ORDER_ID + " = ?", new String[]{String.valueOf(prepaidOrderId)})
                .withValue(BillPaymentDescriptionTable.IS_FAILED, 1)
                .build());
        return operations;
    }

    @Override
    protected ISqlCommand createSqlCommand() {
        return ((BillPaymentDescriptionJdbcConverter)JdbcFactory.getConverter(BillPaymentDescriptionTable.TABLE_NAME)).updateFailedStatusSQL(prepaidOrderId, true, getAppCommandContext());
    }

    public boolean sync(Context context, long prepaidOrderId, IAppCommandContext appCommandContext) {
        this.prepaidOrderId = prepaidOrderId;
        return isFailed(syncStandalone(context, appCommandContext));
    }
}
