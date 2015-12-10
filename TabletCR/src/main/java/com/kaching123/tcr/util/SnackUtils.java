package com.kaching123.tcr.util;

import android.view.View;

//import android.support.design.widget.Snackbar;


/**
 * Created by irikhmayer on 09.06.2015.
 */
public final class SnackUtils {

    public static final void showSnackClose(View parent, int resId) {

        /*final Snackbar snack = Snackbar.make(parent, resId, Snackbar.LENGTH_LONG);
        snack.setAction(R.string.btn_accept, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                snack.dismiss();
            }
        });
        snack.show();*/

    }

    /*public static final void showSnackClose(final SnackBar snack, Activity context, String resId) {
        snack.text(resId);
        snack.actionClickListener(new SnackBar.OnActionClickListener() {
                    @Override
                    public void onActionClick(SnackBar snackBar, int i) {
                        snack.dismiss();
                    }
                });
        snack.applyStyle(R.style.SnackBarSingleLine);
        snack.show(context);
    }*/
}