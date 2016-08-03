package com.kaching123.tcr.store.composer;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;

import com.getbase.android.db.provider.ProviderAction;
import com.kaching123.tcr.commands.store.AsyncCommand;
import com.kaching123.tcr.function.ComposerExWrapFunction;
import com.kaching123.tcr.model.ComposerExModel;
import com.kaching123.tcr.model.ComposerModel;
import com.kaching123.tcr.model.ItemModel;
import com.kaching123.tcr.model.converter.ItemFunction;
import com.kaching123.tcr.service.ISqlCommand;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopSchema2.ComposerView2.ComposerTable;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.store.ShopStore.ItemTable;
import com.telly.groundy.TaskHandler;
import com.telly.groundy.TaskResult;
import com.telly.groundy.annotations.OnFailure;
import com.telly.groundy.annotations.OnSuccess;
import com.telly.groundy.annotations.Param;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static com.kaching123.tcr.util.CursorUtil._wrap;

/**
 * Created by vkompaniets on 03.08.2016.
 */
public class RecalculateHostCompositionMetadataCommand2 extends AsyncCommand {

    private static final Uri COMPOSER_URI = ShopProvider.contentUri(ShopStore.ComposerView.URI_CONTENT);
    private static final Uri ITEM_URI = ShopProvider.contentUri(ShopStore.ItemTable.TABLE_NAME);
    private static final Uri ITEM_MOVEMENT_REAL_URI = ShopProvider.contentUri(ShopStore.ItemMovementTable.URI_CONTENT);
    private static final Uri ITEM_QTY_URI = ShopProvider.contentUri(ShopStore.RecalcQtyQuery.URI_CONTENT);
    private static final Uri URI_ITEM_MOVEMENT = ShopProvider.contentUri(ShopStore.ItemMovementTable.URI_CONTENT);

    private static final String RESULT_ITEM = "RESULT_ITEM";
    private static final String RESULT_ITEM_1 = "RESULT_ITEM_1";
    private static final String RESULT_ITEM_2 = "RESULT_ITEM_2";

    private static final String PARAM_COMPOSER_ITEM = "PARAM_COMPOSER_ITEM";

    protected ArrayList<ContentProviderOperation> ops;

    protected List<ComposerExModel> list;

    protected BigDecimal quantity;
    protected BigDecimal cost;
    protected String itemGuid;

    @Override
    protected TaskResult doCommand() {
        String itemGuid = getArgs().getString(PARAM_COMPOSER_ITEM);

        if (TextUtils.isEmpty(itemGuid))
            return failed();


        ItemModel hostItem = ProviderAction.query(ITEM_URI)
                .where(ItemTable.GUID + " = ?", itemGuid)
                .perform(getContext())
                .toFluentIterable(new ItemFunction())
                .first().orNull();

        if (hostItem == null)
            return failed();

        List<ComposerExModel> compositions = _wrap(ProviderAction.query(COMPOSER_URI)
                .where(ComposerTable.ITEM_HOST_ID + " = ?", itemGuid)
                .perform(getContext()), new ComposerExWrapFunction());

        return succeeded().add(RESULT_ITEM, new ArrayList<>(compositions)).add(RESULT_ITEM_1, hostItem.availableQty).add(RESULT_ITEM_2, hostItem.cost);
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
        return  create(RecalculateHostCompositionMetadataCommand2.class)
                .arg(PARAM_COMPOSER_ITEM, itemGuid)
                .callback(callback)
                .queueUsing(context);
    }

    public static abstract class ComposerCallback {

        @OnSuccess(RecalculateHostCompositionMetadataCommand2.class)
        public final void onSuccess(@Param(RESULT_ITEM) List<ComposerModel> result,
                                    @Param(RESULT_ITEM_1) BigDecimal qty,
                                    @Param(RESULT_ITEM_2) BigDecimal cost) {
            handleSuccess(result, qty, cost);
        }

        protected abstract void handleSuccess(List<ComposerModel> unit, BigDecimal qty, BigDecimal cost);

        @OnFailure(RecalculateHostCompositionMetadataCommand2.class)
        public final void onFailure() {
            handleError();
        }

        protected abstract void handleError();
    }
}

