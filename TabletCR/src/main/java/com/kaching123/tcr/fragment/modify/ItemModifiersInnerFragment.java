package com.kaching123.tcr.fragment.modify;

import android.os.Bundle;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import com.kaching123.tcr.R;

import java.util.ArrayList;

/**
 * Created by gdubina on 26/11/13.
 */
@EFragment(R.layout.item_modifiers_inner_fragment)
public class ItemModifiersInnerFragment extends BaseItemModifiersFragment {

    @FragmentArg
    protected String argItemGuid;

    @FragmentArg
    protected String argSelectedModifierGuid;

    @FragmentArg
    protected ArrayList<String> argSelectedAddonsGuids;

    @FragmentArg
    protected ArrayList<String> argSelectedOptionalsGuids;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setupParams(argItemGuid,
                argSelectedModifierGuid, argSelectedAddonsGuids, argSelectedOptionalsGuids);
    }

}

