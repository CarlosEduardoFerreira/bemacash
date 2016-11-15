package com.kaching123.tcr.fragment.dialog;

import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.kaching123.tcr.R;


public class WriteSettingsPermissionDialogFragment extends DialogFragment {

    private static final String WRITE_SETTINGS_PERMISSION_DIALOG_FRAGMENT_TAG = "com.kaching123.tcr.fragment.dialog.WriteSettingsPermissionDialogFragment";

    private static WriteSettingsPermissionDialogFragment instance;

    public static void show(FragmentManager fragmentManager) {
        if (instance == null) {
            instance = new WriteSettingsPermissionDialogFragment();
        } else {
            instance.dismiss();
        }
        instance.show(fragmentManager, WRITE_SETTINGS_PERMISSION_DIALOG_FRAGMENT_TAG);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.grand_write_settings_permission_dialog_fragment, container, false);
        Button button = (Button)v.findViewById(R.id.grand_write_settings_permission_button);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (Settings.System.canWrite(WriteSettingsPermissionDialogFragment.this.getContext())) {
                        instance.dismiss();
                    }
                    else {
                        Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS);
                        intent.setData(Uri.parse("package:" + getActivity().getPackageName()));
                        //intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
                        startActivity(intent);
                    }
                }
            }
        });

        return v;
    }

}
