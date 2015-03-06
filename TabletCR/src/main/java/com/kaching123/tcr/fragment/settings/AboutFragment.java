package com.kaching123.tcr.fragment.settings;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.widget.TextView;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;
import com.kaching123.tcr.R;
import com.kaching123.tcr.fragment.SuperBaseFragment;
import com.kaching123.tcr.model.ApplicationVersion;
import com.kaching123.tcr.util.Util;

/**
 * Created by pkabakov on 21/05/14.
 */
@EFragment(R.layout.settings_about_fragment)
public class AboutFragment extends SuperBaseFragment{

    @ViewById
    protected TextView dataValue;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ApplicationVersion version = Util.getApplicationVersion(getActivity());
        dataValue.setText(getString(R.string.about_version_value, version.name, version.code));
    }

    public static Fragment instance() {
        return AboutFragment_.builder().build();
    }
}
