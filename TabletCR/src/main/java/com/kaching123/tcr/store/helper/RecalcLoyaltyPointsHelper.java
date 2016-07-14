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
    private static final Uri URI_LOYALTY_MOVEMENT_GROUP_BY = ShopProvider.contentUriGroupBy(LoyaltyPointsMovementTable.URI_CONTENT, LoyaltyPointsMovementTable.CUSTOMER_ID);

    public RecalcLoyaltyPointsHelper(Context context, SQLiteOpenHelper dbHelper) {
        super(context, dbHelper);
    }

    public void recalculateCustomerLoyaltyPoints(String customerId){
        Cursor c = ProviderAction.query(URI_LOYALTY_MOVEMENT_GROUP_BY)
                .projection("SUM(" + LoyaltyPointsMovementTable.LOYALTY_POINTS + ")")
                .where(LoyaltyPointsMovementTable.CUSTOMER_ID + " = ?", customerId)
                .perform(getContext());

        BigDecimal points = null;
        if (c.moveToFirst()){
            points = _decimal(c, 0);
        }
        c.close();

        ContentValues cv = new ContentValues(1);
        cv.put(CustomerTable.TMP_LOYALTY_POINTS, _decimal(points));

        ContentResolver cr = getContext().getContentResolver();
        cr.update(URI_CUSTOMER, cv, CustomerTable.GUID + " = ?", new String[]{customerId});
    }

    public void recalculateCustomerLoyaltyPoints2(String movementId){
        //we use database directly to avoid using ShopProvider which adds 'isDeleted = 0' statement
        Cursor c = getDbHelper().getReadableDatabase().query(
                LoyaltyPointsMovementTable.TABLE_NAME,
                new String[]{LoyaltyPointsMovementTable.CUSTOMER_ID},
                LoyaltyPointsMovementTable.GUID + " = ?",
                new String[]{movementId},
                null, null, null);

        String customerId = null;
        if (c.moveToFirst()){
            customerId = c.getString(0);
        }
        c.close();

        if (customerId != null)
            recalculateCustomerLoyaltyPoints(customerId);

    }

    public void bulkRecalcCustomerLoyaltyPointsAfterSync(){
        Cursor c = ProviderAction.query(URI_LOYALTY_MOVEMENT_GROUP_BY)
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
