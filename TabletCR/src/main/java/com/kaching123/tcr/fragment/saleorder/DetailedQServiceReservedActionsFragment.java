package com.kaching123.tcr.fragment.saleorder;

import android.support.v4.app.Fragment;
import android.widget.Button;

import com.kaching123.tcr.R;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

/**
 * Created by mboychenko on 5/29/2017.
 */
@EFragment(R.layout.detailed_qservice_reserved_actions_fragment)
public class DetailedQServiceReservedActionsFragment extends Fragment {

    @ViewById
    protected Button reserved1;
    @ViewById
    protected Button reserved2;
    @ViewById
    protected Button reserved3;
    @ViewById
    protected Button reserved4;
    @ViewById
    protected Button reserved5;
    @ViewById
    protected Button reserved6;
    @ViewById
    protected Button reserved7;

    @AfterViews
    protected void init() {
        reserved5.setEnabled(false);
        reserved6.setEnabled(false);
        reserved7.setEnabled(false);
    }

}
