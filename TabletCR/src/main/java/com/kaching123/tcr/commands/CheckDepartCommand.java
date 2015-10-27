package com.kaching123.tcr.commands;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.getbase.android.db.provider.ProviderAction;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore;
import com.telly.groundy.Groundy;
import com.telly.groundy.PublicGroundyTask;
import com.telly.groundy.TaskResult;
import com.telly.groundy.annotations.OnFailure;
import com.telly.groundy.annotations.OnSuccess;

import java.util.ArrayList;

/**
 * Created by Teli on 2015/10/27.
 */
public class CheckDepartCommand extends PublicGroundyTask {

    private final static String ARG_STR_DEPARTMENT = "ARG_STR_DEPARTMENT";
    private static final Uri URI_DEPARTMENTS = ShopProvider.getContentUri(ShopStore.DepartmentTable.URI_CONTENT);

    @Override
    protected TaskResult doInBackground() {
        String strDepart = getStringArg(ARG_STR_DEPARTMENT);
        ArrayList<String> list = new ArrayList<String>();
        Cursor c = ProviderAction.query(URI_DEPARTMENTS)
                .projection(
                        ShopStore.DepartmentTable.TITLE
                )
                .perform(getContext());
        while (c.moveToNext()) {
            list.add(c.getString(0));
        }

        return isDepartDuplicated(strDepart, list) ? succeeded() : failed();
    }

    private boolean isDepartDuplicated(String strDepart, ArrayList<String> list) {
        for (int i = 0; i < list.size(); i++) {
            if (strDepart.equalsIgnoreCase(list.get(i)))
                return false;
        }
        return true;
    }

    public static abstract class CheckDepartCommandListener {
        @OnSuccess(CheckDepartCommand.class)
        public void onSuccess() {
            handleSuccess();
        }

        @OnFailure(CheckDepartCommand.class)
        public void onFail() {
            handleFailure();
        }

        protected abstract void handleSuccess();

        protected abstract void handleFailure();
    }

    public static void create(Context context, String strDepart, CheckDepartCommandListener listener) {
        Groundy.create(CheckDepartCommand.class).arg(ARG_STR_DEPARTMENT, strDepart).callback(listener).queueUsing(context);
    }
}
