package com.kaching123.tcr.commands.payment.blackstone.prepaid.update;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.net.Uri;

import com.kaching123.tcr.commands.payment.SOAPWebCommand;
import com.kaching123.tcr.model.payment.blackstone.prepaid.wireless.WirelessItem;
import com.kaching123.tcr.model.payment.blackstone.prepaid.wireless.request.GetProductListRequest;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore.WirelessTable;
import com.kaching123.tcr.websvc.api.prepaid.Broker;
import com.kaching123.tcr.websvc.api.prepaid.ProductListResponse;
import com.telly.groundy.TaskHandler;
import com.telly.groundy.TaskResult;

import java.util.ArrayList;

/**
 * @author Ivan v. Rikhmayer
 *         This class is intended to
 */
public class UpdateMerchantFlagCommand extends SOAPWebCommand<GetProductListRequest> {

    private static final Uri URI_WIRELESS = ShopProvider.getContentUri(WirelessTable.URI_CONTENT);

    protected final static String ARG_REQUEST = "request";
    public final static String ARG_RESULT = "ARG_RESULT";

    protected ProductListResponse response;
    protected GetProductListRequest request;

    public  final static TaskHandler start(Context context, Object callback, GetProductListRequest request) {
        return create(UpdateMerchantFlagCommand.class)
                .arg(ARG_REQUEST, request)
                .callback(callback)
                .queueUsing(context);
    }

    @Override
    protected boolean validateAppCommandContext() {
        return false;
    }

    /** it's not a asynccommand, need for sync. can be standalone **/
    public ProductListResponse sync(Context context, GetProductListRequest request) {
        this.request = request;
        //no need in command cache(creds passed on start)
        TaskResult result = syncStandalone(context, null, null);
        return isFailed(result) ? null : response;
    }

    @Override
    protected TaskResult doInBackground() {
        return super.doInBackground().add(ARG_RESULT, response);
    }

    @Override
    protected Long doCommand(Broker brokerApi, GetProductListRequest request) {
        response = brokerApi.GetProductList(
                request.mID,
                request.tID,
                request.password,
                request.transactionId,
                getSign(request.mID,
                        request.tID,
                        request.password,
                        null,
                        request.transactionId));
        return  response != null ? response.resultId : null;
    }

    @Override
    protected ArrayList<ContentProviderOperation> createDbOperations() {
        ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();

        operations.add(ContentProviderOperation.newDelete(URI_WIRELESS).build());

        int count = response.products == null ? 0 : response.products.size();
        for (int i = 0; i < count; i++) {
            operations.add(ContentProviderOperation.newInsert(URI_WIRELESS)
                    .withValues(new WirelessItem(response.products.get(i)).toValues())
                    .build());
        }

        return operations;
    }

    @Override
    protected GetProductListRequest getRequest() {
        return request == null ? (GetProductListRequest) getArgs().getSerializable(ARG_REQUEST) : request;
    }
}
