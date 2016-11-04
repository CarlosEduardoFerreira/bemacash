package com.kaching123.tcr.commands.store.saleorder;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.commands.store.AsyncCommand;
import com.kaching123.tcr.service.ISqlCommand;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore.ReturnOrderItemsMappingQuery;
import com.kaching123.tcr.store.ShopStore.SaleOrderItemsMappingQuery;
import com.telly.groundy.TaskHandler;
import com.telly.groundy.TaskResult;
import com.telly.groundy.annotations.OnFailure;
import com.telly.groundy.annotations.OnSuccess;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static com.kaching123.tcr.util.ContentValuesUtilBase._bool;
import static com.kaching123.tcr.util.ContentValuesUtilBase._decimalQty;
import static com.kaching123.tcr.util.CursorUtil._selectionArgs;

/**
 * Created by Hans on 6/24/2015.
 */
public class OrderTreeManagementCommand extends AsyncCommand {

    private static final String ORDER_ITEM_GUID = "ORDER_ITEM_GUID";
    private static final Uri URI_ITEMS = ShopProvider.contentUri(SaleOrderItemsMappingQuery.URI_CONTENT);
    private static final Uri URI_ITEMS_FOR_RETURN = ShopProvider.contentUri(ReturnOrderItemsMappingQuery.URI_CONTENT);

    private String orderItemId;
    protected ArrayList<ContentProviderOperation> ops;
    protected List<MovementMetadata> result;
    private boolean isReturn;
    private String saleItemGuid;

    @Override
    protected TaskResult doCommand() {
        List<MovementMetadata> metadata;

        if (isReturn) {
            metadata = getMetadataForReturn();
        } else {
            metadata = getMetadata();
        }

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

        Cursor c = getContext().getContentResolver().query(
                URI_ITEMS,
                new String[]{SaleOrderItemsMappingQuery.ITEM_GUID,
                        SaleOrderItemsMappingQuery.QUANTITY,
                        SaleOrderItemsMappingQuery.SOURCE,
                        SaleOrderItemsMappingQuery.FLAG,
                        SaleOrderItemsMappingQuery.STOCK_TRACKING},
                null,
                _selectionArgs(orderItemId, orderItemId, orderItemId, orderItemId),
                null
        );
        if (c != null && c.moveToFirst()) {

            result = new ArrayList<>(c.getCount());
            do {
                MovementMetadata mv = new MovementMetadata();
                mv.guid = c.getString(c.getColumnIndex(SaleOrderItemsMappingQuery.ITEM_GUID));

                boolean isStockTracking = _bool(c, c.getColumnIndex(SaleOrderItemsMappingQuery.STOCK_TRACKING));
                mv.movement = isStockTracking ? _decimalQty(c, c.getColumnIndex(SaleOrderItemsMappingQuery.QUANTITY_RESULT)) : BigDecimal.ZERO; // -1 *  SUM(QUANTITY_TAG) as QUANTITY_RESULT

                mv.tag = c.getString(c.getColumnIndex(SaleOrderItemsMappingQuery.SOURCE));
                mv.flag = c.getString(c.getColumnIndex(SaleOrderItemsMappingQuery.FLAG));
                result.add(mv);
            } while (c.moveToNext());
            c.close();
            return result;
        } else {
            return null;
        }
    }

    private List<MovementMetadata> getMetadataForReturn() {
        Cursor c = getContext().getContentResolver().query(
                URI_ITEMS_FOR_RETURN,
                new String[]{ReturnOrderItemsMappingQuery.ITEM_GUID,
                        ReturnOrderItemsMappingQuery.QUANTITY,
                        ReturnOrderItemsMappingQuery.SOURCE,
                        ReturnOrderItemsMappingQuery.FLAG,
                        ReturnOrderItemsMappingQuery.STOCK_TRACKING},
                null,
                _selectionArgs(orderItemId, saleItemGuid,
                        orderItemId, saleItemGuid,
                        orderItemId, saleItemGuid,
                        orderItemId, saleItemGuid),
                null
        );
        if (c != null && !c.moveToFirst()) {
            result = new ArrayList<>(c.getCount());
            do {
                MovementMetadata mv = new MovementMetadata();
                mv.guid = c.getString(c.getColumnIndex(ReturnOrderItemsMappingQuery.ITEM_GUID));

                boolean isStockTracking = _bool(c, c.getColumnIndex(ReturnOrderItemsMappingQuery.STOCK_TRACKING));
                if (!isStockTracking) {
                    continue;
                }
                //mv.movement = isStockTracking ? _decimalQty(c, c.getColumnIndex(ReturnOrderItemsMappingQuery.QUANTITY_RESULT)): BigDecimal.ZERO; // -1 *  SUM(QUANTITY_TAG) as QUANTITY_RESULT
                mv.movement = _decimalQty(c, c.getColumnIndex(ReturnOrderItemsMappingQuery.QUANTITY_RESULT));

                mv.tag = c.getString(c.getColumnIndex(ReturnOrderItemsMappingQuery.SOURCE));
                mv.flag = c.getString(c.getColumnIndex(ReturnOrderItemsMappingQuery.FLAG));
                result.add(mv);
            } while (c.moveToNext());

            c.close();
            return result;
        } else {
            return null;
        }

    }


    public static final TaskHandler start(Context context, String orderItemId, Callback callback) {
        return create(OrderTreeManagementCommand.class)
                .arg(ORDER_ITEM_GUID, orderItemId)
                .callback(callback)
                .queueUsing(context);
    }

    public List<MovementMetadata> sync(Context context, String guid, String saleItemGuid, boolean isReturn, IAppCommandContext appCommandContext) {
        this.orderItemId = guid;
        this.isReturn = isReturn;
        this.saleItemGuid = saleItemGuid;
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
