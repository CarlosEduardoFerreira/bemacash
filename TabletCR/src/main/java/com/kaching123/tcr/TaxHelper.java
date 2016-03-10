package com.kaching123.tcr;

import android.database.Cursor;

import com.kaching123.tcr.model.TaxGroupModel;

import static com.kaching123.tcr.model.ContentValuesUtil._decimal;

/**
 * Created by idyuzheva on 09.03.2016.
 */
public class TaxHelper {

    public static final int MAX_TAX_GROUP_COUNT = 2;

    public static String getItemTaxGroupDisplayText(Cursor cursor) {
        String displayText = "";
        if (cursor.moveToFirst()) {
            TaxGroupModel model = new TaxGroupModel(cursor);
            displayText = "(" + _decimal(model.tax) + " %) " + model.title;
            if (cursor.moveToNext()) {
                TaxGroupModel model2 = new TaxGroupModel(cursor);
                displayText += "\n" + "(" + _decimal(model2.tax) + " %) " + model2.title;
            }
        }
        return displayText;
    }
}
