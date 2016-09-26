package com.kaching123.tcr.store.composer;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;

import com.getbase.android.db.provider.ProviderAction;
import com.getbase.android.db.provider.Query;
import com.kaching123.tcr.Logger;
import com.kaching123.tcr.commands.store.AsyncCommand;
import com.kaching123.tcr.function.ComposerExWrapFunction;
import com.kaching123.tcr.model.ComposerExModel;
import com.kaching123.tcr.model.ComposerModel;
import com.kaching123.tcr.model.ItemExModel;
import com.kaching123.tcr.service.ISqlCommand;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopSchema2;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.store.ShopStore.RecalcQtyQuery;
import com.kaching123.tcr.util.CalculationUtil;
import com.telly.groundy.PublicGroundyTask;
import com.telly.groundy.TaskHandler;
import com.telly.groundy.TaskResult;
import com.telly.groundy.annotations.OnFailure;
import com.telly.groundy.annotations.OnSuccess;
import com.telly.groundy.annotations.Param;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import static com.kaching123.tcr.model.ContentValuesUtil._decimal;
import static com.kaching123.tcr.model.ContentValuesUtil._decimalQty;
import static com.kaching123.tcr.util.CursorUtil._selectionArgs;
import static com.kaching123.tcr.util.CursorUtil._wrap;


/**
 * @author Ivan v. Rikhmayer
 *         This class is intended to
 */
public class RecalculateHostCompositionMetadataCommand extends AsyncCommand {

    private static final Uri COMPOSER_URI = ShopProvider.contentUri(ShopStore.ComposerView.URI_CONTENT);
    private static final Uri ITEM_URI = ShopProvider.contentUri(ShopStore.ItemTable.TABLE_NAME);
    private static final Uri ITEM_MOVEMENT_REAL_URI = ShopProvider.contentUri(ShopStore.ItemMovementTable.URI_CONTENT);
    private static final Uri ITEM_QTY_URI = ShopProvider.contentUri(RecalcQtyQuery.URI_CONTENT);
    private static final Uri URI_ITEM_MOVEMENT = ShopProvider.contentUri(ShopStore.ItemMovementTable.URI_CONTENT);

    private static final String RESULT_ITEM = "RESULT_ITEM";
    private static final String RESULT_ITEM_1 = "RESULT_ITEM_1";
    private static final String RESULT_ITEM_2 = "RESULT_ITEM_2";

    private static final String PARAM_COMPOSER_ITEM = "PARAM_COMPOSER_ITEM";

    protected ArrayList<ContentProviderOperation> ops;

    protected ArrayList<ComposerExModel> list;

    protected BigDecimal quantity;
    protected BigDecimal cost;
    protected String itemGuid;

