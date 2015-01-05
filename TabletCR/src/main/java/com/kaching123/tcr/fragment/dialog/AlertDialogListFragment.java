package com.kaching123.tcr.fragment.dialog;

import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.EFragment;
import com.googlecode.androidannotations.annotations.ViewById;
import com.kaching123.tcr.R;

/**
 * Created by gdubina on 10.02.14.
 */
@EFragment
public class AlertDialogListFragment extends AlertDialogFragment {

    @ViewById(android.R.id.list)
    protected ListView listView;

    protected ListAdapter adapter;

    @Override
    protected int getDialogContentLayout() {
        return R.layout.dialog_alert_list_dialog_fragment;
    }

    @Override
    @AfterViews
    protected void bind() {
        super.bind();
        icon.setVisibility(View.GONE);
        listView.setAdapter(adapter);
    }

    public void setAdapter(ListAdapter adapter) {
        this.adapter = adapter;
    }

    public static void show(FragmentActivity activity, ListAdapter adapter) {
        show(activity, activity.getString(R.string.inventory_import_failed_msg_list), adapter);
    }

    public static void show(FragmentActivity activity, String message, ListAdapter adapter) {
        DialogUtil.show(activity, DIALOG_NAME,
                         AlertDialogListFragment_.builder()
                                 .titleId(R.string.error_dialog_title)
                                 .errorMsg(message)
                                 .positiveButtonTitleId(R.string.btn_ok)
                                 .dialogType(DialogType.ALERT).build())
                         .setAdapter(adapter);
    }
}
