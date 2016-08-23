package com.kaching123.tcr.store.composer;

import android.content.Context;

import com.getbase.android.db.provider.ProviderAction;
import com.kaching123.tcr.function.ComposerExWrapFunction;
import com.kaching123.tcr.model.ComposerExModel;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopSchema2.ComposerView2;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.util.CalculationUtil;
import com.telly.groundy.PublicGroundyTask;
import com.telly.groundy.TaskResult;
import com.telly.groundy.annotations.OnFailure;
import com.telly.groundy.annotations.OnSuccess;
import com.telly.groundy.annotations.Param;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import static com.kaching123.tcr.util.CursorUtil._wrap;

/**
 * Created by vkompaniets on 07.08.2016.
 */
public class CollectComposersCommand extends PublicGroundyTask {

    private static final String ARG_ITEM_ID = "ARG_ITEM_ID";
    private static final String EXTRA_COMPOSERS = "EXTRA_COMPOSERS";
    private static final String EXTRA_HOST_QTY = "EXTRA_HOST_QTY";
    private static final String EXTRA_HOST_COST = "EXTRA_HOST_COST";

    @Override
    protected TaskResult doInBackground() {
        String hostItemId = getStringArg(ARG_ITEM_ID);

        List<ComposerExModel> composers = _wrap(ProviderAction.query(ShopProvider.contentUri(ShopStore.ComposerView.URI_CONTENT))
                .where(ComposerView2.ComposerTable.ITEM_HOST_ID + " = ?", hostItemId)
                .perform(getContext()), new ComposerExWrapFunction());

        BigDecimal hostCost = null;
        BigDecimal hostQty = null;
        for (ComposerExModel composer : composers){
            hostCost = (hostCost == null ? BigDecimal.ZERO : hostCost).add(CalculationUtil.getSubTotal(composer.qty, composer.getChildItem().cost));

            if (!composer.restricted)
                continue;

            BigDecimal restrictQty = composer.getChildItem().availableQty.divide(composer.qty, 3, RoundingMode.FLOOR);
            if (hostQty == null || hostQty.compareTo(restrictQty) == 1){
                hostQty = restrictQty;
            }
        }

        return succeeded().add(EXTRA_COMPOSERS, new ArrayList<>(composers)).add(EXTRA_HOST_QTY, hostQty).add(EXTRA_HOST_COST, hostCost);
    }

    public static void start(Context context, String itemGuid, ComposerCallback callback) {
        create(CollectComposersCommand.class)
                .arg(ARG_ITEM_ID, itemGuid)
                .callback(callback)
                .queueUsing(context);
    }

    public static abstract class ComposerCallback {

        @OnSuccess(CollectComposersCommand.class)
        public final void onSuccess(@Param(EXTRA_COMPOSERS) List<ComposerExModel> result,
                                    @Param(EXTRA_HOST_QTY) BigDecimal qty,
                                    @Param(EXTRA_HOST_COST) BigDecimal cost) {
            handleSuccess(result, qty, cost);
        }

        protected abstract void handleSuccess(List<ComposerExModel> composers, BigDecimal qty, BigDecimal cost);

        @OnFailure(CollectComposersCommand.class)
        public final void onFailure() {
            handleError();
        }

        protected abstract void handleError();
    }
}
