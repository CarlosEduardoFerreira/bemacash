package com.kaching123.tcr.store;

import android.content.Context;

import com.kaching123.tcr.TcrApplication;

/**
 * Created by pkabakov on 03.07.2014.
 */
public class TrainingShopOpenHelper extends BaseOpenHelper {

    private static final String DB_NAME_PREFIX = "training_";

    protected static String getDbName() {
        return DB_NAME_PREFIX + BaseOpenHelper.getDbName();
    }

    public TrainingShopOpenHelper(Context context) {
        super(context, getDbName(), null, getDbVersion());
    }

    @Override
    protected void clearDbRelatedPreferences() {
        super.clearDbRelatedPreferences();

        TcrApplication.get().setTrainingMode(false);
    }
}