    @Override
    protected TaskResult doCommand() {

        ops = new ArrayList<ContentProviderOperation>();
        do {
            //item guid
            if (itemGuid == null) {
                itemGuid = getArgs().getString(PARAM_COMPOSER_ITEM);
            }
            list = new ArrayList<ComposerExModel>();

            if (TextUtils.isEmpty(itemGuid)) {
                break;
            }

            // list of composers, not using view to make alg clear to you
            List<ComposerExModel> models = _wrap(syncQuery(itemGuid).perform(getContext()), new ComposerExWrapFunction());
            for (ComposerExModel item : models) {
                list.add(item);
            }

            if (models.size() <= 0) {
                // no items found, abandoning
                break;
            }

            cost = BigDecimal.ZERO;

            for (ComposerExModel itemComposer : list) {

                cost = cost.add(CalculationUtil.getSubTotal(itemComposer.qty, itemComposer.getChildItem().cost));

                if (!itemComposer.restricted) {
                    continue;
                }
                BigDecimal minQtyPerItem = itemComposer.getChildItem().availableQty.divide(itemComposer.qty, 3, RoundingMode.FLOOR);
                if (quantity == null) {
                    // first round, populating
                    quantity = minQtyPerItem;
                } else if (quantity.compareTo(minQtyPerItem) > 0) { // quantity can't be null
                    // if lover, assign
                    quantity = minQtyPerItem;
                }
            }
            // 0 item is guaranteed to exist
            ItemExModel host = models.get(0).getHostItem();

            // for debugging, single usage
            BigDecimal currentQty = host.availableQty;


            if (quantity == null) {
                Cursor c = getContext().getContentResolver().query(
                        ITEM_QTY_URI,
                        new String[]{ShopStore.ItemMovementTable.QTY},
                        null,
                        _selectionArgs(itemGuid),
                        null
                );
                try {
                    if (c.moveToFirst()) {
                        quantity = _decimal(c, 2, BigDecimal.ZERO);
                    } else {
                        break;
                    }
                } finally {
                    c.close();
                }
            }

            if (currentQty.compareTo(quantity) == 0 && cost.compareTo(host.cost) == 0) {
                // no changes, abandoning
                break;
            }
            // could as well rise
            host.availableQty = quantity;
            host.cost = cost;
            // TMP_AVAILABLE_QTY is used to cover the local qty population.
            // we don't have to update the actual movement.
            /*****************************************************************************************************/
            ops.add(ContentProviderOperation.newUpdate(ITEM_URI)
                    .withValues(host.toQtyValues()) // there is only 1 field to update
                    .withSelection(ShopStore.ItemTable.GUID + " = ?", new String[]{host.guid}) // for only one item
                    .build());
            /*****************************************************************************************************/
        } while (false);
        return succeeded().add(RESULT_ITEM, list).add(RESULT_ITEM_1, quantity).add(RESULT_ITEM_2, cost);
    }

    public static Query syncQuery(String guid) {
        Query query = ProviderAction.query(COMPOSER_URI);
        if (guid != null) {
            query = query.where(ShopSchema2.ComposerView2.ComposerTable.ITEM_HOST_ID + " = ?", guid);
        }
        return query;
    }

    private static BigDecimal loadPrevData(Context context, String itemGuid) {
        Cursor c = ProviderAction
                .query(URI_ITEM_MOVEMENT)
                .projection(
                        ShopStore.ItemMovementTable.TMP_AVAILABLE_QTY,
                        ShopStore.ItemMovementTable.GUID
                )
                .where(ShopStore.ItemMovementTable.ITEM_GUID + " = ?", itemGuid)
                .orderBy(ShopStore.ItemMovementTable.CREATE_TIME + " DESC")
                .perform(context);

        BigDecimal result = BigDecimal.ZERO;
        if (c.moveToFirst()) {
            result = _decimalQty(c.getString(0));
            Logger.d("[LOG] loadPrevData for %s = %s", itemGuid, c.getString(1));
        }
        c.close();
        return result;
    }

    @Override
    protected ArrayList<ContentProviderOperation> createDbOperations() {
        return ops;
    }

    @Override
    protected ISqlCommand createSqlCommand() {
        return null;
    }

    public static final TaskHandler start(Context context,
                                          String itemGuid,
                                          ComposerCallback callback) {
        return create(RecalculateHostCompositionMetadataCommand.class)
                .arg(PARAM_COMPOSER_ITEM, itemGuid)
                .callback(callback)
                .queueUsing(context);
    }

    public ArrayList<ComposerExModel> sync(Context context,
                                           String guid,
                                           PublicGroundyTask.IAppCommandContext appCommandContext) {
        this.itemGuid = guid;
        super.sync(context, null, appCommandContext);
        return list;
    }

    public static abstract class ComposerCallback {

        @OnSuccess(RecalculateHostCompositionMetadataCommand.class)
        public final void onSuccess(@Param(RESULT_ITEM) List<ComposerModel> result,
                                    @Param(RESULT_ITEM_1) BigDecimal qty,
                                    @Param(RESULT_ITEM_2) BigDecimal cost) {
            handleSuccess(result, qty, cost);
        }

        protected abstract void handleSuccess(List<ComposerModel> unit, BigDecimal qty, BigDecimal cost);

        @OnFailure(RecalculateHostCompositionMetadataCommand.class)
        public final void onFailure() {
            handleError();
        }

        protected abstract void handleError();
    }
}
