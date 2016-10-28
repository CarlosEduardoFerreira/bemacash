package com.kaching123.tcr.fragment.dialog;

import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.kaching123.tcr.fragment.user.TimesheetNewFragment;

/**
 * Created by gdubina on 11/11/13.
 */
public class DialogUtil {

    public static <T extends DialogFragment> T show(FragmentActivity activity, String dialogName, T newFragment){
        assert activity != null;
        return show(activity.getSupportFragmentManager(), dialogName, newFragment);
    }

    public static <T extends DialogFragment> T show(FragmentManager manager, String dialogName, T newFragment){
        assert manager != null;
        assert newFragment != null;

        hide(manager, dialogName);
        newFragment.show(manager, dialogName);
        return newFragment;
    }

    public static void hide(FragmentActivity activity, String dialogName) {
        assert activity != null;
        hide(activity.getSupportFragmentManager(), dialogName);
    }

    public static void hide(FragmentManager fm, String dialogName) {
        assert fm != null;
        FragmentTransaction ft = fm.beginTransaction();
        DialogFragment prev = (DialogFragment)fm.findFragmentByTag(dialogName);
        if (prev == null) {
            prev = (DialogFragment)fm.findFragmentByTag(TimesheetNewFragment.class.getSimpleName());
        }
        if (prev != null) {
            prev.dismiss();
        }
    }
}
