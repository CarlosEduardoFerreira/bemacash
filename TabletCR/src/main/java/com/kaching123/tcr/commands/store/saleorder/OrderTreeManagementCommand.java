package com.kaching123.tcr.commands.store.saleorder;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.getbase.android.db.provider.ProviderAction;
import com.telly.groundy.TaskHandler;
import com.telly.groundy.TaskResult;
import com.telly.groundy.annotations.OnFailure;
import com.telly.groundy.annotations.OnSuccess;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.commands.store.AsyncCommand;
import com.kaching123.tcr.service.ISqlCommand;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore.SaleOrderItemsMappingQuery;

import static com.kaching123.tcr.util.ContentValuesUtilBase._decimalQty;

/**
 * Created by Hans on 6/24/2015.
 */
public class OrderTreeManagementCommand extends AsyncCommand {

    private static final String ORDER_ITEM_GUID = "ORDER_ITEM_GUID";
    private static final Uri URI_ITEMS = ShopProvider.contentUri(SaleOrderItemsMappingQuery.URI_CONTENT);

    protected String orderItemId;
    protected ArrayList<ContentProviderOperation> ops;
    protected List<MovementMetadata> result;

    @Override
    protected TaskResult doCommand() {
        List<MovementMetadata> metadata = getMetadata();
        if (metadata != null) {
            for (MovementMetadata meta : metadata) {
                Logger.d("transaction : " + meta.toString());
            }
        }
        return succeeded();
    }

    @Override
    protected ISqlCommand createSqlCommand() {
        return null;
    }

    @Override
    protected ArrayList<ContentProviderOperation> createDbOperations() {
        return null;
    }

    private List<MovementMetadata> getMetadata() {
        Cursor c = ProviderAction
                .query(URI_ITEMS)
                .projection(SaleOrderItemsMappingQuery.ITEM_GUID,
                        SaleOrderItemsMappingQuery.QUANTITY,
                        SaleOrderItemsMappingQuery.SOURCE,
                        SaleOrderItemsMappingQuery.FLAG,
                        SaleOrderItemsMappingQuery.STOCK_TRACKING)
                .where("", orderItemId, orderItemId, orderItemId, orderItemId)
                .perform(getContext());
        try {
            if (!c.moveToFirst()) {
                return null;
            }
            result = new ArrayList<>(c.getCount());
            do {
                MovementMetadata mv = new MovementMetadata();
                mv.guid = c.getString(0);
                mv.movement = c.getInt(c.getColumnIndex(SaleOrderItemsMappingQuery.STOCK_TRACKING)) == 1 ? _decimalQty(c, 1): BigDecimal.ZERO;
                mv.tag = c.getString(3);
                mv.flag = c.getString(2);
                result.add(mv);
            } while (c.moveToNext());
            return result;
        } finally {
            c.close();
        }
    }

    public static final TaskHandler start(Context context, String orderItemId, Callback callback) {
        return create(OrderTreeManagementCommand.class)
                .arg(ORDER_ITEM_GUID, orderItemId)
                .callback(callback)
                .queueUsing(context);
    }

    public List<MovementMetadata> sync(Context context, String guid, IAppCommandContext appCommandContext) {
        this.orderItemId = guid;
        super.sync(context, null, appCommandContext);
        return result;
    }

    public static abstract class Callback {

        @OnSuccess(OrderTreeManagementCommand.class)
        public final void onSuccess() {
            handleSuccess();
        }

        protected abstract void handleSuccess();

        @OnFailure(OrderTreeManagementCommand.class)
        public final void onFailure() {
            handleError();
        }

        protected abstract void handleError();
    }

    public class MovementMetadata {

        private String guid;
        private String tag;
        private BigDecimal movement;
        private String flag;

        @Override
        public String toString() {
            return "guid " + guid + " | tag : " + tag + " | movement : " + movement;
        }

        public String getGuid() {
            return guid;
        }

        public String getTag() {
            return tag;
        }

        public BigDecimal getMovement() {
            return movement;
        }

        public String getFlag() {
            return flag;
        }
    }
}
