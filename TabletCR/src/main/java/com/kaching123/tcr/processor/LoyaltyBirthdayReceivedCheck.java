package com.kaching123.tcr.processor;

import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.getbase.android.db.provider.ProviderAction;
import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by bemacarl on 05.22.2017.
 */
public class LoyaltyBirthdayReceivedCheck {

    private static final Uri CUSTOMER_URI = ShopProvider.contentUri(ShopStore.CustomerTable.URI_CONTENT);

    public boolean checkIfBirthdayWasAppliedOnCurrentYear(String customerGuid){
        Log.d("BemaCarl23","LoyaltyBirthdayCheck.checkIfBirthdayWasAppliedOnCurrentYear.customerGuid:    " + customerGuid);
        Cursor c1 = ProviderAction.query(CUSTOMER_URI)
                .projection( ShopStore.CustomerTable.BIRTHDAY_REWARD_RECEIVED_DATE )
                .where(ShopStore.CustomerTable.GUID + " = ?", customerGuid)
                .perform(TcrApplication.get().getApplicationContext());
        if(c1.moveToNext()) {
            int birthday_reward_received_date = c1.getInt(0);
            try {
                Log.d("BemaCarl23","LoyaltyBirthdayCheck.checkIfBirthdayWasAppliedOnCurrentYear.birthday_reward_apply_date:    " + birthday_reward_received_date);
                java.util.Date completeDate =   new java.util.Date((long)birthday_reward_received_date);
                Log.d("BemaCarl23","LoyaltyBirthdayCheck.checkIfBirthdayWasAppliedOnCurrentYear.completeDate:    " + completeDate);
                Calendar cal = Calendar.getInstance();
                cal.setTime(completeDate);
                int month   = cal.get(Calendar.MONTH) + 1;
                int day     = cal.get(Calendar.DATE);
                int year    = cal.get(Calendar.YEAR);
                Log.d("BemaCarl23","LoyaltyBirthdayCheck.checkIfBirthdayWasAppliedOnCurrentYear.year:    " + year);

                Calendar calendar = Calendar.getInstance();
                int actualYear = calendar.get(Calendar.YEAR);
                Log.d("BemaCarl23","LoyaltyBirthdayCheck.checkIfBirthdayWasAppliedOnCurrentYear.actualYear:    " + actualYear);
                if(year >= actualYear){
                    Log.d("BemaCarl23","LoyaltyBirthdayCheck.checkIfBirthdayWasAppliedOnCurrentYear if(year <= actualYear){:    true ");
                    return true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        return false;
    }

}
