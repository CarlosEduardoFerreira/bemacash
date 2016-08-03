package com.kaching123.tcr.store.composer;


import android.content.ContentProviderResult;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.getbase.android.db.provider.ProviderAction;
import com.kaching123.tcr.commands.store.AsyncCommand;
import com.kaching123.tcr.function.ComposerExWrapFunction;
import com.kaching123.tcr.model.ComposerExModel;
import com.kaching123.tcr.model.ItemModel;
import com.kaching123.tcr.model.converter.ItemFunction;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopSchema2.ComposerView2;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.store.ShopStore.ItemTable;
import com.kaching123.tcr.util.CalculationUtil;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import static com.kaching123.tcr.model.ContentValuesUtil._decimalQty;
import static com.kaching123.tcr.util.CursorUtil._wrap;

/**
 * Created by vkompaniets on 11.02.2016.
 */
public abstract class BaseComposerCommand extends AsyncCommand {

    private static final Uri COMPOSER_VIEW_URI = ShopProvider.contentUri(ShopStore.ComposerView.URI_CONTENT);
    private static final Uri ITEM_URI = ShopProvider.contentUri(ShopStore.ItemTable.URI_CONTENT);
    private static final Uri ITEM_QTY_URI = ShopProvider.contentUri(ShopStore.RecalcQtyQuery.URI_CONTENT);

    protected String hostItemId;

    @Override
    protected void afterCommand(ContentProviderResult[] dbOperationResults) {
        super.afterCommand(dbOperationResults);

        ItemModel hostItem = ProviderAction.query(ITEM_URI)
                .where(ItemTable.GUID + " = ?", hostItemId)
                .perform(getContext())
                .toFluentIterable(new ItemFunction())
                .first().orNull();

        if (hostItem == null)
            return;

        List<ComposerExModel> compositions = _wrap(ProviderAction.query(COMPOSER_VIEW_URI)
                .where(ComposerView2.ComposerTable.ITEM_HOST_ID + " = ?", hostItemId)
                .perform(getContext()), new ComposerExWrapFunction());

        BigDecimal hostCost = null;
        BigDecimal hostQty = null;
        for (ComposerExModel composition : compositions){
            hostCost = (hostCost == null ? BigDecimal.ZERO : hostCost).add(CalculationUtil.getSubTotal(composition.qty, composition.getChildItem().cost));

            if (!composition.restricted)
                continue;

            BigDecimal restrictQty = composition.getChildItem().availableQty.divide(composition.qty, 3, RoundingMode.FLOOR);
            if (hostQty == null || hostQty.compareTo(restrictQty) == 1){
                hostQty = restrictQty;
            }
        }

        if (hostQty == null){
            hostQty = getQtyFromMovement(getContext(), hostItemId);
        }
        if (hostCost == null){
            hostCost = hostItem.cost;
        }

        if (hostQty.compareTo(hostItem.availableQty) != 0 || hostCost.compareTo(hostItem.cost) != 0){
            hostItem.availableQty = hostQty;
            hostItem.cost = hostCost;
            getContext().getContentResolver().update(ITEM_URI, hostItem.toQtyValues(), ItemTable.GUID + " = ?", new String[]{hostItemId});
        }
    }

    private static BigDecimal getQtyFromMovement(Context context, String itemGuid){
        Cursor c = ProviderAction
                .query(ITEM_QTY_URI)
                .projection(ShopStore.ItemMovementTable.QTY)
                .where("", itemGuid)
                .perform(context);

        BigDecimal qty = BigDecimal.ZERO;
        if (c.moveToFirst()){
            qty = _decimalQty(c, 2);
        }
        c.close();

        return qty;
    }
}
