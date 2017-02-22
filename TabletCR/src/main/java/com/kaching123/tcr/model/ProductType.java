package com.kaching123.tcr.model;

import android.content.Context;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.R;

/**
 * Created by idyuzheva on 06.08.2014.
 */
public enum ProductType {

    IMPORTED, NATIONAL;

    public static ProductType valueOf(int id) {
        return values()[id];
    }

    public static String getName(final Context context, final ProductType type) {
        if (IMPORTED == type) {
            return context.getString(R.string.product_type_imported);
        } else {
            return context.getString(R.string.product_type_national);
        }
    }

    public static ProductType getByName(final Context context, final String name) {
        Logger.d("static getByName : " + name);
        if ("Imported".equalsIgnoreCase(name) || "Importado".equalsIgnoreCase(name)) {
            Logger.d("static getByName : Imported" + name);
            return ProductType.IMPORTED;
        } else if ("National".equalsIgnoreCase(name) || "Nacional".equalsIgnoreCase(name)) {
            Logger.d("static getByName : National" + name);
            return ProductType.NATIONAL;
        } else {
            Logger.d("static getByName : null");
            return null;
        }
    }
}
