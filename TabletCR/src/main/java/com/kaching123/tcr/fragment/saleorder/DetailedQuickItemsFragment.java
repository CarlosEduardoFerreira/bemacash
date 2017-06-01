package com.kaching123.tcr.fragment.saleorder;

import com.kaching123.tcr.R;
import com.kaching123.tcr.adapter.ObjectsCursorAdapter;
import com.kaching123.tcr.fragment.catalog.BaseItemsPickFragment;
import com.kaching123.tcr.model.ItemExModel;

import org.androidannotations.annotations.EFragment;

/**
 * Created by mboychenko on 5/30/2017.
 */
@EFragment(R.layout.detailed_quick_items_fragment)
public class DetailedQuickItemsFragment extends BaseItemsPickFragment {
    @Override
    protected ObjectsCursorAdapter<ItemExModel> createAdapter() {
        return null;
    }

    @Override
    public void setListener(IItemListener listener) {

    }
}
