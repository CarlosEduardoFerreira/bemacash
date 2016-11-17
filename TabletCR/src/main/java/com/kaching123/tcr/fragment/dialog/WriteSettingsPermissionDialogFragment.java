package com.kaching123.tcr.fragment.dialog;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.kaching123.tcr.R;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;


@EFragment
public class WriteSettingsPermissionDialogFragment extends StyledDialogFragment {

    private static final String DIALOG_NAME = "com.kaching123.tcr.fragment.dialog.WriteSettingsPermissionDialogFragment";

    @ViewById(R.id.grand_write_settings_permission_button)
    protected Button grandWriteSettingsPermissionButton;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().getWindow().setLayout(getResources().getDimensionPixelOffset(R.dimen.holdon_dlg_width),
                getDialog().getWindow().getAttributes().height);
        setCancelable(false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.grand_write_settings_permission_dialog_fragment, container, false);
        return v;
    }

    @AfterViews
    protected void setOnClickListener() {
        grandWriteSettingsPermissionButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) &&
                        (!Settings.System.canWrite(WriteSettingsPermissionDialogFragment.this.getContext()))) {
                    Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS);
                    intent.setData(Uri.parse("package:" + getActivity().getPackageName()));
                    //intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
                    startActivity(intent);
                }
            }
        });
    }

    @Override
    protected int getDialogContentLayout() {
        return 0;
    }

    @Override
    protected int getDialogTitle() {
        return 0;
    }

    @Override
    protected int getNegativeButtonTitle() {
        return 0;
    }

    @Override
    protected int getPositiveButtonTitle() {
        return 0;
    }

    @Override
    protected OnDialogClickListener getPositiveButtonListener() {
        return null;
    }

    public static void show(FragmentActivity activity) {
        DialogUtil.show(activity, DIALOG_NAME, WriteSettingsPermissionDialogFragment_.builder().build());
    }

    public static void hide(FragmentActivity activity) {
        DialogUtil.hide(activity, DIALOG_NAME);
    }
}
