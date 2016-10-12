package com.kaching123.tcr.function;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.content.Loader;

import com.getbase.android.db.loaders.CursorLoaderBuilder;
import com.google.common.base.Function;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopSchema2.TipsView2.SaleOrderTable;
import com.kaching123.tcr.store.ShopSchema2.TipsView2.TipsTable;
import com.kaching123.tcr.store.ShopStore.TipsView;

import java.math.BigDecimal;

import static com.kaching123.tcr.model.ContentValuesUtil._decimal;

/**
 * Created by pkabakov on 10.06.2014.
 */
public class ReadTipsFunction {

    private static final Uri TIPS_URI = ShopProvider.getContentUri(TipsView.URI_CONTENT);

    public static Loader<BigDecimal> createLoader(Context context, String orderGuid) {
        return CursorLoaderBuilder.forUri(TIPS_URI)
                .projection(TipsTable.AMOUNT)
                .where(SaleOrderTable.GUID + " = ? OR " + SaleOrderTable.PARENT_ID + " = ?", orderGuid, orderGuid)
                .wrap(new Function<Cursor, BigDecimal>() {
                    @Override
                    public BigDecimal apply(Cursor cursor) {
                        BigDecimal result = BigDecimal.ZERO;
                        while (cursor.moveToNext()) {
                            BigDecimal amount = _decimal(cursor, 0, BigDecimal.ZERO);
                            result = result.add(amount);
                        }
                        return result;
                    }
                })
                .build(context);
    }

}
