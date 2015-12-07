package com.kaching123.tcr.commands.unitlabel;

import android.net.Uri;
import android.text.TextUtils;

import com.getbase.android.db.provider.ProviderAction;
import com.getbase.android.db.provider.Query;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore;

/**
 * Created by  alboyko 07.12.2015
 */
public class UnitLabelCommandUtils {

    public static final Uri UNIT_LABEL_URI = ShopProvider.contentUri(ShopStore.UnitLabelTable.URI_CONTENT);
    public static final Uri ITEM_URI = ShopProvider.contentUri(ShopStore.ItemTable.URI_CONTENT);

    public static Query unitQuery(String shortcut) {
        Query query = ProviderAction.query(UNIT_LABEL_URI);
        if (!TextUtils.isEmpty(shortcut)) {
            query = query.where(ShopStore.UnitLabelTable.SHORTCUT + " = ?", shortcut);
        }
        return query;
    }

    public static Query unitForeignKeyQuery(String unitLabelId) {
        Query query = ProviderAction.query(ITEM_URI);
        if (!TextUtils.isEmpty(unitLabelId)) {
            query = query.where(ShopStore.ItemTable.UNIT_LABEL_ID + " = ?", unitLabelId);
        }
        return query;
    }

}
