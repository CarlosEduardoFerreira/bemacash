package com.kaching123.tcr.fragment.prepaid;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.Click;
import com.googlecode.androidannotations.annotations.EFragment;
import com.googlecode.androidannotations.annotations.ViewById;
import com.kaching123.tcr.R;

/**
 * Created by teli.yin on 10/28/2014.
 */
@EFragment
public class PrepaidHomeHeadFragment extends PrepaidHomeHeadBaseFragment {

    @ViewById
    protected TextView homeButton;
    @ViewById
    protected ImageView backButton;
    @ViewById
    protected TextView headInstruction;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.prepaid_home_head_fragment, container, false);
    }

    @AfterViews
    public void init() {
        String inTro = PrepaidHomeHeadBaseFragment.SELECT_PRODUCT;
        if (getArguments() != null)
            if (getArguments().getString(PrepaidHomeHeadBaseFragment.HEAD_INSTRUCTION) != null)
                inTro = getArguments().getString(PrepaidHomeHeadBaseFragment.HEAD_INSTRUCTION);
        if (inTro.equalsIgnoreCase(PrepaidHomeHeadBaseFragment.SELECT_AMOUNT))
            headInstruction.setText(PrepaidHomeHeadBaseFragment.SELECT_AMOUNT);

    }

    @Click
    void homeButton() {
        Toast.makeText(getActivity(), getResources().getString(R.string.prepaid_home_page_already), Toast.LENGTH_LONG).show();
    }

    @Click
    void backButton() {
        Toast.makeText(getActivity(), getResources().getString(R.string.prepaid_home_page_already), Toast.LENGTH_LONG).show();
    }
}
