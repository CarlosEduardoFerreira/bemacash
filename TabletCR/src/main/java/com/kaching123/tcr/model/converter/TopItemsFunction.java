package com.kaching123.tcr.model.converter;

import android.database.Cursor;

import com.google.common.base.Optional;
import com.kaching123.tcr.model.TopItemModel;
import com.kaching123.tcr.store.ShopSchema2.ReportsTopItemsView2;
import com.kaching123.tcr.store.ShopSchema2.ReportsTopItemsView2.SaleItemTable;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static com.kaching123.tcr.model.ContentValuesUtil._decimalQty;

/**
 * Created by pkabakov on 17.01.14.
 */
public class TopItemsFunction extends ListConverterFunction<Optional<List<TopItemModel>>> {

    private static final int MAX_COUNT = 10;

    @Override
    public Optional<List<TopItemModel>> apply(Cursor cursor) {
        super.apply(cursor);

        if (!cursor.moveToFirst())
            return Optional.fromNullable(null);

        HashMap<String, TopItemModel> itemQuantityMap = new HashMap<String, TopItemModel>();
        do {
            final String guid = cursor.getString(indexHolder.get(ReportsTopItemsView2.ItemTable.GUID));
            final String description = cursor.getString(indexHolder.get(ReportsTopItemsView2.ItemTable.DESCRIPTION));
            final BigDecimal quantity = _decimalQty(cursor, indexHolder.get(SaleItemTable.QUANTITY), BigDecimal.ZERO);
            final BigDecimal refundQuantity = _decimalQty(cursor, indexHolder.get(ReportsTopItemsView2.SaleItemTable.TMP_REFUND_QUANTITY), BigDecimal.ZERO);//negative value
            final BigDecimal resultQuantity = quantity.add(refundQuantity);

            TopItemModel topItemModel = itemQuantityMap.get(guid);
            if (topItemModel == null) {
                topItemModel = new TopItemModel(guid, description);
                itemQuantityMap.put(guid, topItemModel);
            }
            topItemModel.quantity = topItemModel.quantity.add(resultQuantity);

        } while (cursor.moveToNext());

        List<TopItemModel> itemsList = new ArrayList<TopItemModel>(itemQuantityMap.values());
        Collections.sort(itemsList);
        if (itemsList.size() > MAX_COUNT)
            itemsList = itemsList.subList(0, MAX_COUNT);

        return Optional.fromNullable(itemsList);
    }
}
