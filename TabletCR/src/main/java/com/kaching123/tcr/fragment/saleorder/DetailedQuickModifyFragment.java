package com.kaching123.tcr.fragment.saleorder;

import com.kaching123.tcr.R;
import com.kaching123.tcr.activity.BaseQuickServiceActiviry;
import com.kaching123.tcr.fragment.modify.ItemModifiersFragment;
import com.kaching123.tcr.fragment.quickservice.QuickModifyFragment;

import org.androidannotations.annotations.EFragment;

/**
 * Created by mboychenko on 5/30/2017.
 */
@EFragment(R.layout.detailed_quick_modify_fragment)
public class DetailedQuickModifyFragment extends ItemModifiersFragment implements BaseQuickServiceActiviry.IModifierFragmentBaseActions {

    @Override
    public void setCancelListener(QuickModifyFragment.OnCancelListener listener) {

    }

    @Override
    public void setupParams(String itemGuid, String saleItemGuid, OnAddonsChangedListener listener) {

    }

    @Override
    public void setupParams(String itemGuid, OnAddonsChangedListener listener) {

    }
}
