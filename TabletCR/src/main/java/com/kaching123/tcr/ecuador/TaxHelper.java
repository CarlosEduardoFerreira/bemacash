package com.kaching123.tcr.ecuador;

import android.database.Cursor;

import com.kaching123.tcr.R;
import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.model.ContentValuesUtil;
import com.kaching123.tcr.model.TaxGroupModel;

import java.util.ArrayList;
import java.util.List;

import static com.kaching123.tcr.fragment.UiHelper.qtyFormat;
import static com.kaching123.tcr.model.ContentValuesUtil._decimal;

/**
 * Created by idyuzheva on 09.03.2016.
 */
public class TaxHelper {

    public static final int MAX_TAX_GROUP_COUNT = 2;

    public static String getItemTaxGroupDisplayText(Cursor cursor) {
        String displayText = "";
        if (cursor != null && cursor.moveToFirst()) {
            TaxGroupModel model = new TaxGroupModel(cursor);
            displayText = "(" + _decimal(model.tax) + " %) " + model.title;
            if (cursor.moveToNext()) {
                TaxGroupModel model2 = new TaxGroupModel(cursor);
                displayText += "\n" + "(" + _decimal(model2.tax) + " %) " + model2.title;
            }
        } else {
            displayText = "(" + ContentValuesUtil._decimal(TcrApplication.get().getShopInfo().taxVat) + " %) "
                    + TcrApplication.get().getString(R.string.item_tax_group_default);
        }
        return displayText;
    }


    public static TaxGroupModel getTaxById(List<TaxGroupModel> taxes, String id){
        if (id == null)
            return null;

        for (TaxGroupModel tax : taxes){
            if (id.equals(tax.guid))
                return tax;
        }
        return null;
    }

    public static List<TaxGroupModel> getDefaultTaxes(List<TaxGroupModel> taxes){
        ArrayList<TaxGroupModel> defaultTaxes = new ArrayList<>();
        for (TaxGroupModel tax : taxes){
            if (tax.isDefault)
                defaultTaxes.add(tax);
        }
        return defaultTaxes;
    }

    public static String getTaxDisplayText(List<TaxGroupModel> taxes){
        String builder = "";
        for (TaxGroupModel tax : taxes){
            if (tax == null)
                continue;
            builder += String.format("(%s%%) %s", qtyFormat(tax.tax, false), tax.title) + "\n";
        }
        return builder.substring(0, builder.length() - 1);
    }
}
