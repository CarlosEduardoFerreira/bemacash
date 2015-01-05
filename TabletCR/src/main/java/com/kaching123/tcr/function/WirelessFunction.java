package com.kaching123.tcr.function;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.getbase.android.db.provider.ProviderAction;
import com.kaching123.tcr.model.converter.ListConverterFunction;
import com.kaching123.tcr.model.payment.blackstone.prepaid.wireless.WirelessItem;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore.WirelessTable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dot on 15.02.14.
 */
public final class WirelessFunction extends ListConverterFunction<WirelessItem> {

    private static final Uri URI = ShopProvider.getContentUri(WirelessTable.URI_CONTENT);

    private List<String> types;

    public WirelessFunction(List<String> types) {
        this.types = types;
    }
    public WirelessFunction() {
    }

    public static ArrayList<WirelessItem> load(Context context) {
        Cursor c = ProviderAction.query(URI).orderBy(WirelessTable.NAME).perform(context);
        return readItems(c);
    }

    private static ArrayList<WirelessItem> readItems(Cursor c) {
        ArrayList<WirelessItem> items = new ArrayList<WirelessItem>();
        if (c.moveToFirst()) {
            do {
                WirelessItem model = new WirelessItem(c);
                items.add(model);
            } while (c.moveToNext());
        }
        c.close();
        return items;
    }

    @Override
    public WirelessItem apply(Cursor c) {
        super.apply(c);
        return new WirelessItem(c);
    }
}
