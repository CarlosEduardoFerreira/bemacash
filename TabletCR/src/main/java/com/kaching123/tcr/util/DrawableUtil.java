package com.kaching123.tcr.util;

import android.graphics.drawable.Drawable;

/**
 * Created by hamsterksu on 11.12.13.
 */
public final class DrawableUtil {

    private DrawableUtil(){}

    public static Drawable boundDrawable(Drawable d){
        d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
        return d;
    }
}
