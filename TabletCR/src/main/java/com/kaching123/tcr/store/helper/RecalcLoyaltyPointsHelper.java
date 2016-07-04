package com.kaching123.tcr.store.helper;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

import com.getbase.android.db.provider.ProviderAction;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore.LoyaltyPointsMovementTable;
import com.kaching123.tcr.store.ShopStore.CustomerTable;

import java.math.BigDecimal;

import static com.kaching123.tcr.model.ContentValuesUtil._decimal;

/**
 * Created by vkompaniets on 01.07.2016.
 */
public class RecalcLoyaltyPointsHelper extends ProviderHelper {

    private static final Uri URI_CUSTOMER = ShopProvider.contentUri(CustomerTable.URI_CONTENT);
    private static final Uri URI_LOYALTY_MOVEMENT = ShopProvider.contentUriGroupBy(LoyaltyPointsMovementTable.URI_CONTENT, LoyaltyPointsMovementTable.CUSTOMER_ID);

    public RecalcLoyaltyPointsHelper(Context context, SQLiteOpenHelper dbHelper) {
        super(context, dbHelper);
    }

    public void recalculateCustomerLoyaltyPoints(String customerId, BigDecimal points){
        Cursor c = ProviderAction.query(URI_CUSTOMER)
                .projection(CustomerTable.TMP_LOYALTY_POINTS)
                .where(CustomerTable.GUID + " = ?", customerId)
                .perform(getContext());

        BigDecimal currentPoints = BigDecimal.ZERO;
        if (c.moveToFirst()){
            currentPoints = _decimal(c, 0);
        }
        c.close();

        currentPoints = currentPoints.add(points);
        ContentValues cv = new ContentValues(1);
        cv.put(CustomerTable.TMP_LOYALTY_POINTS, _decimal(currentPoints));

        ContentResolver cr = getContext().getContentResolver();
        cr.update(URI_CUSTOMER, cv, CustomerTable.GUID + " = ?", new String[]{customerId});
    }

    public void bulkRecalcCustomerLoyaltyPointsAfterSync(){
        Cursor c = ProviderAction.query(URI_LOYALTY_MOVEMENT)
                .projection(LoyaltyPointsMovementTable.CUSTOMER_ID, "SUM(" + LoyaltyPointsMovementTable.LOYALTY_POINTS + ")")
                .perform(getContext());

        while (c.moveToNext()){
            String customerId = c.getString(0);
            BigDecimal loyaltyPoints = _decimal(c, 1);
            ContentValues cv = new ContentValues(1);
            cv.put(CustomerTable.TMP_LOYALTY_POINTS, _decimal(loyaltyPoints));

            ContentResolver cr = getContext().getContentResolver();
            cr.update(URI_CUSTOMER, cv, CustomerTable.GUID + " = ?", new String[]{customerId});
        }
        c.close();
    }
}
