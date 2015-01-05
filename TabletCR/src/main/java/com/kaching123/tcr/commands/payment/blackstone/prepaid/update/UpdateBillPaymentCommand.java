package com.kaching123.tcr.commands.payment.blackstone.prepaid.update;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.net.Uri;

import com.kaching123.tcr.commands.store.AsyncCommand;
import com.kaching123.tcr.model.payment.blackstone.prepaid.wireless.request.BillPaymentItem;
import com.kaching123.tcr.service.ISqlCommand;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore;
import com.telly.groundy.TaskHandler;
import com.telly.groundy.TaskResult;

import java.util.ArrayList;

/**
 * @author Ivan v. Rikhmayer
 *         This class is intended to
 */
public class UpdateBillPaymentCommand extends AsyncCommand {

    private static final Uri URI_BILLPAYMENT = ShopProvider.getContentUri(ShopStore.BillPayment.URI_CONTENT);
    private final static String ARG_LIST = "ARG_LIST";
    private ArrayList<BillPaymentItem> list;


    public final static TaskHandler start(Context context, Object callback, ArrayList<BillPaymentItem> list) {
        return create(UpdateBillPaymentCommand.class)
                .arg(ARG_LIST, list)
                .callback(callback)
                .queueUsing(context);
    }


//    public final TaskResult sync(Context context, BillPaymentItem[] items) {
//        list = items;
//        return sync(context, getArgs(), getAppCommandContext());
//    }

    @Override
    protected TaskResult doCommand() {
        list = getArgs().getParcelableArrayList(ARG_LIST);
        return succeeded();
    }

    @Override
    protected ISqlCommand createSqlCommand() {
        return null;
    }

    @Override
    protected ArrayList<ContentProviderOperation> createDbOperations() {
        ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();

        operations.add(ContentProviderOperation.newDelete(URI_BILLPAYMENT).build());

        int count = list == null ? 0 : list.size();
        for (int i = 0; i < count; i++) {
            operations.add(ContentProviderOperation.newInsert(URI_BILLPAYMENT)
                    .withValues(list.get(i).toValues())
                    .build());
        }

        return operations;
    }

}